package com.GuardouPagou.views;

import com.GuardouPagou.controllers.MarcaController;
import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO;
import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.Marca;
import com.GuardouPagou.models.NotaFiscal; // Importe NotaFiscal
import com.GuardouPagou.controllers.NotaFaturaController; // Importe NotaFaturaController
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality; // Importe Modality
import javafx.stage.Stage;    // Importe Stage
import javafx.stage.Window;   // Importe Window
import javafx.scene.Scene;      
import javafx.stage.Stage;    
import javafx.stage.Modality;   
import javafx.stage.Window;     
import java.util.Objects;       

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainView {

    private final RadioButton rbFiltraPeriodo = new RadioButton("Filtrar por Período");
    private final RadioButton rbFiltraMarca = new RadioButton("Filtrar por Marca");
    private final ToggleGroup filtroToggleGroup;
    private BorderPane root;
    private Button btnListarFaturas, btnListarMarcas, btnArquivadas;
    private Button btnNovaFatura, btnNovaMarca, btnSalvarEmail;
    private Label labelText;
    private TextField emailField;
    private DatePicker dpFiltroPeriodo;
    private ComboBox<Marca> cbFiltroMarca;
    private VBox filtroContainer;

    public MainView() {
        criarUI();
        filtroToggleGroup = new ToggleGroup();
        try {
            Fatura faturaMaisProxima = new FaturaDAO().obterFaturaMaisProximaDoVencimentoNaoEmitida();
            ObservableList<Fatura> faturasParaExibir = FXCollections.observableArrayList();

            if (faturaMaisProxima != null) {
                faturasParaExibir.add(faturaMaisProxima);
            }
            mostrarListaFaturas(faturasParaExibir);

        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar faturas na inicialização: " + ex.getMessage());
            alert.showAndWait();
            root.setCenter(labelText);
        }
    }

    public BorderPane getRoot() {
        return this.root;
    }

    private void atualizarListaFaturas() {
        try {
            ObservableList<Fatura> faturasParaExibir = FXCollections.observableArrayList();
            FaturaDAO faturaDAO = new FaturaDAO();

            if (rbFiltraPeriodo.isSelected()) {
                LocalDate dataSelecionada = dpFiltroPeriodo.getValue();
                if (dataSelecionada != null) {
                    System.out.println("Atualizando lista por período: " + dataSelecionada);
                    faturasParaExibir.setAll(faturaDAO.listarFaturasPorPeriodo(dataSelecionada));
                } else {
                    Fatura faturaMaisProxima = faturaDAO.obterFaturaMaisProximaDoVencimentoNaoEmitida();
                    if (faturaMaisProxima != null) {
                        faturasParaExibir.add(faturaMaisProxima);
                    }
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Atencao");
                    alert.setHeaderText(null);
                    alert.setContentText("Selecione uma data para filtrar por periodo.");
                    alert.showAndWait();
                }

            } else if (rbFiltraMarca.isSelected()) {
                Marca selectedMarcaObject = cbFiltroMarca.getValue();
                String marcaSelecionada = (selectedMarcaObject != null) ? selectedMarcaObject.getNome() : null;

                if (marcaSelecionada != null && !marcaSelecionada.isEmpty()) {
                    System.out.println("Atualizando lista por marca: " + marcaSelecionada);
                    faturasParaExibir.setAll(faturaDAO.listarFaturasPorMarca(marcaSelecionada));
                } else {
                    Fatura faturaMaisProxima = faturaDAO.obterFaturaMaisProximaDoVencimentoNaoEmitida();
                    if (faturaMaisProxima != null) {
                        faturasParaExibir.add(faturaMaisProxima);
                    }
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Atenção");
                    alert.setHeaderText(null);
                    alert.setContentText("Selecione uma marca para filtrar.");
                    alert.showAndWait();
                }
            } else {
                Fatura faturaMaisProxima = faturaDAO.obterFaturaMaisProximaDoVencimentoNaoEmitida();
                if (faturaMaisProxima != null) {
                    faturasParaExibir.add(faturaMaisProxima);
                }
            }
            mostrarListaFaturas(faturasParaExibir);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar faturas: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void criarUI() {
        root = new BorderPane();
        root.getStyleClass().add("main-root");
        root.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        VBox menuLateral = new VBox();
        menuLateral.getStyleClass().add("menu-lateral-root");

        btnListarFaturas = criarBotao("Listar Faturas", "/icons/list.png", "botao-listagem");
        btnListarMarcas = criarBotao("Listar Marcas", "/icons/list.png", "botao-listagem");
        btnArquivadas = criarBotao("Arquivadas", "/icons/archive.png", "botao-listagem");
        btnNovaFatura = criarBotao("Cadastrar Faturas", "/icons/plus.png", "botao-cadastro");
        btnNovaMarca = criarBotao("Cadastrar Marca", "/icons/plus.png", "botao-cadastro");

        labelText = new Label("Bem-vindo ao GuardouPagou");
        labelText.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
        labelText.setTextFill(Color.web("#000000"));

        VBox secaoListagens = new VBox(
                criarTitulo("Principais Listagens"),
                btnListarFaturas,
                btnListarMarcas,
                btnArquivadas
        );
        secaoListagens.getStyleClass().add("menu-section");

        VBox secaoCadastros = new VBox(
                criarTitulo("Novos Cadastros"),
                btnNovaFatura,
                btnNovaMarca
        );
        secaoCadastros.getStyleClass().add("menu-section");

        btnSalvarEmail = criarBotao("E-mails de Alerta", "/icons/campaing.png", "botao-listagem");
        btnSalvarEmail.setPrefWidth(220);

        VBox secaoOutros = new VBox(
                criarTitulo("Outros"),
                btnSalvarEmail
        );
        secaoOutros.getStyleClass().add("menu-section");

        menuLateral.getChildren().addAll(
                criarLogo(),
                criarSeparadorLogo(),
                secaoListagens,
                secaoCadastros,
                secaoOutros,
                criarEspaçoFlexível()
        );

        btnListarFaturas.setFocusTraversable(false);
        btnListarMarcas.setFocusTraversable(false);
        btnArquivadas.setFocusTraversable(false);
        btnNovaFatura.setFocusTraversable(false);
        btnNovaMarca.setFocusTraversable(false);
        btnSalvarEmail.setFocusTraversable(false);

        root.setLeft(menuLateral);
        root.setCenter(labelText);
    }

    private Button criarBotao(String texto, String iconPath, String cssClass) {
        Button btn = new Button(" " + texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().addAll("menu-button", cssClass);

        if (iconPath != null) {
            try {
                ImageView icon = new ImageView(getClass().getResource(iconPath).toExternalForm());
                icon.setPreserveRatio(true);
                btn.setGraphic(icon);
            } catch (Exception e) {
                System.err.println("Erro ao carregar ícone: " + iconPath);
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
            Image logoImage = new Image(getClass().getResource("/icons/G-Clock_home.png").toExternalForm());
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

    private VBox criarEspaçoFlexível() {
        VBox espaço = new VBox();
        VBox.setVgrow(espaço, Priority.ALWAYS);
        return espaço;
    }

    public void mostrarListaMarcas(ObservableList<Marca> marcas) {
        TableView<Marca> tabela = new TableView<>();
        tabela.setStyle("-fx-border-color: #4A4A4A; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        TableColumn<Marca, Integer> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaId.setCellFactory(column -> new TableCell<Marca, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(id.toString());
                    setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        colunaId.setPrefWidth(80);

        TableColumn<Marca, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setCellFactory(column -> new TableCell<Marca, String>() {
            @Override
            protected void updateItem(String nome, boolean empty) {
                super.updateItem(nome, empty);
                if (empty || nome == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(nome);

                    Marca marca = getTableView().getItems().get(getIndex());
                    String cor = marca.getCor();

                    if (cor != null && cor.matches("#[0-9A-Fa-f]{6}")) {
                        setStyle("-fx-text-fill: " + cor + "; "
                                + "-fx-font-weight: bold; "
                                + "-fx-border-color: #4A4A4A; "
                                + "-fx-border-width: 0.5; "
                                + "-fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #FFFFFF; "
                                + "-fx-font-weight: bold; "
                                + "-fx-border-color: #4A4A4A; "
                                + "-fx-border-width: 0.5; "
                                + "-fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });
        colunaNome.setPrefWidth(200);

        TableColumn<Marca, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colunaDescricao.setCellFactory(column -> new TableCell<Marca, String>() {
            @Override
            protected void updateItem(String descricao, boolean empty) {
                super.updateItem(descricao, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (descricao == null || descricao.trim().isEmpty()) {
                    setText("Nenhuma descrição adicionada");
                    setStyle("");
                } else {
                    setText(descricao);
                    setStyle("");
                }
            }
        });
        colunaDescricao.setPrefWidth(250);

        tabela.getColumns().addAll(colunaId, colunaNome, colunaDescricao);
        tabela.setItems(marcas);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #BDBDBD;");

        Label titulo = new Label("LISTAGEM DE MARCAS");
        titulo.setStyle("-fx-text-fill: #F0A818; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox toolbar = new HBox(10);
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle("-fx-background-color: #C88200; -fx-text-fill: #000000; -fx-font-weight: bold;");

        Button btnNovaMarca = new Button("Nova Marca");
        btnNovaMarca.setStyle("-fx-background-color: #F0A818; -fx-text-fill: #000000; -fx-font-weight: bold;");

        toolbar.getChildren().addAll(btnAtualizar, btnNovaMarca);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(titulo, toolbar, tabela);
        root.setCenter(container);

        btnAtualizar.setOnAction(e -> atualizarListaMarcas());
        btnNovaMarca.setOnAction(e -> mostrarFormularioMarca());
    }

    public void mostrarListaFaturas(ObservableList<Fatura> faturas) {
        TableView<Fatura> tabela = new TableView<>();
        tabela.setStyle("-fx-border-color: #4A4A4A; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        TableColumn<Fatura, String> colunaNumeroNota = new TableColumn<>("Nº NOTA FISCAL");
        colunaNumeroNota.setCellValueFactory(new PropertyValueFactory<>("numeroNota"));
        colunaNumeroNota.setCellFactory(column -> new TableCell<Fatura, String>() {
            @Override
            protected void updateItem(String numeroNota, boolean empty) {
                super.updateItem(numeroNota, empty);
                if (empty || numeroNota == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(numeroNota);
                    setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        colunaNumeroNota.setPrefWidth(150);

        TableColumn<Fatura, Integer> colunaOrdem = new TableColumn<>("ORDEM DA FATURA");
        colunaOrdem.setCellValueFactory(new PropertyValueFactory<>("numeroFatura"));
        colunaOrdem.setCellFactory(column -> new TableCell<Fatura, Integer>() {
            @Override
            protected void updateItem(Integer numeroFatura, boolean empty) {
                super.updateItem(numeroFatura, empty);
                if (empty || numeroFatura == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(numeroFatura.toString());
                    setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        colunaOrdem.setPrefWidth(120);

        TableColumn<Fatura, LocalDate> colunaVencimento = new TableColumn<>("DATA DE VENCIMENTO");
        colunaVencimento.setCellValueFactory(new PropertyValueFactory<>("vencimento"));
        colunaVencimento.setCellFactory(column -> new TableCell<Fatura, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate vencimento, boolean empty) {
                super.updateItem(vencimento, empty);
                if (empty || vencimento == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(vencimento.format(formatter));
                    setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        colunaVencimento.setPrefWidth(120);

        TableColumn<Fatura, String> colunaMarca = new TableColumn<>("MARCA");
        colunaMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colunaMarca.setCellFactory(column -> new TableCell<Fatura, String>() {
            @Override
            protected void updateItem(String marca, boolean empty) {
                super.updateItem(marca, empty);
                if (empty || marca == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(marca);
                    setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #4A4A4A; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        colunaMarca.setPrefWidth(150);

        TableColumn<Fatura, String> colunaStatus = new TableColumn<>("STATUS DA FATURA");
        colunaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colunaStatus.setCellFactory(column -> new TableCell<Fatura, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equalsIgnoreCase("Vencida")) {
                        setStyle("-fx-text-fill: #f0a818; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });
        colunaStatus.setPrefWidth(120);

        TableColumn<Fatura, Void> colunaAcoes = new TableColumn<>("Ações");
        colunaAcoes.setPrefWidth(200); // Ajusta a largura para acomodar os dois botões
        colunaAcoes.setCellFactory(param -> new TableCell<Fatura, Void>() {
            private final Button btnEmitida = new Button("Emitida");
            private final Button btnEditar = new Button("Editar"); // NOVO BOTÃO DE EDIÇÃO

            {
                // Estilo para o botão Emitida
                btnEmitida.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px; -fx-background-radius: 3;");
                btnEmitida.setOnAction(event -> {
                    Fatura fatura = getTableView().getItems().get(getIndex());
                    marcarFaturaComoEmitida(fatura);
                });

                // Estilo para o botão Editar
                btnEditar.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px; -fx-background-radius: 3;");
                btnEditar.setOnAction(event -> {
                    Fatura fatura = getTableView().getItems().get(getIndex());
                    // Chama o método para abrir a modal de edição
                    abrirModalEdicao(fatura.getNotaFiscalId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Fatura fatura = getTableView().getItems().get(getIndex());

                    HBox buttonContainer = new HBox(5); // Espaçamento entre os botões
                    buttonContainer.setAlignment(Pos.CENTER);

                    // Condição para mostrar/ocultar o botão Emitida
                    if (!"Emitida".equalsIgnoreCase(fatura.getStatus()) && !"Vencida".equalsIgnoreCase(fatura.getStatus())) {
                        buttonContainer.getChildren().add(btnEmitida);
                    }

                    // Condição para mostrar/ocultar o botão Editar (apenas para notas não arquivadas)
                    // Precisamos buscar o status de arquivamento da NotaFiscal
                    try {
                        NotaFiscalDAO notaFiscalDAO = new NotaFiscalDAO();
                        // Aqui precisamos de um método para obter a NF e seu status de arquivamento
                        // Fatura já tem o notaFiscalId, mas não se a NF está arquivada.
                        // Poderíamos adicionar `boolean arquivada` ao modelo Fatura, ou buscar aqui.
                        // Para evitar chamadas de DB pesadas na UI, idealmente o arquivada viria na Fatura
                        // Por simplicidade, vamos buscar a NotaFiscal aqui (pode impactar performance em grandes listas)
                        NotaFiscal nfAssociada = notaFiscalDAO.obterNotaFiscalPorId(fatura.getNotaFiscalId());
                        if (nfAssociada != null && !nfAssociada.isArquivada()) { // Assume que NotaFiscal tem isArquivada()
                            buttonContainer.getChildren().add(btnEditar);
                        } else {
                            // Se estiver arquivada, o botão editar não aparece
                            // Ou se nfAssociada for null (erro ao buscar), também não aparece
                        }
                    } catch (SQLException e) {
                        System.err.println("Erro ao verificar status de arquivamento para edição: " + e.getMessage());
                        // Em caso de erro, não mostra o botão para evitar edição em estado incerto
                    }

                    setGraphic(buttonContainer);
                }
            }
        });

        tabela.getColumns().addAll(colunaNumeroNota, colunaOrdem, colunaVencimento, colunaMarca, colunaStatus, colunaAcoes);
        tabela.setItems(faturas);

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #BDBDBD;");

        Label titulo = new Label("LISTAGEM DE FATURAS");
        titulo.setStyle("-fx-text-fill: #F0A818; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox toolbar = new HBox(10);
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle("-fx-background-color: #C88200; -fx-text-fill: #000000; -fx-font-weight: bold;");

        rbFiltraPeriodo.setToggleGroup(filtroToggleGroup);
        rbFiltraPeriodo.setStyle("-fx-text-fill: #000000;");

        rbFiltraMarca.setToggleGroup(filtroToggleGroup);
        rbFiltraMarca.setStyle("-fx-text-fill: #000000;");

        if (dpFiltroPeriodo == null) {
            dpFiltroPeriodo = new DatePicker();
            dpFiltroPeriodo.setPromptText("Selecione a data");
            dpFiltroPeriodo.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
            dpFiltroPeriodo.setPrefWidth(200);
            dpFiltroPeriodo.setVisible(false);
            dpFiltroPeriodo.setManaged(false);
        }

        if (cbFiltroMarca == null) {
            cbFiltroMarca = new ComboBox<>();
            cbFiltroMarca.setPromptText("Selecione a Marca");
            cbFiltroMarca.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
            cbFiltroMarca.setPrefWidth(200);
            cbFiltroMarca.setVisible(false);
            cbFiltroMarca.setManaged(false);

            try {
                ObservableList<Marca> marcas = new MarcaDAO().listarMarcas();
                cbFiltroMarca.setItems(marcas);
                cbFiltroMarca.setCellFactory(lv -> new ListCell<Marca>() {
                    @Override
                    protected void updateItem(Marca item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getNome());
                    }
                });
                cbFiltroMarca.setButtonCell(new ListCell<Marca>() {
                    @Override
                    protected void updateItem(Marca item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getNome());
                    }
                });
            } catch (SQLException e) {
                System.err.println("Erro ao carregar marcas para o ComboBox: " + e.getMessage());
            }
        }

        if (filtroContainer == null) {
            filtroContainer = new VBox(10);
            filtroContainer.setAlignment(Pos.CENTER_LEFT);
            filtroContainer.setPadding(new Insets(0, 0, 10, 0));
            filtroContainer.getChildren().addAll(dpFiltroPeriodo, cbFiltroMarca);
        }

        toolbar.getChildren().clear();
        toolbar.getChildren().addAll(rbFiltraPeriodo, rbFiltraMarca, btnAtualizar);
        toolbar.setSpacing(15);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(titulo, toolbar, filtroContainer, tabela);
        root.setCenter(container);

        btnAtualizar.setOnAction(e -> atualizarListaFaturas());

        if (rbFiltraPeriodo.getOnAction() == null) {
            btnAtualizar.setOnAction(e -> atualizarListaFaturas());

            rbFiltraPeriodo.setOnAction(e -> {
                System.out.println("Filtrar por Periodo selecionado");
                dpFiltroPeriodo.setVisible(true);
                dpFiltroPeriodo.setManaged(true);
                cbFiltroMarca.setVisible(false);
                cbFiltroMarca.setManaged(false);

                dpFiltroPeriodo.setOnAction(event -> {
                    System.out.println("Data selecionada: " + dpFiltroPeriodo.getValue());

                });
            });

            rbFiltraMarca.setOnAction(e -> {
                System.out.println("Filtrar por Marca selecionado");
                cbFiltroMarca.setVisible(true);
                cbFiltroMarca.setManaged(true);
                dpFiltroPeriodo.setVisible(false);
                dpFiltroPeriodo.setManaged(false);

                cbFiltroMarca.setOnAction(event -> {
                    Marca selectedMarca = cbFiltroMarca.getSelectionModel().getSelectedItem();
                    if (selectedMarca != null) {
                        System.out.println("Marca selecionada: " + selectedMarca.getNome());
                    }
                });
            });
        }
    }

    private void marcarFaturaComoEmitida(Fatura fatura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Marcar Fatura como Emitida");
        alert.setContentText("Tem certeza que deseja marcar a fatura Nº " + fatura.getNumeroFatura() + " da Nota " + fatura.getNumeroNota() + " como emitida?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FaturaDAO faturaDAO = new FaturaDAO();

                boolean faturaMarcada = faturaDAO.marcarFaturaIndividualComoEmitida(fatura.getId());

                if (faturaMarcada) {
                    boolean todasEmitidasNaNota = faturaDAO.todasFaturasDaNotaEmitidas(fatura.getNotaFiscalId());

                    if (todasEmitidasNaNota) {
                        boolean notaFiscalArquivada = new NotaFiscalDAO().marcarComoArquivada(fatura.getNotaFiscalId(), LocalDate.now());
                        if (notaFiscalArquivada) {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Sucesso");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Todas as faturas da Nota Fiscal " + fatura.getNumeroNota() + " foram emitidas e a Nota Fiscal foi arquivada!");
                            successAlert.showAndWait();

                            atualizarListaFaturas();
                            if (getBtnArquivadas() != null) {
                                getBtnArquivadas().fire();
                            }
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Erro ao arquivar a Nota Fiscal após todas as faturas serem emitidas.");
                            errorAlert.showAndWait();
                        }
                    } else {
                        Fatura proximaFaturaDaMesmaNota = faturaDAO.obterProximaFaturaNaoEmitidaDaMesmaNota(fatura.getNotaFiscalId());
                        ObservableList<Fatura> faturasParaExibir = FXCollections.observableArrayList();

                        if (proximaFaturaDaMesmaNota != null) {
                            faturasParaExibir.add(proximaFaturaDaMesmaNota);
                            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                            infoAlert.setTitle("Fatura Emitida");
                            infoAlert.setHeaderText(null);
                            infoAlert.setContentText("Fatura Nº " + fatura.getNumeroFatura() + " marcada como emitida. Próxima fatura da Nota " + fatura.getNumeroNota() + " exibida.");
                            infoAlert.showAndWait();
                        } else {
                            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                            infoAlert.setTitle("Fatura Emitida");
                            infoAlert.setHeaderText(null);
                            infoAlert.setContentText("Fatura Nº " + fatura.getNumeroFatura() + " marcada como emitida. Não há mais faturas não emitidas para esta nota.");
                            infoAlert.showAndWait();
                            atualizarListaFaturas();
                            return;
                        }
                        mostrarListaFaturas(faturasParaExibir);
                    }
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erro");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Não foi possível marcar a fatura como emitida.");
                    errorAlert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Erro ao processar emissão da fatura: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    // NOVO MÉTODO: Abrir a janela modal de edição
    private void abrirModalEdicao(int notaFiscalId) {
        try {
            NotaFiscalDAO notaFiscalDAO = new NotaFiscalDAO();
            FaturaDAO faturaDAO = new FaturaDAO();

            NotaFiscal notaParaEditar = notaFiscalDAO.obterNotaFiscalPorId(notaFiscalId);
            ObservableList<Fatura> faturasParaEditar = faturaDAO.listarFaturasPorNotaFiscalId(notaFiscalId);

            if (notaParaEditar == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Nota Fiscal não encontrada para edição.");
                alert.showAndWait();
                return;
            }

            // Critério de aceitação: A edição deve ser permitida apenas para notas que não estejam arquivadas.
            // Isso já é tratado no updateItem do TableCell, mas um double-check aqui é bom.
            if (notaParaEditar.isArquivada()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atenção");
                alert.setHeaderText(null);
                alert.setContentText("Não é possível editar uma Nota Fiscal arquivada.");
                alert.showAndWait();
                return;
            }

            Stage modalStage = new Stage();
            Window owner = root.getScene().getWindow();
            modalStage.initOwner(owner);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.setTitle("Editar Nota Fiscal e Faturas");

            // Crie a view de edição passando os dados
            NotaFaturaView edicaoView = new NotaFaturaView(notaParaEditar, faturasParaEditar);
            new NotaFaturaController(edicaoView, notaParaEditar, faturasParaEditar); // Passe os dados para o controller também

            Scene scene = new Scene(edicaoView.getRoot(), 850, 650); // Ajuste o tamanho conforme necessário
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            /* Adicionar ícone à janela modal
            modalStage.getIcons().add(
                    new Image(
                            Objects.requireNonNull(
                                    getClass().getResourceAsStream("/icons/edit.png") // Você precisará de um ícone 'edit.png'
                            )
                    )
            );
             */
            // Adicione um listener para atualizar a lista principal quando o modal for fechado
            modalStage.setOnHidden(e -> atualizarListaFaturas());

            modalStage.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar dados para edição: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void atualizarListaMarcas() {
        try {
            ObservableList<Marca> marcas = new MarcaDAO().listarMarcas();
            mostrarListaMarcas(marcas);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar marcas: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void mostrarFormularioMarca() {
        MarcaView marcaView = new MarcaView();
        new MarcaController(marcaView);
        root.setCenter(marcaView.getRoot());
    }

    private void editarMarca(Marca marca) {
        MarcaView marcaView = new MarcaView();
        new MarcaController(marcaView);

        marcaView.getNomeField().setText(marca.getNome());
        marcaView.getDescricaoArea().setText(marca.getDescricao());
        try {
            String cor = marca.getCor() != null && marca.getCor().matches("#[0-9A-Fa-f]{6}") ? marca.getCor() : "#000000";
            marcaView.getCorPicker().setValue(Color.web(cor));
        } catch (IllegalArgumentException e) {
            System.out.println("Cor inválida: " + marca.getCor());
            marcaView.getCorPicker().setValue(Color.BLACK);
        }

        root.setCenter(marcaView.getRoot());
    }

    private void excluirMarca(Marca marca) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Excluir Marca");
        alert.setContentText("Tem certeza que deseja excluir a marca " + marca.getNome() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                new MarcaDAO().excluirMarca(marca.getId());
                atualizarListaMarcas();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Erro ao excluir marca: " + e.getMessage());
                errorAlert.showAndWait();
            }
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
