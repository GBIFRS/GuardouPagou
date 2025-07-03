package com.GuardouPagou.views;

import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO;
import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.Marca;
import com.GuardouPagou.models.NotaFiscal;
import com.GuardouPagou.controllers.NotaFaturaController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableRow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.HBox;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.Window;

public class MainView {

    private static final Logger LOGGER = Logger.getLogger(MainView.class.getName());

    private MenuButton btnFiltrar;
    private RadioMenuItem miFiltrarPeriodo;
    private RadioMenuItem miFiltrarMarca;
    private BorderPane root;
    private Button btnListarFaturas, btnListarMarcas, btnArquivadas;
    private Button btnNovaFatura, btnNovaMarca, btnSalvarEmail;
    private Label labelText;
    private TextField emailField;
    private DatePicker dpDataInicio, dpDataFim;
    private Button btnAplicarFiltro, btnRemoverFiltro;
    private VBox filtroContainer;
    private ComboBox<Marca> cbFiltroMarca;
    private ObservableList<Marca> cacheMarcas;
    private final Popup filtroPopup = new Popup();
    private LocalDate periodoFilterStart;
    private LocalDate periodoFilterEnd;
    private final Set<Marca> marcaFilters = new HashSet<>();
    private HBox filterTokens;
    private TableView<Fatura> tabelaFaturas;
    private java.util.function.Consumer<Fatura> notaDoubleClickHandler;

    public MainView() {
        criarUI();
        try {
            Fatura faturaMaisProxima = new FaturaDAO().obterFaturaMaisProximaDoVencimentoNaoEmitida();
            ObservableList<Fatura> faturasParaExibir = FXCollections.observableArrayList();
            if (faturaMaisProxima != null) {
                faturasParaExibir.add(faturaMaisProxima);
            }
            root.setCenter(criarViewFaturas(faturasParaExibir));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar faturas iniciais na MainView.", ex);
            setConteudoPrincipal(new Label("Erro ao carregar faturas iniciais: " + ex.getMessage()));
        }
    }

    public BorderPane getRoot() {
        return this.root;
    }

    public void setConteudoPrincipal(Node novoConteudo) {
        root.setCenter(novoConteudo);
    }

    public void setNotaDoubleClickHandler(java.util.function.Consumer<Fatura> handler) {
        this.notaDoubleClickHandler = handler;
    }

    private void atualizarListaFaturas() {
        atualizarListaComFiltros();
    }

    private void criarUI() {
        root = new BorderPane();
        root.getStyleClass().add("main-root");
        root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        root.setLeft(criarMenuLateral());

        labelText = new Label("Bem-vindo ao Guardou-Pagou");
        labelText.getStyleClass().add("h5");
        labelText.setTextFill(Color.web("#323437"));

        StackPane painelCentral = new StackPane(labelText);
        painelCentral.setStyle("-fx-background-color: #BDBDBD;");
        root.setCenter(painelCentral);
    }

    @SuppressWarnings("unused")
    private Button criarBotao(String texto, String iconPath, String cssClass) {
        Button btn = new Button(" " + texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().addAll("menu-button", cssClass);

        if (iconPath != null) {
            try {
                ImageView icon = new ImageView(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm());
                icon.setPreserveRatio(true);
                btn.setGraphic(icon);
            } catch (Exception e) {
                System.err.println("Erro ao carregar icone: " + iconPath);
            }
        }

        btn.setOnMouseClicked(e -> btn.getParent().requestFocus());

        return btn;
    }

    private VBox criarLogo() {
        VBox logoContainer = new VBox();
        logoContainer.setPadding(new Insets(10, 0, 5, 10));
        logoContainer.setSpacing(0);
        logoContainer.setAlignment(Pos.TOP_LEFT);

        try {
            Image logoImage = new Image(Objects.requireNonNull(getClass().getResource("/icons/G-Clock_home.png")).toExternalForm());
            ImageView logoView = new ImageView(logoImage);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            logoView.setCache(true);

            logoContainer.getChildren().add(logoView);
        } catch (Exception e) {
            Label fallback = new Label("LOGO");
            fallback.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
            fallback.setTextFill(Color.web("#F0A818"));
            logoContainer.getChildren().add(fallback);
        }

        return logoContainer;
    }

    private Region criarSeparadorLogo() {
        Region linha = new Region();
        linha.setPrefHeight(2);
        linha.setMaxWidth(Double.MAX_VALUE);
        linha.getStyleClass().add("logo-divider");
        return linha;
    }

    private Label criarTitulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("menu-subtitle-light");
        return label;
    }

    private VBox criarEspacoFlexivel() {
        VBox espaco = new VBox();
        VBox.setVgrow(espaco, Priority.ALWAYS);
        return espaco;
    }

    private VBox criarMenuLateral() {
        VBox menuLateral = new VBox();
        menuLateral.getStyleClass().add("menu-lateral-root");

        btnListarFaturas = criarBotao("Listar Faturas", "/icons/list.png", "botao-listagem");
        btnListarMarcas = criarBotao("Listar Marcas", "/icons/list.png", "botao-listagem");
        btnArquivadas = criarBotao("Arquivadas", "/icons/archive.png", "botao-listagem");
        btnNovaFatura = criarBotao("Cadastrar Faturas", "/icons/plus.png", "botao-cadastro");
        btnNovaMarca = criarBotao("Cadastrar Marca", "/icons/plus.png", "botao-cadastro");

        VBox secaoListagens = new VBox(criarTitulo("Principais Listagens"), btnListarFaturas, btnListarMarcas, btnArquivadas);
        secaoListagens.getStyleClass().add("menu-section");

        VBox secaoCadastros = new VBox(criarTitulo("Novos Cadastros"), btnNovaFatura, btnNovaMarca);
        secaoCadastros.getStyleClass().add("menu-section");

        btnSalvarEmail = criarBotao("E-mails de Alerta", "/icons/campaing.png", "botao-listagem");
        btnSalvarEmail.setPrefWidth(220);
        VBox secaoOutros = new VBox(criarTitulo("Outros"), btnSalvarEmail);
        secaoOutros.getStyleClass().add("menu-section");

        menuLateral.getChildren().addAll(criarLogo(), criarSeparadorLogo(), secaoListagens, secaoCadastros, secaoOutros, criarEspacoFlexivel());

        for (Button b : List.of(btnListarFaturas, btnListarMarcas, btnArquivadas, btnNovaFatura, btnNovaMarca, btnSalvarEmail)) {
            b.setFocusTraversable(false);
        }

        return menuLateral;
    }

    public Node criarViewFaturas(ObservableList<Fatura> faturas) {
        VBox container = new VBox(18);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #BDBDBD;");

        Label titulo = new Label("LISTAGEM DE FATURAS");
        titulo.getStyleClass().add("h5");
        titulo.setTextFill(Color.web("#F0A818"));

        if (btnFiltrar == null) {
            ImageView filterIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/filter_list.png"))));
            filterIcon.setFitHeight(22);
            filterIcon.setPreserveRatio(true);
            btnFiltrar = new MenuButton("Filtrar", filterIcon, miFiltrarPeriodo, miFiltrarMarca);
            btnFiltrar.getStyleClass().addAll("menu-button", "botao-listagem", "btn-filtrar");
            btnFiltrar.setContentDisplay(ContentDisplay.LEFT);
            btnFiltrar.setGraphicTextGap(10);
        }
        configurarFiltroMenuButton(btnFiltrar);

        HBox toolbar = new HBox(12, btnFiltrar, filterTokens, new HBox(), new Button("Atualizar"));
        ((Button) toolbar.getChildren().get(toolbar.getChildren().size() - 1)).setOnAction(e -> atualizarListaFaturas());

        toolbar.setAlignment(Pos.CENTER_LEFT);

        this.tabelaFaturas = new TableView<>();
        ViewUtils.aplicarEstiloPadrao(this.tabelaFaturas);
        this.tabelaFaturas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.tabelaFaturas.setRowFactory(tv -> {
            TableRow<Fatura> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty() && notaDoubleClickHandler != null) {
                    notaDoubleClickHandler.accept(row.getItem());
                }
            });
            return row;
        });
        VBox.setVgrow(this.tabelaFaturas, Priority.ALWAYS);

        TableColumn<Fatura, String> colunaNumeroNota = criarColunaNumeroNotaFatura();
        TableColumn<Fatura, Integer> colunaOrdem = criarColunaOrdemFatura();
        TableColumn<Fatura, LocalDate> colunaVencimento = criarColunaVencimentoFatura();
        TableColumn<Fatura, String> colunaMarca = criarColunaMarcaFatura();
        TableColumn<Fatura, String> colunaStatus = criarColunaStatusFatura();
        TableColumn<Fatura, Void> colunaAcoes = criarColunaAcoesFatura(); // BOTÃO EDITAR AQUI

        this.tabelaFaturas.getColumns().setAll(List.of(colunaNumeroNota, colunaOrdem, colunaVencimento, colunaMarca, colunaStatus, colunaAcoes));
        this.tabelaFaturas.setItems(faturas);

        container.getChildren().addAll(titulo, toolbar, this.tabelaFaturas);
        return container;
    }

    private void configurarFiltroMenuButton(MenuButton btnFiltrar) {
        if (miFiltrarPeriodo == null) {
            miFiltrarPeriodo = new RadioMenuItem("Filtrar por Período");
            miFiltrarMarca = new RadioMenuItem("Filtrar por Marca");
            ToggleGroup filtroToggleGroup = new ToggleGroup();
            miFiltrarPeriodo.setToggleGroup(filtroToggleGroup);
            miFiltrarMarca.setToggleGroup(filtroToggleGroup);
            btnFiltrar.getItems().setAll(miFiltrarPeriodo, miFiltrarMarca);
        }

        if (dpDataInicio == null) {
            dpDataInicio = new DatePicker();
            dpDataFim = new DatePicker();
            btnAplicarFiltro = new Button();
            btnRemoverFiltro = new Button();
            cbFiltroMarca = new ComboBox<>();
            filterTokens = new HBox(8);

            Label lblDataInicio = new Label("Data Inicial");
            lblDataInicio.getStyleClass().add("field-subtitle");
            VBox dataInicioBox = new VBox(6, lblDataInicio, dpDataInicio);
            dataInicioBox.getStyleClass().add("pill-field");

            Label lblDataFim = new Label("Data Final");
            lblDataFim.getStyleClass().add("field-subtitle");
            VBox dataFimBox = new VBox(6, lblDataFim, dpDataFim);
            dataFimBox.getStyleClass().add("pill-field");

            btnAplicarFiltro.getStyleClass().addAll("modal-button", "btn-aplicar");
            ImageView checkIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/check.png"))));
            checkIcon.setPreserveRatio(true);
            btnAplicarFiltro.setGraphic(checkIcon);
            btnAplicarFiltro.setFocusTraversable(false);
            btnAplicarFiltro.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btnAplicarFiltro.setOnAction(e -> {
                aplicarFiltroPeriodo();
                filtroPopup.hide();
            });

            btnRemoverFiltro.getStyleClass().addAll("modal-button", "btn-cancelar");
            ImageView cancelIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cancel.png"))));
            cancelIcon.setFitHeight(20);
            cancelIcon.setPreserveRatio(true);
            btnRemoverFiltro.setGraphic(cancelIcon);
            btnRemoverFiltro.setFocusTraversable(false);
            btnRemoverFiltro.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            btnRemoverFiltro.setOnAction(e -> {
                periodoFilterStart = periodoFilterEnd = null;
                filterTokens.getChildren().removeIf(n -> "periodo".equals(n.getUserData()));
                atualizarListaComFiltros();
                filtroPopup.hide();
            });

            VBox dateVBox = new VBox(10, dataInicioBox, dataFimBox);
            dateVBox.setAlignment(Pos.CENTER_LEFT);
            VBox buttonVBox = new VBox(10, btnAplicarFiltro, btnRemoverFiltro);
            buttonVBox.setAlignment(Pos.CENTER_LEFT);
            HBox contentHBox = new HBox(20, dateVBox, buttonVBox);
            contentHBox.setAlignment(Pos.CENTER);
            VBox periodContent = new VBox(contentHBox);
            periodContent.getStyleClass().addAll("painel-filtros", "painel-filtros-canto-quadrado");
            periodContent.setPadding(new Insets(15));

            Label lblFiltrarMarca = new Label("Filtrar por Marca:");
            lblFiltrarMarca.getStyleClass().add("field-subtitle");
            cbFiltroMarca = new ComboBox<>();
            cbFiltroMarca.setPromptText("Selecione uma marca");
            cbFiltroMarca.setPrefWidth(200);
            try {
                cbFiltroMarca.setItems(new MarcaDAO().listarMarcas());
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Falha ao carregar a lista de marcas para o ComboBox de filtro.", ex);
            }
            cbFiltroMarca.setOnAction(e -> {
                aplicarFiltroMarca();
                filtroPopup.hide();
            });

            VBox marcaBox = new VBox(6, lblFiltrarMarca, cbFiltroMarca);
            marcaBox.getStyleClass().add("pill-field");
            VBox marcaContent = new VBox(marcaBox);
            marcaContent.getStyleClass().addAll("painel-filtros", "painel-filtros-canto-quadrado");
            marcaContent.setPadding(new Insets(15));

            filtroPopup.setAutoHide(true);
            filtroPopup.setHideOnEscape(true);
            filtroPopup.setOnShowing(evt
                    -> btnFiltrar.getStyleClass().add("filter-open")
            );
            filtroPopup.setOnHiding(evt
                    -> btnFiltrar.getStyleClass().remove("filter-open")
            );

            miFiltrarPeriodo.setOnAction(e -> {
                filtroPopup.getContent().setAll(periodContent);
                Bounds b = btnFiltrar.localToScreen(btnFiltrar.getBoundsInLocal());
                filtroPopup.show(btnFiltrar.getScene().getWindow(), b.getMinX(), b.getMaxY());
            });
            miFiltrarMarca.setOnAction(e -> {
                filtroPopup.getContent().setAll(marcaContent);
                Bounds b = btnFiltrar.localToScreen(btnFiltrar.getBoundsInLocal());
                filtroPopup.show(btnFiltrar.getScene().getWindow(), b.getMinX(), b.getMaxY());
            });
        }
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, Integer> criarColunaIdFatura() {
        TableColumn<Fatura, Integer> coluna = new TableColumn<>("ID");
        coluna.setCellValueFactory(new PropertyValueFactory<>("id"));
        coluna.setPrefWidth(80);
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(id.toString());
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return coluna;
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, String> criarColunaNumeroNotaFatura() {
        TableColumn<Fatura, String> coluna = new TableColumn<>("Nº NOTA FISCAL");
        coluna.setCellValueFactory(new PropertyValueFactory<>("numeroNota"));
        coluna.setPrefWidth(150);
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String numeroNota, boolean empty) {
                super.updateItem(numeroNota, empty);
                if (empty || numeroNota == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(numeroNota);
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return coluna;
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, Integer> criarColunaOrdemFatura() {
        TableColumn<Fatura, Integer> coluna = new TableColumn<>("ORDEM DA FATURA");
        coluna.setCellValueFactory(new PropertyValueFactory<>("numeroFatura"));
        coluna.setPrefWidth(120);
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer ordem, boolean empty) {
                super.updateItem(ordem, empty);
                if (empty || ordem == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(ordem.toString());
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return coluna;
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, LocalDate> criarColunaVencimentoFatura() {
        TableColumn<Fatura, LocalDate> coluna = new TableColumn<>("DATA DE VENCIMENTO");
        coluna.setCellValueFactory(new PropertyValueFactory<>("vencimento"));
        coluna.setPrefWidth(120);
        coluna.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate venc, boolean empty) {
                super.updateItem(venc, empty);
                setText((empty || venc == null) ? null : venc.format(fmt));
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
            }
        });
        return coluna;
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, String> criarColunaMarcaFatura() {
        TableColumn<Fatura, String> coluna = new TableColumn<>("MARCA");
        coluna.setCellValueFactory(new PropertyValueFactory<>("marca"));
        coluna.setPrefWidth(150);
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String marca, boolean empty) {
                super.updateItem(marca, empty);
                if (empty || marca == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(marca);
                    String cor = getTableView().getItems().get(getIndex()).getMarcaColor();
                    if (cor != null && cor.matches("#[0-9A-Fa-f]{6}")) {
                        setTextFill(Color.web(cor));
                    } else {
                        setTextFill(Color.WHITE);
                    }
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return coluna;
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, String> criarColunaStatusFatura() {
        TableColumn<Fatura, String> coluna = new TableColumn<>("STATUS DA FATURA");
        coluna.setCellValueFactory(new PropertyValueFactory<>("status"));
        coluna.setPrefWidth(120);
        coluna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setTextFill("Vencida".equalsIgnoreCase(status) ? Color.web("#f0a818") : Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return coluna;
    }

    @SuppressWarnings("unused")
    private TableColumn<Fatura, Void> criarColunaAcoesFatura() {
        TableColumn<Fatura, Void> coluna = new TableColumn<>("Ações");
        coluna.setPrefWidth(200);
        coluna.setCellFactory(col -> new TableCell<>() {
            private final Button btnEmitida = new Button("Emitida");
            private final Button btnEditar = new Button("Editar");

            {
                btnEmitida.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px; -fx-background-radius: 3;");
                btnEmitida.setOnAction(evt -> {
                    Fatura f = getTableView().getItems().get(getIndex());
                    marcarFaturaComoEmitida(f);
                });

                btnEditar.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px; -fx-background-radius: 3;");
                btnEditar.setOnAction(evt -> {
                    Fatura f = getTableView().getItems().get(getIndex());
                    abrirModalEdicao(f.getNotaFiscalId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Fatura fatura = getTableView().getItems().get(getIndex());

                    HBox buttonContainer = new HBox(5);
                    buttonContainer.setAlignment(Pos.CENTER);

                    if (!"Emitida".equalsIgnoreCase(fatura.getStatus()) && !"Vencida".equalsIgnoreCase(fatura.getStatus())) {
                        buttonContainer.getChildren().add(btnEmitida);
                    }

                    try {
                        NotaFiscalDAO notaFiscalDAO = new NotaFiscalDAO();
                        NotaFiscal nfAssociada = notaFiscalDAO.obterNotaFiscalPorId(fatura.getNotaFiscalId());
                        if (nfAssociada != null && !nfAssociada.isArquivada()) {
                            buttonContainer.getChildren().add(btnEditar);
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Erro ao verificar status de arquivamento para edicao", e);
                    }

                    setGraphic(buttonContainer);
                }
            }
        });
        return coluna;
    }

    // MÉTODO criarViewMarcas - CORRIGIDO E ORIGINAL
    @SuppressWarnings("unused")
    public Node criarViewMarcas(ObservableList<Marca> marcas) {
        TableView<Marca> tabela = new TableView<>();
        ViewUtils.aplicarEstiloPadrao(tabela);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Marca, Integer> colunaId = criarColunaIdMarca();
        TableColumn<Marca, String> colunaNome = criarColunaNomeMarca();
        TableColumn<Marca, String> colunaDescricao = criarColunaDescricaoMarca();

        tabela.getColumns().setAll(List.of(colunaId, colunaNome, colunaDescricao));
        tabela.setItems(marcas);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #BDBDBD;");

        Label titulo = new Label("LISTAGEM DE MARCAS");
        titulo.setStyle("-fx-text-fill: #F0A818; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox toolbar = new HBox(10);
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle("-fx-background-color: #C88200; -fx-text-fill: #000000; -fx-font-weight: bold;");
        btnAtualizar.setOnAction(e -> atualizarListaMarcas());

        toolbar.getChildren().add(btnAtualizar);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(titulo, toolbar, tabela);
        return container;
    }

    @SuppressWarnings("unused")
    private TableColumn<Marca, Integer> criarColunaIdMarca() {
        TableColumn<Marca, Integer> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaId.setPrefWidth(80);
        colunaId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(id.toString());
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return colunaId;
    }

    @SuppressWarnings("unused")
    private TableColumn<Marca, String> criarColunaNomeMarca() {
        TableColumn<Marca, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setPrefWidth(200);
        colunaNome.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String nome, boolean empty) {
                super.updateItem(nome, empty);
                if (empty || nome == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(nome);
                    String cor = getTableView().getItems().get(getIndex()).getCor();
                    if (cor != null && cor.matches("#[0-9A-Fa-f]{6}")) {
                        setTextFill(Color.web(cor));
                    } else {
                        setTextFill(Color.WHITE);
                    }
                    setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return colunaNome;
    }

    @SuppressWarnings("unused")
    private TableColumn<Marca, String> criarColunaDescricaoMarca() {
        TableColumn<Marca, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colunaDescricao.setPrefWidth(250);
        colunaDescricao.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String desc, boolean empty) {
                super.updateItem(desc, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (desc == null || desc.isBlank()) {
                    setText("Nenhuma descrição adicionada");
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-alignment: CENTER-LEFT;");
                } else {
                    setText(desc);
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: transparent; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        return colunaDescricao;
    }

    private void mostrarListaFaturas(ObservableList<Fatura> faturas) {
        if (this.tabelaFaturas != null) {
            this.tabelaFaturas.setItems(faturas);
        }
    }

    private void marcarFaturaComoEmitida(Fatura fatura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmacao");
        alert.setHeaderText("Marcar Fatura como Emitida");
        alert.setContentText("Tem certeza que deseja marcar a fatura Nº " + fatura.getNumeroFatura() + " da Nota " + fatura.getNumeroNota() + " como emitida?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FaturaDAO faturaDAO = new FaturaDAO();

                boolean faturaMarcada = faturaDAO.marcarFaturaIndividualComoEmitida(fatura.getId());

                if (faturaMarcada) {
                    boolean todasEmitidas = faturaDAO.todasFaturasDaNotaEmitidas(fatura.getNotaFiscalId());

                    if (todasEmitidas) {
                        boolean notaFiscalArquivada = new NotaFiscalDAO().marcarComoArquivada(
                                fatura.getNotaFiscalId(),
                                LocalDate.now().plusDays(3)
                        );
                        if (notaFiscalArquivada) {
                            atualizarListaFaturas();
                            if (getBtnArquivadas() != null) {
                                getBtnArquivadas().fire();
                            }
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Erro ao arquivar a Nota Fiscal apos todas as faturas serem emitidas.");
                            errorAlert.showAndWait();
                        }
                    } else {
                        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                        infoAlert.setTitle("Fatura Emitida");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText("Fatura Nº " + fatura.getNumeroFatura() + " marcada como emitida. Aguardando outras faturas da Nota " + fatura.getNumeroNota() + " para arquivamento.");
                        infoAlert.showAndWait();
                        atualizarListaFaturas();
                    }
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erro");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Não foi possível marcar a fatura como emitida.");
                    errorAlert.showAndWait();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erro ao processar emissão da fatura.", e);

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Erro ao processar emissão da fatura: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void abrirModalEdicao(int notaFiscalId) {
        try {
            NotaFiscalDAO notaFiscalDAO = new NotaFiscalDAO();
            FaturaDAO faturaDAO = new FaturaDAO();

            NotaFiscal notaParaEditar = notaFiscalDAO.obterNotaFiscalPorId(notaFiscalId);
            ObservableList<Fatura> faturasParaEditar = FXCollections.observableArrayList(faturaDAO.listarFaturasDaNota(notaFiscalId));

            if (notaParaEditar == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Nota Fiscal nao encontrada para edicao.");
                alert.showAndWait();
                return;
            }

            if (notaParaEditar.isArquivada()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atencao");
                alert.setHeaderText(null);
                alert.setContentText("Nao e possivel editar uma Nota Fiscal arquivada.");
                alert.showAndWait();
                return;
            }

            Stage modalStage = new Stage();
            Window owner = root.getScene().getWindow();
            modalStage.initOwner(owner);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.setTitle("Editar Nota Fiscal e Faturas");

            NotaFaturaView edicaoView = new NotaFaturaView(notaParaEditar, faturasParaEditar);
            new NotaFaturaController(edicaoView, notaParaEditar, faturasParaEditar);

            Scene scene = new Scene(edicaoView.getRoot(), 850, 650);
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            // Adicionar icone a janela modal (comentar se nao tiver o arquivo edit.png)
            // modalStage.getIcons().add(
            //     new Image(
            //         Objects.requireNonNull(
            //             getClass().getResourceAsStream("/icons/edit.png")
            //         )
            //     )
            // );
            modalStage.setOnHidden(e -> atualizarListaFaturas());

            modalStage.showAndWait();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar dados para edicao", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar dados para edicao: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showAlert(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void atualizarListaMarcas() {
        try {
            ObservableList<Marca> marcas = new MarcaDAO().listarMarcas();
            criarViewMarcas(marcas);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar a lista de marcas do banco de dados.", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar marcas: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @SuppressWarnings("unused")
    private void aplicarFiltroPeriodo() {
        LocalDate inicio = dpDataInicio.getValue();
        LocalDate fim = dpDataFim.getValue();
        if (inicio != null && fim != null) {
            periodoFilterStart = inicio;
            periodoFilterEnd = fim;

            atualizarListaComFiltros();

            filterTokens.getChildren().removeIf(n -> "periodo".equals(n.getUserData()));

            String txt = inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " – "
                    + fim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            Button periodoBtn = new Button(txt);
            periodoBtn.setUserData("periodo");
            periodoBtn.getStyleClass().add("filter-token");

            ImageView cancelIcon = new ImageView(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cancel.png")))
            );
            cancelIcon.setFitHeight(20);
            cancelIcon.setPreserveRatio(true);
            periodoBtn.setGraphic(cancelIcon);
            periodoBtn.setContentDisplay(ContentDisplay.RIGHT);

            periodoBtn.setOnAction(ev -> {
                periodoFilterStart = periodoFilterEnd = null;
                filterTokens.getChildren().remove(periodoBtn);
                atualizarListaComFiltros();
            });

            filterTokens.getChildren().add(periodoBtn);
        }
        filtroPopup.hide();
    }

    private void atualizarListaComFiltros() {
        try {
            ObservableList<Fatura> resultado;
            boolean filtraPeriodo = periodoFilterStart != null && periodoFilterEnd != null;
            boolean filtraMarca = !marcaFilters.isEmpty();

            if (filtraPeriodo && filtraMarca) {
                List<String> nomes = marcaFilters.stream()
                        .map(Marca::getNome)
                        .toList();
                resultado = new FaturaDAO().listarFaturasPorPeriodoEMarcas(
                        periodoFilterStart,
                        periodoFilterEnd,
                        nomes
                );
            } else if (filtraPeriodo) {
                resultado = new FaturaDAO().listarFaturasPorPeriodo(
                        periodoFilterStart,
                        periodoFilterEnd
                );
            } else if (filtraMarca) {
                List<String> nomes = marcaFilters.stream()
                        .map(Marca::getNome)
                        .toList();
                resultado = new FaturaDAO().listarFaturasPorMarcas(nomes);
            } else {
                Fatura faturaMaisProxima = new FaturaDAO().obterFaturaMaisProximaDoVencimentoNaoEmitida();
                resultado = FXCollections.observableArrayList();
                if (faturaMaisProxima != null) {
                    resultado.add(faturaMaisProxima);
                }
            }

            mostrarListaFaturas(resultado);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao aplicar filtros de faturas", ex);
            showAlert("Erro ao filtrar faturas", ex.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void aplicarFiltroMarca() {
        Marca sel = cbFiltroMarca.getValue();
        if (sel != null && marcaFilters.add(sel)) {
            atualizarListaComFiltros();

            Button marcaBtn = new Button(sel.getNome());
            marcaBtn.setUserData(sel);
            marcaBtn.getStyleClass().add("filter-token");
            ImageView cancelIcon = new ImageView(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cancel.png")))
            );
            cancelIcon.setFitHeight(20);
            cancelIcon.setPreserveRatio(true);
            marcaBtn.setGraphic(cancelIcon);
            marcaBtn.setContentDisplay(ContentDisplay.RIGHT);

            marcaBtn.setOnAction(ev -> {
                marcaFilters.remove(sel);
                filterTokens.getChildren().remove(marcaBtn);
                atualizarListaComFiltros();
            });

            filterTokens.getChildren().add(marcaBtn);
        }
        filtroPopup.hide();
    }

    private void mostrarTodasFaturas() {
        try {
            ObservableList<Fatura> faturas = new FaturaDAO().listarFaturas(false);
            Node viewFaturas = criarViewFaturas(faturas);
            setConteudoPrincipal(viewFaturas);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar todas as faturas.", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao carregar todas as faturas: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    public Button getBtnListarFaturas() {
        return btnListarFaturas;
    }

    public Button getBtnListarMarcas() {
        return btnListarMarcas;
    }

    public Button getBtnArquivadas() {
        return btnArquivadas;
    }

    public Button getBtnNovaFatura() {
        return btnNovaFatura;
    }

    public Button getBtnNovaMarca() {
        return btnNovaMarca;
    }

    public Button getBtnSalvarEmail() {
        return btnSalvarEmail;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public Label getConteudoLabel() {
        return labelText;
    }
}
