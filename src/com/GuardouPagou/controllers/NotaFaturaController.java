package com.GuardouPagou.controllers;

import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO;
import com.GuardouPagou.models.DatabaseConnection;
import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.Marca;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.views.NotaFaturaView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotaFaturaController {

    private static final Logger LOGGER = Logger.getLogger(NotaFaturaController.class.getName());

    private final NotaFaturaView view;
    private final NotaFiscalDAO notaFiscalDAO;
    private final FaturaDAO faturaDAO;
    private final MarcaDAO marcaDAO;
    private NotaFiscal notaFiscalEmEdicao;

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final NumberFormat CURRENCY_FORMAT
            = NumberFormat.getCurrencyInstance(PT_BR);

    // Construtor para MODO CADASTRO
    public NotaFaturaController(NotaFaturaView view) {
        this.view = view;
        this.notaFiscalDAO = new NotaFiscalDAO();
        this.faturaDAO = new FaturaDAO();
        this.marcaDAO = new MarcaDAO();
        this.notaFiscalEmEdicao = null;

        configurarEventos();
        carregarMarcas();
    }

    // Construtor para MODO EDIÇÃO
    public NotaFaturaController(NotaFaturaView view, NotaFiscal notaFiscal, ObservableList<Fatura> faturas) {
        this.view = view;
        this.notaFiscalDAO = new NotaFiscalDAO();
        this.faturaDAO = new FaturaDAO();
        this.marcaDAO = new MarcaDAO();
        this.notaFiscalEmEdicao = notaFiscal;

        configurarEventos();
        carregarMarcas();
    }

    private void configurarEventos() {
        view.getNumeroNotaField().textProperty()
                .addListener((ignoredObs, ignoredOld, newVal) -> {
                    if (!newVal.matches("\\d*")) {
                        view.getNumeroNotaField()
                                .setText(newVal.replaceAll("\\D", ""));
                    }
                });

        view.getSpinnerFaturas().valueProperty()
                .addListener((ignoredObs, ignoredOld, newVal)
                        -> view.inicializarFaturas(newVal)
                );

        view.getBtnLimpar().setOnAction(this::onLimpar);
        view.getBtnGravar().setOnAction(this::onGravar);
    }

    private void onLimpar(ActionEvent ignored) {
        limparFormulario();
    }

    private void onGravar(ActionEvent ignored) {
        salvarNotaFiscal();
    }

    private void carregarMarcas() {
        try {
            var nomes = marcaDAO.listarMarcas().stream()
                    .map(Marca::getNome)
                    .toList();

            view.getMarcaComboBox()
                    .setItems(FXCollections.observableArrayList(nomes));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar marcas", ex);
            mostrarAlerta(
                    "Erro ao carregar marcas: " + ex.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    private void salvarNotaFiscal() {
        String numNota = view.getNumeroNotaField().getText().trim();
        LocalDate data = view.getDataEmissaoPicker().getValue();
        String marcaNome = view.getMarcaComboBox().getValue();

        if (numNota.isEmpty() || data == null || marcaNome == null || view.getVencimentosColumn().getChildren().isEmpty()) {
            mostrarAlerta(
                    "Erro ao salvar, verifique os campos obrigatorios!",
                    Alert.AlertType.ERROR
            );
            return;
        }

        List<Fatura> faturasColetadas = new ArrayList<>();
        // CORREÇÃO AQUI: Começar o índice do loop em 0, mas acessar os filhos com i+1 para pular o Label de cabeçalho
        for (int i = 0; i < view.getSpinnerFaturas().getValue(); i++) {
            // Acessar o VBox correto pulando o Label de cabeçalho (índice 0)
            VBox vencBox = (VBox) view.getVencimentosColumn().getChildren().get(i + 1); // ADICIONADO +1
            VBox valBox = (VBox) view.getValoresColumn().getChildren().get(i + 1);    // ADICIONADO +1
            VBox statusBox = (VBox) view.getStatusColumn().getChildren().get(i + 1); // ADICIONADO +1

            DatePicker vencDp = (DatePicker) vencBox.getChildren().get(1);
            TextField valTf = (TextField) valBox.getChildren().get(1);
            ComboBox<String> statusCb = (ComboBox<String>) statusBox.getChildren().get(1);

            LocalDate venc = vencDp.getValue();
            String valTxt = valTf.getText().trim();
            String status = statusCb.getValue();

            if (venc == null || venc.isBefore(data) || valTxt.isEmpty() || status == null) {
                mostrarAlerta(
                        "Erro ao salvar, verifique todos os campos das faturas!",
                        Alert.AlertType.ERROR
                );
                return;
            }

            try {
                String cleanVal = valTxt.replaceAll("[^\\d,]", "").replace(",", ".");
                double valor = Double.parseDouble(cleanVal);

                Fatura fatura = new Fatura();
                fatura.setNumeroFatura(i + 1);
                fatura.setVencimento(venc);
                fatura.setValor(valor);
                fatura.setStatus(status);

                if (valTf.getUserData() instanceof Integer) {
                    fatura.setId((Integer) valTf.getUserData());
                }

                faturasColetadas.add(fatura);

            } catch (NumberFormatException e) {
                mostrarAlerta("Valor da fatura invalido!", Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            if (notaFiscalEmEdicao == null) {
                if (notaFiscalDAO.existeNotaFiscal(numNota)) {
                    mostrarAlerta("Erro: Ja existe uma Nota Fiscal com este numero!", Alert.AlertType.ERROR);
                    return;
                }
            } else {
                if (notaFiscalDAO.existeNotaFiscalComOutroId(numNota, notaFiscalEmEdicao.getId())) {
                    mostrarAlerta("Erro: Ja existe outra Nota Fiscal com este numero!", Alert.AlertType.ERROR);
                    return;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar numero da Nota Fiscal", e);
            mostrarAlerta(
                    "Erro ao verificar numero da Nota Fiscal: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            NotaFiscal nf = new NotaFiscal(numNota, data, marcaNome, faturasColetadas);
            nf.setId(view.getNotaFiscalIdParaEdicao());

            int notaFiscalId = nf.getId();

            if (notaFiscalEmEdicao == null) {
                notaFiscalId = notaFiscalDAO.inserirNotaFiscal(nf);
                if (notaFiscalId == -1) {
                    throw new SQLException("Erro ao inserir Nota Fiscal.");
                }
                nf.setId(notaFiscalId);
                faturaDAO.inserirFaturas(faturasColetadas, nf.getId());

                mostrarAlerta("Nota Fiscal cadastrada com sucesso!", Alert.AlertType.INFORMATION);

            } else {
                if (!notaFiscalDAO.atualizarNotaFiscal(nf)) {
                    throw new SQLException("Erro ao atualizar Nota Fiscal.");
                }

                faturaDAO.excluirFaturasPorNotaFiscalId(nf.getId());
                for (Fatura fatura : faturasColetadas) {
                    fatura.setNotaFiscalId(nf.getId());
                    faturaDAO.inserirFatura(fatura);
                }
                mostrarAlerta("Nota Fiscal atualizada com sucesso!", Alert.AlertType.INFORMATION);
            }

            conn.commit();
            limparFormulario();
            Stage stage = (Stage) view.getRoot().getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar nota fiscal", e);
            try {
                var conn = DatabaseConnection.getConnection();
                if (conn != null && !conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erro no rollback", ex);
            }
            mostrarAlerta(
                    "Erro ao salvar nota fiscal: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    private void limparFormulario() {
        view.getNumeroNotaField().clear();
        view.getDataEmissaoPicker().setValue(null);
        view.getMarcaComboBox().setValue(null);
        view.getSpinnerFaturas().getValueFactory().setValue(1);

        view.inicializarFaturas(1);
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
