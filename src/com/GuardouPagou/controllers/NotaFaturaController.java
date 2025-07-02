package com.GuardouPagou.controllers;

import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO;
import com.GuardouPagou.models.DatabaseConnection;
import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.views.NotaFaturaView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

public class NotaFaturaController {

    private final NotaFaturaView view;
    private final NotaFiscalDAO notaFiscalDAO;
    private final FaturaDAO faturaDAO;
    private final MarcaDAO marcaDAO;
    private NotaFiscal notaFiscalEmEdicao;

    private final DateTimeFormatter formatter
            = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMAT
            = NumberFormat.getCurrencyInstance(PT_BR);


    // Construtor para MODO CADASTRO
    public NotaFaturaController(NotaFaturaView view) {
        this.view = view;
        this.notaFiscalDAO = new NotaFiscalDAO();
        this.faturaDAO = new FaturaDAO();
        this.marcaDAO = new MarcaDAO();
        this.notaFiscalEmEdicao = null;

        // A view já inicializa os campos no construtor padrão
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

        // A view já inicializa os campos no construtor de edição
        configurarEventos();
        carregarMarcas();
    }


    private void configurarEventos() {
        view.getNumeroNotaField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getNumeroNotaField().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        view.getSpinnerFaturas().valueProperty()
                .addListener((obs, o, n) -> view.inicializarFaturas(n)); // Chama o método na view

        view.getBtnLimpar().setOnAction(e -> limparFormulario());
        view.getBtnGravar().setOnAction(e -> salvarNotaFiscal());
    }

    private void carregarMarcas() {
        try {
            List<String> nomes = marcaDAO.listarMarcas()
                    .stream()
                    .map(m -> m.getNome())
                    .toList();
            view.getMarcaComboBox()
                    .setItems(FXCollections.observableArrayList(nomes));
        } catch (SQLException e) {
            mostrarAlerta("Erro ao carregar marcas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ESTE MÉTODO AGORA ESTÁ NA VIEW, NÃO MAIS AQUI NO CONTROLLER
    /*
    private void inicializarFaturas(int quantidade) {
        // ... (conteúdo do método movido para NotaFaturaView) ...
    }
    */

    private void salvarNotaFiscal() {
        String numNota = view.getNumeroNotaField().getText().trim();
        LocalDate data = view.getDataEmissaoPicker().getValue();
        String marcaNome = view.getMarcaComboBox().getValue();

        if (numNota.isEmpty() || data == null || marcaNome == null || view.getVencimentosColumn().getChildren().isEmpty()) {
            mostrarAlerta("Erro ao salvar, verifique os campos obrigatórios!", Alert.AlertType.ERROR);
            return;
        }

        List<Fatura> faturasColetadas = new ArrayList<>();
        for (int i = 0; i < view.getSpinnerFaturas().getValue(); i++) {
            VBox vencBox = (VBox) view.getVencimentosColumn().getChildren().get(i);
            VBox valBox = (VBox) view.getValoresColumn().getChildren().get(i);
            VBox statusBox = (VBox) view.getStatusColumn().getChildren().get(i);

            DatePicker vencDp = (DatePicker) vencBox.getChildren().get(1);
            TextField valTf = (TextField) valBox.getChildren().get(1);
            ComboBox<String> statusCb = (ComboBox<String>) statusBox.getChildren().get(1);

            LocalDate venc = vencDp.getValue();
            String valTxt = valTf.getText().trim();
            String status = statusCb.getValue();

            if (venc == null || venc.isBefore(data) || valTxt.isEmpty() || status == null) {
                mostrarAlerta("Erro ao salvar, verifique todos os campos das faturas!", Alert.AlertType.ERROR);
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
                mostrarAlerta("Valor da fatura inválido!", Alert.AlertType.ERROR);
                return;
            }
        }

        try {
            if (notaFiscalEmEdicao == null) {
                if (notaFiscalDAO.existeNotaFiscal(numNota)) {
                    mostrarAlerta("Erro: Já existe uma Nota Fiscal com este número!", Alert.AlertType.ERROR);
                    return;
                }
            } else {
                if (notaFiscalDAO.existeNotaFiscalComOutroId(numNota, notaFiscalEmEdicao.getId())) {
                    mostrarAlerta("Erro: Já existe outra Nota Fiscal com este número!", Alert.AlertType.ERROR);
                    return;
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Erro ao verificar número da Nota Fiscal: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
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
                for(Fatura fatura : faturasColetadas) {
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
            mostrarAlerta("Erro ao salvar nota fiscal: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null && !conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void limparFormulario() {
        view.getNumeroNotaField().clear();
        view.getDataEmissaoPicker().setValue(null);
        view.getMarcaComboBox().setValue(null);
        view.getSpinnerFaturas().getValueFactory().setValue(1);

        view.inicializarFaturas(1); // Chama o método na view
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}