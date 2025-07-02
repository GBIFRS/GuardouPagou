package com.GuardouPagou.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.models.Fatura;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Locale;

public class NotaFaturaView {

    private final BorderPane root;

    // Campos principais da Nota Fiscal
    private final TextField numeroNotaField;
    private final DatePicker dataEmissaoPicker;
    private final ComboBox<String> marcaComboBox;
    private int notaFiscalIdParaEdicao = -1;

    // Spinner de nº de faturas
    private final Spinner<Integer> spinnerFaturas;
    // Container para as linhas de vencimento/valor/status
    private final VBox vencimentosColumn;
    private final VBox valoresColumn;
    private final VBox statusColumn;

    // Botões
    private final Button btnLimpar;
    private final Button btnGravar;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(PT_BR);

    // Construtor padrão para o MODO CADASTRO
    public NotaFaturaView() {
        this.numeroNotaField = new TextField();
        this.dataEmissaoPicker = new DatePicker();
        this.marcaComboBox = new ComboBox<>();
        this.spinnerFaturas = new Spinner<>(1, 100, 1);
        this.vencimentosColumn = new VBox(10);
        this.valoresColumn = new VBox(10);
        this.statusColumn = new VBox(10);
        this.btnLimpar = new Button("Limpar");
        this.btnGravar = new Button("Gravar");
        this.root = new BorderPane();
        criarUI();
        inicializarFaturas(this.spinnerFaturas.getValue());
    }

    // Construtor para o MODO EDIÇÃO
    public NotaFaturaView(NotaFiscal notaFiscalParaEdicao, ObservableList<Fatura> faturasParaEdicao) {
        this.numeroNotaField = new TextField();
        this.dataEmissaoPicker = new DatePicker();
        this.marcaComboBox = new ComboBox<>();
        this.vencimentosColumn = new VBox(10);
        this.valoresColumn = new VBox(10);
        this.statusColumn = new VBox(10);

        this.spinnerFaturas = new Spinner<>(1, 100, faturasParaEdicao.size());

        this.btnLimpar = new Button("Limpar");
        this.btnGravar = new Button("Gravar");
        this.root = new BorderPane();
        criarUI();

        if (notaFiscalParaEdicao != null) {
            this.notaFiscalIdParaEdicao = notaFiscalParaEdicao.getId();
            this.numeroNotaField.setText(notaFiscalParaEdicao.getNumeroNota());
            this.dataEmissaoPicker.setValue(notaFiscalParaEdicao.getDataEmissao());
            this.marcaComboBox.setValue(notaFiscalParaEdicao.getMarca());
            this.spinnerFaturas.getValueFactory().setValue(faturasParaEdicao.size());
        }

        inicializarFaturas(faturasParaEdicao.size());

        preencherFaturasParaEdicao(faturasParaEdicao);
    }

    private void criarUI() {
        root.getStyleClass().add("nota-fatura-root");
        root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        root.setStyle("-fx-background-color: #323437; -fx-padding: 20;");

        // ——— HEADER ———
        Label titulo = new Label("Cadastro/Edição de Faturas");
        titulo.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.web("#F0A818"));
        Label sub1 = new Label("Dados da Nota Fiscal Eletrônica");
        sub1.setFont(Font.font("Poppins", FontWeight.NORMAL, 14));
        sub1.setTextFill(Color.web("#7890A8"));
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #7890A8;");
        VBox headerBox = new VBox(8, titulo, sub1, sep1);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // ——— DADOS DA NOTA ———
        Label lblNumero = new Label("# Número da NF-e");
        lblNumero.getStyleClass().add("field-subtitle");
        numeroNotaField.setPromptText("Digite o nº da NF-e");
        numeroNotaField.setPrefWidth(250);
        VBox numeroBox = new VBox(6, lblNumero, numeroNotaField);
        numeroBox.getStyleClass().add("pill-field");

        Label lblData = new Label("Data de Emissão");
        lblData.getStyleClass().add("field-subtitle");
        dataEmissaoPicker.setPromptText("DD/MM/AAAA");
        dataEmissaoPicker.setPrefWidth(250);
        VBox dataBox = new VBox(6, lblData, dataEmissaoPicker);
        dataBox.getStyleClass().add("pill-field");

        Label lblMarca = new Label("Marca");
        lblMarca.getStyleClass().add("field-subtitle");
        marcaComboBox.setPromptText("Selecione");
        marcaComboBox.setPrefWidth(250);
        VBox marcaBox = new VBox(6, lblMarca, marcaComboBox);
        marcaComboBox.getEditor().getStyleClass().add("text-field");
        marcaBox.getStyleClass().add("pill-field");

        HBox dadosNota = new HBox(20, numeroBox, dataBox, marcaBox);
        dadosNota.setPadding(new Insets(15, 0, 15, 0));
        dadosNota.setAlignment(Pos.CENTER_LEFT);

        // ——— SUBTÍTULO FATURAS ———
        Label sub2 = new Label("Dados da Fatura");
        sub2.setFont(Font.font("Poppins", FontWeight.NORMAL, 14));
        sub2.setTextFill(Color.web("#7890A8"));
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #7890A8;");

        // ——— SEÇÃO FATURAS ———
        Label lblSpinner = new Label("# Nº de Faturas");
        lblSpinner.getStyleClass().add("field-subtitle");
        spinnerFaturas.setPrefWidth(100);
        VBox spinnerBox = new VBox(6, lblSpinner, spinnerFaturas);
        spinnerBox.getStyleClass().add("pill-field");

        // Botões
        btnLimpar.getStyleClass().addAll("modal-button", "icon-clean");
        btnLimpar.setFont(Font.font("Poppins", FontWeight.BOLD, 16));
        btnGravar.getStyleClass().addAll("modal-button", "icon-save");
        btnGravar.setFont(Font.font("Poppins", FontWeight.BOLD, 16));

        btnLimpar.setPrefSize(120, 40);
        btnGravar.setPrefSize(120, 40);

        VBox leftPanel = new VBox(20, spinnerBox, btnLimpar, btnGravar);
        leftPanel.setAlignment(Pos.TOP_CENTER);

        // Colunas scrolláveis para Vencimento, Valor e STATUS
        vencimentosColumn.setPadding(new Insets(10));
        vencimentosColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");
        valoresColumn.setPadding(new Insets(10));
        valoresColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");
        statusColumn.setPadding(new Insets(10));
        statusColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");

        ScrollPane spVenc = new ScrollPane(vencimentosColumn);
        spVenc.setFitToWidth(true);
        spVenc.setPrefSize(220, 250);
        ScrollPane spVal = new ScrollPane(valoresColumn);
        spVal.setFitToWidth(true);
        spVal.setPrefSize(220, 250);
        ScrollPane spStatus = new ScrollPane(statusColumn);
        spStatus.setFitToWidth(true);
        spStatus.setPrefSize(220, 250);

        HBox faturasSection = new HBox(25, leftPanel, spVenc, spVal, spStatus);
        faturasSection.setAlignment(Pos.TOP_LEFT);

        // ——— AGRUPA TUDO ———
        VBox container = new VBox(12, headerBox, dadosNota, sub2, sep2, faturasSection);
        container.setAlignment(Pos.TOP_LEFT);
        root.setCenter(container);
    }

    public void inicializarFaturas(int quantidade) {
        vencimentosColumn.getChildren().clear();
        valoresColumn.getChildren().clear();
        statusColumn.getChildren().clear();

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
            vencimentosColumn.getChildren().add(vbV);

            // — VALOR —
            VBox vbVal = new VBox(5);
            vbVal.getStyleClass().add("pill-field");
            Label lblVal = new Label("Valor Fatura " + i + ":");
            lblVal.getStyleClass().add("field-subtitle");

            TextField tf = new TextField();
            tf.setPrefWidth(150);
            tf.setText(CURRENCY_FORMAT.format(BigDecimal.ZERO));

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

                // --- ALTERACAO AQUI PARA MAIOR ROBUSTEZ NA MASCARA ---
                // Verifica se o texto ja e o que esperamos para evitar loops e problemas de cursor
                if (!tf.getText().equals(formatted)) {
                    tf.setText(formatted);
                    // Posiciona o cursor no final do texto
                    tf.positionCaret(formatted.length());
                } else if (newText.isEmpty()) { // Caso especifico de campo vazio
                    tf.positionCaret(0);
                }
                // --- FIM DA ALTERACAO ---

                updating[0] = false;
            });

            vbVal.getChildren().addAll(lblVal, tf);
            valoresColumn.getChildren().add(vbVal);

            // — STATUS (NOVO) —
            VBox vbStatus = new VBox(5);
            vbStatus.getStyleClass().add("pill-field");
            Label lblStatus = new Label("Status Fatura " + i + ":");
            lblStatus.getStyleClass().add("field-subtitle");

            ComboBox<String> statusCb = new ComboBox<>();
            statusCb.setPrefWidth(150);
            statusCb.setItems(FXCollections.observableArrayList("Não Emitida", "Emitida"));
            statusCb.setValue("Não Emitida");

            vbStatus.getChildren().addAll(lblStatus, statusCb);
            statusColumn.getChildren().add(vbStatus);
        }
    }

    public void preencherFaturasParaEdicao(ObservableList<Fatura> faturas) {
        for (int i = 0; i < faturas.size(); i++) {
            Fatura fatura = faturas.get(i);
            // Vencimento
            DatePicker dp = (DatePicker) ((VBox) vencimentosColumn.getChildren().get(i)).getChildren().get(1);
            dp.setValue(fatura.getVencimento());

            // Valor
            TextField tf = (TextField) ((VBox) valoresColumn.getChildren().get(i)).getChildren().get(1);
            tf.setText(CURRENCY_FORMAT.format(fatura.getValor()));

            // Status
            ComboBox<String> statusCb = (ComboBox<String>) ((VBox) statusColumn.getChildren().get(i)).getChildren().get(1);
            statusCb.setValue(fatura.getStatus());

            tf.setUserData(fatura.getId());
        }
    }

    // === GETTERS ===
    public BorderPane getRoot() {
        return root;
    }

    public TextField getNumeroNotaField() {
        return numeroNotaField;
    }

    public DatePicker getDataEmissaoPicker() {
        return dataEmissaoPicker;
    }

    public ComboBox<String> getMarcaComboBox() {
        return marcaComboBox;
    }

    public Spinner<Integer> getSpinnerFaturas() {
        return spinnerFaturas;
    }

    public VBox getVencimentosColumn() {
        return vencimentosColumn;
    }

    public VBox getValoresColumn() {
        return valoresColumn;
    }

    public VBox getStatusColumn() {
        return statusColumn;
    }

    public Button getBtnLimpar() {
        return btnLimpar;
    }

    public Button getBtnGravar() {
        return btnGravar;
    }

    public int getNotaFiscalIdParaEdicao() {
        return notaFiscalIdParaEdicao;
    }
}
