package com.GuardouPagou.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class NotaFaturaView {

    private final BorderPane root;

    // Campos principais
    private final TextField numeroNotaField;
    private final DatePicker dataEmissaoPicker;
    private final ComboBox<String> marcaComboBox;

    // Spinner de nº de faturas
    private final Spinner<Integer> spinnerFaturas;
    // Container para as linhas de vencimento/valor
    private final VBox vencimentosColumn;
    private final VBox valoresColumn;

    // Botões
    private final Button btnLimpar;
    private final Button btnGravar;

    public NotaFaturaView() {
        numeroNotaField = new TextField();
        dataEmissaoPicker = new DatePicker();
        marcaComboBox = new ComboBox<>();
        spinnerFaturas = new Spinner<>(1, 100, 1);
        vencimentosColumn = new VBox(10);
        valoresColumn = new VBox(10);
        btnLimpar = new Button("Limpar");
        btnGravar = new Button("Gravar");
        root = new BorderPane();
        criarUI();
    }

    private void criarUI() {
        // Carrega o CSS de estilos e aplica classe de root
        root.getStyleClass().add("nota-fatura-root");
        root.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        root.setStyle("-fx-background-color: #323437; -fx-padding: 20;");

        // ——— HEADER ———
        Label titulo = new Label("Cadastro de Faturas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.web("#F0A818"));
        Label sub1 = new Label("Dados da Nota Fiscal Eletrônica");
        sub1.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
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
        // força aplicação da classe TextField no editor do ComboBox
        marcaComboBox.getEditor().getStyleClass().add("text-field");
        marcaBox.getStyleClass().add("pill-field");

        HBox dadosNota = new HBox(20, numeroBox, dataBox, marcaBox);
        dadosNota.setPadding(new Insets(15, 0, 15, 0));
        dadosNota.setAlignment(Pos.CENTER_LEFT);

        // ——— SUBTÍTULO FATURAS ———
        Label sub2 = new Label("Dados da Fatura");
        sub2.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        sub2.setTextFill(Color.web("#7890A8"));
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #7890A8;");

        // ——— SEÇÃO FATURAS ———
        // Spinner dentro de um pill-field
        Label lblSpinner = new Label("# Nº de Faturas");
        lblSpinner.getStyleClass().add("field-subtitle");
        spinnerFaturas.setPrefWidth(100);
        VBox spinnerBox = new VBox(6, lblSpinner, spinnerFaturas);
        spinnerBox.getStyleClass().add("pill-field");

        // Botões
        btnLimpar.getStyleClass().addAll("modal-button", "icon-clean");
        btnLimpar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btnGravar.getStyleClass().addAll("modal-button", "icon-save");
        btnGravar.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        btnLimpar.setPrefSize(120, 40);
        btnGravar.setPrefSize(120, 40);

        VBox leftPanel = new VBox(20, spinnerBox, btnLimpar, btnGravar);
        leftPanel.setAlignment(Pos.TOP_CENTER);

        // Colunas scrolláveis
        vencimentosColumn.setPadding(new Insets(10));
        vencimentosColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");
        valoresColumn.setPadding(new Insets(10));
        valoresColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");
        ScrollPane spVenc = new ScrollPane(vencimentosColumn);
        spVenc.setFitToWidth(true);
        spVenc.setPrefSize(220, 250);
        ScrollPane spVal = new ScrollPane(valoresColumn);
        spVal.setFitToWidth(true);
        spVal.setPrefSize(220, 250);

        HBox faturasSection = new HBox(25, leftPanel, spVenc, spVal);
        faturasSection.setAlignment(Pos.TOP_LEFT);

        // ——— AGRUPA TUDO ———
        VBox container = new VBox(12, headerBox, dadosNota, sub2, sep2, faturasSection);
        container.setAlignment(Pos.TOP_LEFT);
        root.setCenter(container);
    }

    private VBox wrapField(TextField field, String labelText, String prompt) {
        Label lbl = new Label(labelText);
        lbl.setTextFill(Color.web("#323437"));
        field.setPromptText(prompt);
        field.setPrefWidth(250);
        field.setStyle(
                "-fx-background-color: transparent; "
                + "-fx-text-fill: #323437; "
                + "-fx-prompt-text-fill: #323437; "
                + "-fx-border-width: 0;"
        );
        VBox box = new VBox(6, lbl, field);
        box.setStyle(
                "-fx-background-color: #BDBDBD; "
                + "-fx-background-radius: 10; "
                + "-fx-padding: 12;"
        );
        return box;
    }

    private VBox wrapDatePicker(DatePicker dp, String labelText, String prompt) {
        Label lbl = new Label(labelText);
        lbl.setTextFill(Color.web("#323437"));
        dp.setPromptText(prompt);
        dp.setPrefWidth(250);
        dp.setStyle(
                "-fx-background-color: transparent; "
                + "-fx-text-fill: #323437; "
                + "-fx-border-width: 0;"
        );
        VBox box = new VBox(6, lbl, dp);
        box.setStyle(
                "-fx-background-color: #BDBDBD; "
                + "-fx-background-radius: 10; "
                + "-fx-padding: 12;"
        );
        return box;
    }

    private VBox wrapComboBox(ComboBox<String> cb, String labelText, String prompt) {
        Label lbl = new Label(labelText);
        lbl.setTextFill(Color.web("#323437"));
        cb.setPromptText(prompt);
        cb.setPrefWidth(250);
        cb.setStyle(
                "-fx-background-color: transparent; "
                + "-fx-text-fill: #323437; "
                + "-fx-border-width: 0;"
        );
        VBox box = new VBox(6, lbl, cb);
        box.setStyle(
                "-fx-background-color: #BDBDBD; "
                + "-fx-background-radius: 10; "
                + "-fx-padding: 12;"
        );
        return box;
    }

    // === GETTERS para o controller ===
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

    public Button getBtnLimpar() {
        return btnLimpar;
    }

    public Button getBtnGravar() {
        return btnGravar;
    }
}
