package com.GuardouPagou.controllers;

import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.Marca;
import com.GuardouPagou.views.ArquivadasView;
import com.GuardouPagou.views.MainView;
import com.GuardouPagou.views.MarcaView;
import com.GuardouPagou.views.NotaFaturaView;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.controllers.MarcaController;
import com.GuardouPagou.controllers.NotaFaturaController;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;

public class MainController {

    private MainView view;
    private Button botaoSelecionado;

    public MainController(MainView view) {
        this.view = view;
        configurarEventos();
    }

    private void configurarEventos() {
        view.getBtnListarFaturas().setOnAction(e -> {
            try {
                ObservableList<Fatura> faturas = new FaturaDAO().listarFaturas();
                view.mostrarListaFaturas(faturas);

            } catch (SQLException ex) {
                view.getConteudoLabel().setText("Erro ao carregar faturas.");
                ex.printStackTrace();
            }
        });

        view.getBtnListarMarcas().setOnAction(e -> {
            try {
                ObservableList<Marca> marcas = new MarcaDAO().listarMarcas();
                view.mostrarListaMarcas(marcas);
            } catch (SQLException ex) {
                view.getConteudoLabel().setText("Erro ao carregar marcas.");
                ex.printStackTrace();
            }
        });

        view.getBtnArquivadas().setOnAction(e -> {
            // Em vez de: atualizarConteudo("Documentos Arquivados");
            ArquivadasView arquivadasView = new ArquivadasView();
            new ArquivadasController(arquivadasView); // O controller carrega os dados
            view.getRoot().setCenter(arquivadasView.getRoot());
        });

        view.getBtnNovaFatura().setOnAction(e -> {
            Stage modal = new Stage();
            Window owner = view.getRoot().getScene().getWindow();
            modal.initOwner(owner);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle("Cadastro de Nota Fiscal");

            NotaFaturaView notaView = new NotaFaturaView();
            new NotaFaturaController(notaView);

            // Define largura x altura maiores
            Scene cena = new Scene(notaView.getRoot(), 700, 500);

            modal.setScene(cena);
            modal.setResizable(false);
            modal.setOnShown(ev -> {
                /* centraliza como antes */
            });
            modal.showAndWait();
        });

        view.getBtnNovaMarca().setOnAction(e -> {
            Stage modal = new Stage();
            Window owner = view.getRoot().getScene().getWindow();
            modal.initOwner(owner);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle("Cadastro de Marca");

            MarcaView marcaView = new MarcaView();
            new MarcaController(marcaView);

            // Define largura x altura maiores
            Scene scene = new Scene(marcaView.getRoot(), 650, 400);

            modal.setScene(scene);
            modal.setResizable(false);
            modal.setOnShown(ev -> {
                /* centraliza como antes */
            });
            modal.showAndWait();
        });

        view.getBtnSalvarEmail().setOnAction(e -> {
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
}
