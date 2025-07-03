package com.GuardouPagou.views;

import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.NotaFiscal;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Locale;
import javafx.scene.Node;

public class NotaFiscalDetalhesView {

    private final BorderPane root;

    // Campos da Nota Fiscal (LABELS PARA SOMENTE LEITURA)
    private final Label numeroNotaValueLabel;
    private final Label dataEmissaoValueLabel;
    private final Label marcaValueLabel;
    private final Label arquivadaValueLabel;
    private final Label dataArquivamentoValueLabel;

    // Container para as faturas
    private final VBox vencimentosColumn;
    private final VBox valoresColumn;
    private final VBox statusColumn;

    // Botões
    private final Button btnFechar;
    private final Button btnEditar;

    // Formatadores
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final java.text.NumberFormat CURRENCY_FORMAT = java.text.NumberFormat.getCurrencyInstance(PT_BR);

    public NotaFiscalDetalhesView() {
        numeroNotaValueLabel = new Label();
        dataEmissaoValueLabel = new Label();
        marcaValueLabel = new Label();
        arquivadaValueLabel = new Label();
        dataArquivamentoValueLabel = new Label();

        vencimentosColumn = new VBox(10);
        valoresColumn = new VBox(10);
        statusColumn = new VBox(10);

        btnFechar = new Button("Fechar");
        btnEditar = new Button("Editar");

        root = new BorderPane();
        criarUI();
    }

    private void criarUI() {
        root.getStyleClass().addAll("nota-fatura-root", "detalhes-nota-root");
        root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        root.setStyle("-fx-background-color: #323437; -fx-padding: 20;");

        // --- HEADER ---
        Label titulo = new Label("Detalhes da Nota Fiscal");
        titulo.setFont(Font.font("Poppins", FontWeight.BOLD, 26));
        titulo.setTextFill(Color.web("#F0A818"));
        Label sub1 = new Label("Informações Gerais da NF-e");
        sub1.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));
        sub1.setTextFill(Color.web("#7890A8"));
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #7890A8;");
        VBox headerBox = new VBox(8, titulo, sub1, sep1);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // --- DADOS DA NOTA (Usando Labels para exibição) ---
        VBox dadosNotaBox = new VBox(12);
        dadosNotaBox.setPadding(new Insets(15, 0, 15, 0));

        // Numero da Nota
        Label lblNumeroNF = new Label("# Numero da NF-e:");
        lblNumeroNF.setStyle("-fx-text-fill: #BDBDBD; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox numeroBox = new HBox(5, lblNumeroNF, numeroNotaValueLabel);
        numeroNotaValueLabel.getStyleClass().add("detalhe-valor");
        numeroNotaValueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        dadosNotaBox.getChildren().add(numeroBox);

        // Data de Emissao
        Label lblDataEmissao = new Label("Data de Emissão:");
        lblDataEmissao.setStyle("-fx-text-fill: #BDBDBD; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox dataBox = new HBox(5, lblDataEmissao, dataEmissaoValueLabel);
        dataEmissaoValueLabel.getStyleClass().add("detalhe-valor");
        dataEmissaoValueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        dadosNotaBox.getChildren().add(dataBox);

        // Marca
        Label lblMarca = new Label("Marca:");
        lblMarca.setStyle("-fx-text-fill: #BDBDBD; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox marcaBox = new HBox(5, lblMarca, marcaValueLabel);
        marcaValueLabel.getStyleClass().add("detalhe-valor");
        marcaValueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        dadosNotaBox.getChildren().add(marcaBox);

        // Status de Arquivamento
        Label lblArquivada = new Label("Arquivada:");
        lblArquivada.setStyle("-fx-text-fill: #BDBDBD; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox arquivadaBox = new HBox(5, lblArquivada, arquivadaValueLabel);
        arquivadaValueLabel.getStyleClass().add("detalhe-valor");
        arquivadaValueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        dadosNotaBox.getChildren().add(arquivadaBox);

        // Data de Arquivamento
        Label lblDataArquivamento = new Label("Data de Arquivamento:");
        lblDataArquivamento.setStyle("-fx-text-fill: #BDBDBD; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox dataArquivamentoBox = new HBox(5, lblDataArquivamento, dataArquivamentoValueLabel);
        dataArquivamentoValueLabel.getStyleClass().add("detalhe-valor");
        dataArquivamentoValueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        dadosNotaBox.getChildren().add(dataArquivamentoBox);

        for (Node node : dadosNotaBox.getChildren()) {
            if (node instanceof HBox) {
                ((Label) ((HBox) node).getChildren().get(0)).getStyleClass().add("detalhe-titulo");
            }
        }

        // --- SUBTITULO FATURAS ---
        Label sub2 = new Label("Faturas Associadas");
        sub2.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));
        sub2.setTextFill(Color.web("#7890A8"));
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #7890A8;");

        // --- SEÇÃO FATURAS ---
        vencimentosColumn.setPadding(new Insets(10));
        vencimentosColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");
        valoresColumn.setPadding(new Insets(10));
        valoresColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");
        statusColumn.setPadding(new Insets(10));
        statusColumn.setStyle("-fx-background-color: #7890A8; -fx-background-radius: 5;");

        ScrollPane spVenc = new ScrollPane(vencimentosColumn);
        spVenc.setFitToWidth(true);
        spVenc.setPrefSize(180, 500);
        ScrollPane spVal = new ScrollPane(valoresColumn);
        spVal.setFitToWidth(true);
        spVal.setPrefSize(150, 500);
        ScrollPane spStatus = new ScrollPane(statusColumn);
        spStatus.setFitToWidth(true);
        spStatus.setPrefSize(150, 500);

        spVal.vvalueProperty().bindBidirectional(spVenc.vvalueProperty());
        spStatus.vvalueProperty().bindBidirectional(spVenc.vvalueProperty());

        HBox faturasSection = new HBox(25, spVenc, spVal, spStatus);
        faturasSection.setAlignment(Pos.TOP_LEFT);

        // --- BOTÕES: Fechar e Editar ---
        btnFechar.getStyleClass().add("modal-button");
        ImageView backIcon = new ImageView(Objects.requireNonNull(getClass().getResource("/icons/back.png")).toExternalForm());
        backIcon.setFitHeight(18);
        backIcon.setFitWidth(18);
        btnFechar.setGraphic(backIcon);
        btnFechar.setFocusTraversable(false);
        btnFechar.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));

        btnEditar.getStyleClass().add("modal-button");
        ImageView editIcon = new ImageView(Objects.requireNonNull(getClass().getResource("/icons/edit.png")).toExternalForm());
        editIcon.setFitHeight(20);
        editIcon.setFitWidth(20);
        btnEditar.setGraphic(editIcon);
        btnEditar.setFocusTraversable(false);
        btnEditar.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));

        HBox botoes = new HBox(10, btnFechar, btnEditar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(15, 0, 0, 0));

        // --- AGRUPA TUDO ———
        VBox container = new VBox(15, headerBox, dadosNotaBox, sub2, sep2, faturasSection, botoes);
        container.setAlignment(Pos.TOP_LEFT);
        root.setCenter(container);
    }

    // MÉTODO: Para preencher os dados da View
    public void preencherDados(NotaFiscal notaFiscal, ObservableList<Fatura> faturas) {
        // Popula os Labels da Nota Fiscal
        numeroNotaValueLabel.setText(notaFiscal.getNumeroNota());
        dataEmissaoValueLabel.setText(notaFiscal.getDataEmissao().format(FORMATTER));
        marcaValueLabel.setText(notaFiscal.getMarca());
        arquivadaValueLabel.setText(notaFiscal.isArquivada() ? "Sim" : "Não");
        if (notaFiscal.isArquivada() && notaFiscal.getDataArquivamento() != null) {
            dataArquivamentoValueLabel.setText(notaFiscal.getDataArquivamento().format(FORMATTER));
        } else {
            dataArquivamentoValueLabel.setText("N/A");
        }

        // Limpa e preenche as colunas de faturas
        vencimentosColumn.getChildren().clear();
        valoresColumn.getChildren().clear();
        statusColumn.getChildren().clear();

        // Adiciona cabeçalhos as colunas de fatura
        vencimentosColumn.getChildren().add(criarColunaHeader("Fatura"));
        valoresColumn.getChildren().add(criarColunaHeader("Valor"));
        statusColumn.getChildren().add(criarColunaHeader("Status"));

        for (Fatura fatura : faturas) {
            // Número da Fatura
            Label faturaNumLabel = new Label("Fatura " + fatura.getNumeroFatura());
            faturaNumLabel.getStyleClass().add("detalhe-valor-fatura");
            faturaNumLabel.setStyle("-fx-font-size: 16px;"); // AUMENTADO E GARANTIDO
            vencimentosColumn.getChildren().add(faturaNumLabel);

            // Valor
            Label valorLabel = new Label(CURRENCY_FORMAT.format(fatura.getValor()));
            valorLabel.getStyleClass().add("detalhe-valor-fatura");
            valorLabel.setStyle("-fx-font-size: 16px;"); // AUMENTADO E GARANTIDO
            valoresColumn.getChildren().add(valorLabel);

            // Status
            Label statusLabel = new Label(fatura.getStatus());
            statusLabel.getStyleClass().add("detalhe-valor-fatura");
            statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"); // AUMENTADO E NEGITO
            if ("Vencida".equalsIgnoreCase(fatura.getStatus())) {
                statusLabel.setTextFill(Color.web("#f0a818")); // Laranja/Amarelo
            } else if ("Emitida".equalsIgnoreCase(fatura.getStatus())) {
                statusLabel.setTextFill(Color.web("#66BB6A")); // Verde mais claro
            } else {
                statusLabel.setTextFill(Color.WHITE);
            }
            statusColumn.getChildren().add(statusLabel);
        }
    }

    // Metodo auxiliar para criar cabeçalhos de coluna para faturas
    private Label criarColunaHeader(String texto) {
        Label header = new Label(texto);
        header.setFont(Font.font("Poppins", FontWeight.BOLD, 16)); // AUMENTADO PARA 16
        header.setTextFill(Color.WHITE);
        header.setPadding(new Insets(10, 0, 10, 0)); // Aumentar padding
        header.getStyleClass().add("detalhe-header-fatura");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        return header;
    }

    // === GETTERS ===
    public BorderPane getRoot() {
        return root;
    }

    public Button getBtnFechar() {
        return btnFechar;
    }

    public Button getBtnEditar() {
        return btnEditar;
    }
}
