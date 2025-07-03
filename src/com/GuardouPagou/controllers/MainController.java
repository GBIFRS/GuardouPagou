package com.GuardouPagou.controllers;

import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.Marca;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.views.ArquivadasView;
import com.GuardouPagou.views.MainView;
import com.GuardouPagou.views.MarcaView;
import com.GuardouPagou.views.NotaFaturaView;
import com.GuardouPagou.views.NotaFiscalDetalhesView;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO;
import com.GuardouPagou.controllers.NotaFiscalDetalhesController;
import com.GuardouPagou.controllers.NotaFaturaController;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.Node;
import java.util.Objects;
import java.sql.SQLException;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    private MainView view;
    private Button botaoSelecionado;

    public MainController(MainView view) {
        this.view = view;
        configurarEventos();
    }

    private void configurarEventos() {
        view.getBtnListarFaturas().setOnAction(eventListarFaturas -> {
            ProgressIndicator pi = new ProgressIndicator();
            VBox boxCarregando = new VBox(new Label("Carregando faturas..."), pi);
            boxCarregando.setAlignment(Pos.CENTER);
            boxCarregando.setSpacing(10);
            view.setConteudoPrincipal(boxCarregando);

            Task<ObservableList<Fatura>> carregarFaturasTask = new Task<>() {
                @Override
                protected ObservableList<Fatura> call() throws Exception {
                    return new FaturaDAO().listarFaturas(false);
                }
            };

            carregarFaturasTask.setOnSucceeded(event -> {
                ObservableList<Fatura> faturas = carregarFaturasTask.getValue();
                Node viewFaturas = view.criarViewFaturas(faturas);
                view.setConteudoPrincipal(viewFaturas);
                view.setNotaDoubleClickHandler(this::abrirDetalhesNotaFiscal);
            });

            carregarFaturasTask.setOnFailed(event -> {
                view.getConteudoLabel().setText("Erro ao carregar faturas: " + carregarFaturasTask.getException().getMessage());
                carregarFaturasTask.getException().printStackTrace();
            });

            new Thread(carregarFaturasTask).start();
        });

        view.getBtnListarMarcas().setOnAction(eventListarMarcas -> {
            try {
                ObservableList<Marca> marcas = new MarcaDAO().listarMarcas();
                Node viewMarcas = view.criarViewMarcas(marcas);
                view.setConteudoPrincipal(viewMarcas);
            } catch (SQLException ex) {
                view.getConteudoLabel().setText("Erro ao carregar marcas: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        view.getBtnArquivadas().setOnAction(eventArquivadas -> {
            ArquivadasView arquivadasView = new ArquivadasView();
            new ArquivadasController(arquivadasView);
            view.setConteudoPrincipal(arquivadasView.getRoot());
        });

        view.getBtnNovaFatura().setOnAction(eventNovaFatura -> {
            Stage modal = new Stage();
            Window owner = view.getRoot().getScene().getWindow();
            modal.initOwner(owner);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle("Cadastro de Nota Fiscal");

            modal.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/plus.png"))));

            NotaFaturaView notaView = new NotaFaturaView();
            new NotaFaturaController(notaView);

            Scene cena = new Scene(notaView.getRoot(), 850, 650);
            cena.getStylesheets().addAll(
                    view.getRoot().getScene().getStylesheets()
            );

            cena.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ESCAPE) {
                    modal.close();
                }
            });

            modal.setOnHidden(eventModalHidden -> {
                view.getBtnListarFaturas().fire();
            });

            modal.setScene(cena);
            modal.setResizable(false);
            modal.showAndWait();
        });

        view.getBtnNovaMarca().setOnAction(eventNovaMarca -> {
            Stage modal = new Stage();
            Window owner = view.getRoot().getScene().getWindow();
            modal.initOwner(owner);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle("Cadastro de Marca");

            modal.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/plus.png"))));

            MarcaView marcaView = new MarcaView();
            new MarcaController(marcaView);

            Scene scene = new Scene(marcaView.getRoot(), 650, 400);
            scene.getStylesheets().addAll(
                    view.getRoot().getScene().getStylesheets()
            );

            scene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ESCAPE) {
                    modal.close();
                }
            });

            modal.setScene(scene);
            modal.setResizable(false);
            modal.showAndWait();
            view.getBtnListarMarcas().fire();
        });

        view.getBtnSalvarEmail().setOnAction(eventSalvarEmail -> {
            String email = view.getEmailField().getText();
            if (validarEmail(email)) {
                atualizarConteudo("E-mail para alertas salvo: " + email);
            } else {
                atualizarConteudo("E-mail inválido!");
            }
        });
    }

    private void atualizarConteudo(String texto) {
        view.getConteudoLabel().setText(texto);
    }

    private boolean validarEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    // MÉTODO abrirDetalhesNotaFiscal - REMOVIDO O LOG DE DEPURACAO
    private void abrirDetalhesNotaFiscal(Fatura fatura) {
        try {
            NotaFiscalDAO notaFiscalDAO = new NotaFiscalDAO();
            NotaFiscal nota = notaFiscalDAO.obterNotaFiscalPorId(fatura.getNotaFiscalId());

            if (nota == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Detalhes da Nota Fiscal nao encontrados.");
                alert.showAndWait();
                return;
            }
            // Logs de depuração removidos aqui
            // } else {
            //     LOGGER.log(Level.INFO, "NotaFiscal carregada: ID=" + nota.getId() + 
            //                            ", Numero=" + nota.getNumeroNota() + 
            //                            ", Data Emissao=" + nota.getDataEmissao() + 
            //                            ", Marca=" + nota.getMarca() + 
            //                            ", Arquivada=" + nota.isArquivada() + 
            //                            ", Data Arq=" + nota.getDataArquivamento());
            //     if (nota.getNumeroNota() == null || nota.getNumeroNota().isEmpty()) {
            //         LOGGER.log(Level.WARNING, "Numero da Nota Fiscal esta vazio/nulo no objeto.");
            //     }
            //     if (nota.getDataEmissao() == null) {
            //         LOGGER.log(Level.WARNING, "Data de Emissao da Nota Fiscal esta nula no objeto.");
            //     }
            //     if (nota.getMarca() == null || nota.getMarca().isEmpty()) {
            //         LOGGER.log(Level.WARNING, "Marca da Nota Fiscal esta vazio/nulo no objeto.");
            //     }
            // }

            Stage modal = new Stage();
            Window owner = view.getRoot().getScene().getWindow();
            modal.initOwner(owner);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle("Detalhes da Nota Fiscal " + nota.getNumeroNota());

            modal.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/list.png"))));

            NotaFiscalDetalhesView detalhesView = new NotaFiscalDetalhesView();
            NotaFiscalDetalhesController controller = new NotaFiscalDetalhesController(detalhesView, modal);
            controller.preencherDados(nota);

            Scene scene = new Scene(detalhesView.getRoot(), 850, 650);
            scene.getStylesheets().addAll(view.getRoot().getScene().getStylesheets());

            scene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.ESCAPE) {
                    modal.close();
                }
            });

            modal.setScene(scene);
            modal.setResizable(false);
            modal.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Erro ao abrir detalhes: " + ex.getMessage());
            a.showAndWait();
            ex.printStackTrace();
        }
    }
}
