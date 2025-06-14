package com.GuardouPagou.controllers;

import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO;
import com.GuardouPagou.models.DatabaseConnection;
import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.views.NotaFaturaView;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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

    // formatação de data pt-BR
    private final DateTimeFormatter formatter
            = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

    // constantes para máscara de moeda
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMAT
            = NumberFormat.getCurrencyInstance(PT_BR);

    // listas dinâmicas de campos
    private final List<DatePicker> vencimentoPickers = new ArrayList<>();
    private final List<TextField> valorFields = new ArrayList<>();

    public NotaFaturaController(NotaFaturaView view) {
        this.view = view;
        this.notaFiscalDAO = new NotaFiscalDAO();
        this.faturaDAO = new FaturaDAO();
        this.marcaDAO = new MarcaDAO();

        configurarEventos();
        carregarMarcas();
        inicializarFaturas(view.getSpinnerFaturas().getValue());
    }

    private void configurarEventos() {
        // só dígitos no nº da nota
        view.getNumeroNotaField().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getNumeroNotaField().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // recria campos ao mudar nº de faturas
        view.getSpinnerFaturas().valueProperty()
                .addListener((obs, o, n) -> inicializarFaturas(n));

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

    private void inicializarFaturas(int quantidade) {
        vencimentoPickers.clear();
        valorFields.clear();
        view.getVencimentosColumn().getChildren().clear();
        view.getValoresColumn().getChildren().clear();

        for (int i = 1; i <= quantidade; i++) {
            // — VENCIMENTO —
            VBox vbV = new VBox(5);
            vbV.getStyleClass().add("pill-field");
            Label lblV = new Label("Vencimento Fatura " + i + ":");
            lblV.getStyleClass().add("field-subtitle");

            DatePicker dp = new DatePicker();
            dp.setPromptText("DD/MM/AAAA");
            dp.setPrefWidth(150);
            dp.setConverter(new javafx.util.StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate d) {
                    return d == null ? "" : formatter.format(d);
                }

                @Override
                public LocalDate fromString(String s) {
                    try {
                        return (s == null || s.isBlank())
                                ? null
                                : LocalDate.parse(s, formatter);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            });

            vbV.getChildren().addAll(lblV, dp);
            view.getVencimentosColumn().getChildren().add(vbV);
            vencimentoPickers.add(dp);

            // — VALOR —
            VBox vbVal = new VBox(5);
            vbVal.getStyleClass().add("pill-field");
            Label lblVal = new Label("Valor Fatura " + i + ":");
            lblVal.getStyleClass().add("field-subtitle");

            TextField tf = new TextField();
            tf.setPrefWidth(150);
            // inicia em R$ 0,00
            tf.setText(CURRENCY_FORMAT.format(BigDecimal.ZERO));

            // listener para formatar em tempo real
            final boolean[] updating = {false};
            tf.textProperty().addListener((obs, oldText, newText) -> {
                if (updating[0]) {
                    return;
                }
                updating[0] = true;

                String digits = newText.replaceAll("\\D", "");
                long cents = digits.isEmpty() ? 0L : Long.parseLong(digits);
                BigDecimal value = BigDecimal.valueOf(cents, 2);

                String formatted = CURRENCY_FORMAT.format(value);
                tf.setText(formatted);
                tf.positionCaret(formatted.length());

                updating[0] = false;
            });

            vbVal.getChildren().addAll(lblVal, tf);
            view.getValoresColumn().getChildren().add(vbVal);
            valorFields.add(tf);
        }
    }

    private void salvarNotaFiscal() {
        String numNota = view.getNumeroNotaField().getText().trim();
        LocalDate data = view.getDataEmissaoPicker().getValue();
        String marca = view.getMarcaComboBox().getValue();
        if (numNota.isEmpty() || data == null || marca == null || vencimentoPickers.isEmpty()) {
            mostrarAlerta("Erro ao cadastrar, verifique os campos!", Alert.AlertType.ERROR);
            return;
        }

        for (int i = 0; i < vencimentoPickers.size(); i++) {
            LocalDate venc = vencimentoPickers.get(i).getValue();
            String valTxt = valorFields.get(i).getText().trim();
            if (venc == null || venc.isBefore(data) || valTxt.isEmpty()) {
                mostrarAlerta("Erro ao cadastrar, verifique os campos!", Alert.AlertType.ERROR);
                return;
            }
        }

        try (var conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            NotaFiscal nf = new NotaFiscal(numNota, data, marca, new ArrayList<>());
            int nfId = notaFiscalDAO.inserirNotaFiscal(nf);

            List<Fatura> lista = new ArrayList<>();
            for (int i = 0; i < vencimentoPickers.size(); i++) {
                LocalDate venc = vencimentoPickers.get(i).getValue();
                String text = valorFields.get(i).getText();
                // remove tudo que não for dígito ou vírgula, e normaliza o decimal
                String clean = text
                        .replaceAll("[^\\d,]", "") // mantém só dígitos e vírgulas
                        .replace(",", ".");         // converte vírgula em ponto

                double valor = Double.parseDouble(clean);
                lista.add(new Fatura(
                        /* nº da fatura */i + 1,
                        /* vencimento  */ venc,
                        /* valor       */ valor,
                        /* status      */ "Não Emitida"
                ));
            }
            faturaDAO.inserirFaturas(lista, nfId);
            conn.commit();

            mostrarAlerta("Faturas cadastradas com sucesso!", Alert.AlertType.INFORMATION);
            limparFormulario();

        } catch (Exception e) {
            mostrarAlerta("Erro ao salvar nota fiscal: " + e.getMessage(), Alert.AlertType.ERROR);
            try {
                DatabaseConnection.getConnection().rollback();
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

        vencimentoPickers.clear();
        valorFields.clear();
        view.getVencimentosColumn().getChildren().clear();
        view.getValoresColumn().getChildren().clear();
        inicializarFaturas(1);
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
