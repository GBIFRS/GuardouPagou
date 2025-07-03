package com.GuardouPagou.controllers;

import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.views.NotaFiscalDetalhesView;
import com.GuardouPagou.views.NotaFaturaView; // Para abrir a tela de edicao
import com.GuardouPagou.dao.FaturaDAO; // Para buscar faturas
import com.GuardouPagou.dao.NotaFiscalDAO; // Para buscar Nota Fiscal
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.image.Image; // Importar Image para o icone

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotaFiscalDetalhesController {

    private static final Logger LOGGER = Logger.getLogger(NotaFiscalDetalhesController.class.getName());

    private final NotaFiscalDetalhesView view;
    private final Stage stage;

    private NotaFiscal notaFiscalDetalhes;
    private ObservableList<Fatura> faturasDetalhes;

    public NotaFiscalDetalhesController(NotaFiscalDetalhesView view, Stage stage) {
        this.view = view;
        this.stage = stage;
        configurarEventos();
    }

    private void configurarEventos() {
        view.getBtnFechar().setOnAction(e -> stage.close());

        view.getBtnEditar().setOnAction(e -> abrirModalEdicao());
    }

    // Metodo para preencher os dados na view (Chamado do MainController)
    public void preencherDados(NotaFiscal notaFiscal) {
        this.notaFiscalDetalhes = notaFiscal;

        try {
            // Recarrega as faturas para garantir que sao as mais recentes
            this.faturasDetalhes = FXCollections.observableArrayList(new FaturaDAO().listarFaturasDaNota(notaFiscal.getId()));

            // CORRECAO: Chamar o metodo preencherDados da view com os objetos corretos
            view.preencherDados(this.notaFiscalDetalhes, this.faturasDetalhes);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar faturas para detalhes da nota " + notaFiscal.getId(), ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar faturas para a tela de detalhes: " + ex.getMessage());
            alert.showAndWait();
            stage.close();
        }
    }

    private void abrirModalEdicao() {
        try {
            if (notaFiscalDetalhes.isArquivada()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atencao");
                alert.setHeaderText(null);
                alert.setContentText("Nao e possivel editar uma Nota Fiscal arquivada.");
                alert.showAndWait();
                return;
            }

            Stage edicaoModalStage = new Stage();
            Window owner = stage.getOwner();
            edicaoModalStage.initOwner(owner);
            edicaoModalStage.initModality(Modality.WINDOW_MODAL);
            edicaoModalStage.setTitle("Editar Nota Fiscal e Faturas");

            NotaFaturaView edicaoView = new NotaFaturaView(this.notaFiscalDetalhes, this.faturasDetalhes);
            new NotaFaturaController(edicaoView, this.notaFiscalDetalhes, this.faturasDetalhes);

            Scene scene = new Scene(edicaoView.getRoot(), 850, 650);
            edicaoModalStage.setScene(scene);
            edicaoModalStage.setResizable(false);

            // Icone da modal de edicao (comentar se o arquivo nao existir)
            // edicaoModalStage.getIcons().add(
            //     new Image(
            //         Objects.requireNonNull(
            //             getClass().getResourceAsStream("/icons/edit.png")
            //         )
            //     )
            // );
            edicaoModalStage.setOnHidden(e -> {
                stage.close();
            });

            edicaoModalStage.showAndWait();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar dados para edicao da Nota Fiscal " + notaFiscalDetalhes.getId(), e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao abrir tela de edicao: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
