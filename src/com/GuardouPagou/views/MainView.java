package com.GuardouPagou.views;

import com.GuardouPagou.dao.MarcaDAO;
import com.GuardouPagou.controllers.MarcaController;
import com.GuardouPagou.dao.FaturaDAO;
import com.GuardouPagou.dao.NotaFiscalDAO; // Adicionado: para marcar nota fiscal como arquivada
import com.GuardouPagou.models.Fatura;
import com.GuardouPagou.models.Marca;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
// As importações de HBox e Pos já estavam implícitas mas vou garantir que estejam explícitas.
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import com.GuardouPagou.controllers.ArquivadasController; // Adicionado: para navegar para a tela de arquivadas

public class MainView {

    private BorderPane root;
    private Button btnListarFaturas, btnListarMarcas, btnArquivadas;
    private Button btnNovaFatura, btnNovaMarca, btnSalvarEmail;
    private Label labelText; // Alterado de conteudoLabel para labelText para corresponder ao uso em criarUI
    private TextField emailField;
    private RadioButton rbFiltraPeriodo = new RadioButton("Filtrar por Perí­odo");
    private RadioButton rbFiltraMarca = new RadioButton("Filtrar por Marca");
    private ToggleGroup filtroToggleGroup;
    private DatePicker dpDataInicio;
    private DatePicker dpDataFim;
    private ComboBox<Marca> cbFiltroMarca;
    private VBox filtroContainer;

    public BorderPane getRoot() {
        return this.root;
    }

    public MainView() {
        criarUI();
        filtroToggleGroup = new ToggleGroup();
        try {
            // Mudar de new FaturaDAO().listarFaturas() para:
            ObservableList<Fatura> faturas = new FaturaDAO().listarFaturas(false); // Listar apenas não arquivadas
            mostrarListaFaturas(faturas);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao carregar faturas na inicialização: " + ex.getMessage());
            alert.showAndWait();
            root.setCenter(labelText); // Mantém a mensagem padrão em caso de erro
        }
    }

    // Este é o método atualizarListaFaturas() que será usado para recarregar a lista
    private void atualizarListaFaturas() {
        try {
            // Mudar de new FaturaDAO().listarFaturas() para:
            ObservableList<Fatura> faturas;
            if (rbFiltraPeriodo.isSelected()) {
                LocalDate dataSelecionadaInicio = dpDataInicio.getValue();
                LocalDate dataSelecionadaFim = dpDataFim.getValue();
                
                
                if (dataSelecionadaInicio != null && dataSelecionadaFim != null) {
                    System.out.println("Atualizando lista por periodo: " + dataSelecionadaInicio + " ate: " + dataSelecionadaFim);
                    // por Periodo
                    faturas = new FaturaDAO().listarFaturasPorPeriodo(dataSelecionadaInicio, dataSelecionadaFim);

                } else {
                    faturas = new FaturaDAO().listarFaturas(false);
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
                    // Por Marca
                    faturas = new FaturaDAO().listarFaturasPorMarca(marcaSelecionada);
                } else {
                    faturas = new FaturaDAO().listarFaturas(false);
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Atenção");
                    alert.setHeaderText(null);
                    alert.setContentText("Selecione uma marca para filtrar.");
                    alert.showAndWait();
                }
            } else {
                faturas = new FaturaDAO().listarFaturas(false);
                //faturas = new FaturaDAO().listarFaturas();
            }
            mostrarListaFaturas(faturas);
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
        root.setStyle("-fx-background-color: #BDBDBD;");
        root.getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm()
        );
        
        VBox menuLateral = new VBox(20);
        menuLateral.setPadding(new Insets(20));
        menuLateral.setStyle("-fx-background-color: #323437; -fx-min-width: 250px;");

        btnListarFaturas = criarBotao("Listar Faturas", "#C88200");
        btnListarMarcas = criarBotao("Listar Marcas", "#C88200");
        btnArquivadas = criarBotao("Arquivadas", "#C88200");
        btnNovaFatura = criarBotao("Cadastrar Nota com Faturas", "#f0a818");
        btnNovaMarca = criarBotao("Cadastrar nova marca", "#f0a818");

        labelText = new Label("Bem-vindo ao GuardouPagou");
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        labelText.setTextFill(Color.web("#000000"));

        menuLateral.getChildren().addAll(
                criarLogo(),
                criarTitulo("Principais listagens"),
                btnListarFaturas, btnListarMarcas, btnArquivadas,
                criarTitulo("Novos Cadastros"),
                btnNovaFatura, btnNovaMarca,
                criarEspaçoFlexível(),
                criarEmailPanel()
        );

        root.setLeft(menuLateral);
        root.setCenter(labelText);
    }

    private Button criarBotao(String texto, String cor) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + cor + "; "
                + "-fx-text-fill: #000000; "
                + "-fx-font-weight: bold;");
        return btn;
    }

    private VBox criarLogo() {
        VBox logoContainer = new VBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(0, 0, 20, 0));
        logoContainer.setMinHeight(150);

        try {
            Image logoImage = new Image("file:logo.png");
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitWidth(180);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(logoView);
        } catch (Exception e) {
            Label logoPlaceholder = new Label("LOGO DA LOJA");
            logoPlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            logoPlaceholder.setTextFill(Color.web("#f0a818"));
            logoPlaceholder.setStyle("-fx-border-color: #C88200; -fx-border-width: 2px; -fx-padding: 40px;");
            logoContainer.getChildren().add(logoPlaceholder);
        }
        return logoContainer;
    }

    private Label criarTitulo(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#f0a818"));
        return label;
    }

    private VBox criarEspaçoFlexível() {
        VBox espaço = new VBox();
        VBox.setVgrow(espaço, Priority.ALWAYS);
        return espaço;
    }

    private VBox criarEmailPanel() {
        VBox emailPanel = new VBox(10);
        emailPanel.setPadding(new Insets(15));
        emailPanel.setStyle("-fx-background-color: #3d4043; "
                + "-fx-border-color: #C88200; "
                + "-fx-border-radius: 5;");

        Label emailTitle = new Label("Alertas por E-mail");
        emailTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        emailTitle.setTextFill(Color.web("#f0a818"));

        emailField = new TextField();
        emailField.setPromptText("Digite o e-mail para alertas");
        emailField.setPrefWidth(200);
        emailField.setStyle("-fx-prompt-text-fill: #BDBDBD;");

        btnSalvarEmail = new Button("Salvar E-mail");
        btnSalvarEmail.setStyle("-fx-background-color: #C88200; "
                + "-fx-text-fill: #000000; "
                + "-fx-font-weight: bold;");

        emailPanel.getChildren().addAll(emailTitle, emailField, btnSalvarEmail);
        return emailPanel;
    }

    // MÉTODO mostrarListaMarcas - CORRIGIDO E ORIGINAL
    public void mostrarListaMarcas(ObservableList<Marca> marcas) {
        TableView<Marca> tabela = new TableView<>();
        tabela.setStyle("-fx-border-color: #4A4A4A; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        TableColumn<Marca, Integer> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaId.setCellFactory(column -> new TableCell<Marca, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) { // CORRIGIDO: Tipo Integer e lógica original
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

        // Coluna Ação (Editar/Excluir) - Reabilitada conforme sua necessidade em projetos anteriores
        TableColumn<Marca, Void> colunaAcoes = new TableColumn<>("Ações");
        colunaAcoes.setCellFactory(column -> new TableCell<Marca, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");

            {
                btnEditar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px;");
                btnExcluir.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px;");

                btnEditar.setOnAction(e -> {
                    Marca marca = getTableView().getItems().get(getIndex());
                    editarMarca(marca);
                });

                btnExcluir.setOnAction(e -> {
                    Marca marca = getTableView().getItems().get(getIndex());
                    excluirMarca(marca);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox botoes = new HBox(5, btnEditar, btnExcluir);
                    botoes.setAlignment(Pos.CENTER);
                    setGraphic(botoes);
                }
            }
        });
        colunaAcoes.setPrefWidth(150);

        tabela.getColumns().addAll(colunaId, colunaNome, colunaDescricao, colunaAcoes); // Adicionando coluna Ações
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

    // MÉTODO mostrarListaFaturas - ADICIONADO E CORRIGIDO
    public void mostrarListaFaturas(ObservableList<Fatura> faturas) {
        TableView<Fatura> tabela = new TableView<>();
        tabela.setStyle("-fx-border-color: #4A4A4A; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        TableColumn<Fatura, Integer> colunaId = new TableColumn<>("ID");
        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaId.setCellFactory(column -> new TableCell<Fatura, Integer>() {
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

        TableColumn<Fatura, String> colunaNumeroNota = new TableColumn<>("NÚMERO DA NOTA");
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

        TableColumn<Fatura, LocalDate> colunaVencimento = new TableColumn<>("VENCIMENTO");
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
                    setStyle("-fx-text-fill: #000000; -fx-background-color: transparent; -fx-font-weight: bold; -fx-border-color: #ffffff; -fx-border-width: 0.5; -fx-alignment: CENTER-LEFT;");
                }
            }
        });
        colunaMarca.setPrefWidth(150);

        TableColumn<Fatura, String> colunaStatus = new TableColumn<>("STATUS");
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

        // INÍCIO DA ADIÇÃO DA NOVA COLUNA E LÓGICA
        TableColumn<Fatura, Void> colunaAcoes = new TableColumn<>("Ações");
        colunaAcoes.setCellFactory(param -> new TableCell<Fatura, Void>() {
            private final Button btnEmitida = new Button("Emitida");

            {
                btnEmitida.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5px;");
                btnEmitida.setOnAction(event -> {
                    Fatura fatura = getTableView().getItems().get(getIndex());
                    marcarFaturaComoEmitida(fatura);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Oculta o botão se a fatura já estiver arquivada ou emitida (status "Emitida" ou "Vencida")
                    Fatura fatura = getTableView().getItems().get(getIndex());
                    if ("Emitida".equalsIgnoreCase(fatura.getStatus()) || "Vencida".equalsIgnoreCase(fatura.getStatus())) {
                        setGraphic(null); // Oculta o botão
                    } else {
                        HBox buttonContainer = new HBox(btnEmitida);
                        buttonContainer.setAlignment(Pos.CENTER);
                        setGraphic(buttonContainer);
                    }
                }
            }
        });
        colunaAcoes.setPrefWidth(100); // Ajuste a largura conforme necessário

        // Adicione a nova coluna aqui
        tabela.getColumns().addAll(colunaId, colunaNumeroNota, colunaOrdem, colunaVencimento, colunaMarca, colunaStatus, colunaAcoes);
        tabela.setItems(faturas);
        // FIM DA ADIÇÃO DA NOVA COLUNA E LÓGICA

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #BDBDBD;");

        Label titulo = new Label("LISTAGEM DE FATURAS");
        titulo.setStyle("-fx-text-fill: #F0A818; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox toolbar = new HBox(10);
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle("-fx-background-color: #C88200; -fx-text-fill: #000000; -fx-font-weight: bold;");

        // --- filtros - RADIOBUTTONS E TOGGLEGROUP ---
        rbFiltraPeriodo.setToggleGroup(filtroToggleGroup);
        rbFiltraPeriodo.setStyle("-fx-text-fill: #000000;");

        rbFiltraMarca.setToggleGroup(filtroToggleGroup);
        rbFiltraMarca.setStyle("-fx-text-fill: #000000;");

        // Inicializa o DatePicker e ComboBox AQUI
        if (dpDataInicio == null) {
            dpDataInicio = new DatePicker();
            dpDataInicio.setPromptText("Data inicial");
            dpDataInicio.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
            dpDataInicio.setPrefWidth(150);
            dpDataInicio.setVisible(false);
            dpDataInicio.setManaged(false);
        }

        if (dpDataFim == null) {
            dpDataFim = new DatePicker();
            dpDataFim.setPromptText("Data final");
            dpDataFim.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
            dpDataFim.setPrefWidth(150);
            dpDataFim.setVisible(false);
            dpDataFim.setManaged(false);
        }

        if (cbFiltroMarca == null) {
            cbFiltroMarca = new ComboBox<>();
            cbFiltroMarca.setPromptText("Selecione a Marca");
            cbFiltroMarca.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;");
            cbFiltroMarca.setPrefWidth(200);
            cbFiltroMarca.setVisible(false);
            cbFiltroMarca.setManaged(false); // Nao ocupa espaco quando invisivel

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
            filtroContainer.getChildren().addAll(dpDataInicio, dpDataFim, cbFiltroMarca);
        }

        toolbar.getChildren().clear();
        toolbar.getChildren().addAll(rbFiltraPeriodo, rbFiltraMarca, btnAtualizar);
        toolbar.setSpacing(15);
        // --- FIM DA ADIÇÃO DOS RADIOBUTTONS E TOGGLEGROUP ---
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(titulo, toolbar, filtroContainer, tabela);
        root.setCenter(container);

        btnAtualizar.setOnAction(e -> atualizarListaFaturas());

        // Adiciona os listeners para os RadioButtons e o botão de atualização apenas uma vez
        if (rbFiltraPeriodo.getOnAction() == null) {
            btnAtualizar.setOnAction(e -> atualizarListaFaturas());

            rbFiltraPeriodo.setOnAction(e -> {
                System.out.println("Filtrar por Periodo selecionado");
                dpDataInicio.setVisible(true);
                dpDataInicio.setManaged(true);
                dpDataFim.setVisible(true);
                dpDataFim.setManaged(true);
                cbFiltroMarca.setVisible(false);
                cbFiltroMarca.setManaged(false);
                /*
                dpFiltroPeriodo.setOnAction(event -> {
                    System.out.println("Data selecionada: " + dpFiltroPeriodo.getValue());

                });
                */
            });

            rbFiltraMarca.setOnAction(e -> {
                System.out.println("Filtrar por Marca selecionado");
                cbFiltroMarca.setVisible(true);
                cbFiltroMarca.setManaged(true);
                dpDataInicio.setVisible(false);
                dpDataInicio.setManaged(false);
                dpDataFim.setVisible(false);
                dpDataFim.setManaged(false);


                cbFiltroMarca.setOnAction(event -> {
                    Marca selectedMarca = cbFiltroMarca.getSelectionModel().getSelectedItem();
                    if (selectedMarca != null) {
                        System.out.println("Marca selecionada: " + selectedMarca.getNome());
                    }
                });
            });
        }
    }

    // MÉTODO marcarFaturaComoEmitida - ADICIONADO
    private void marcarFaturaComoEmitida(Fatura fatura) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Marcar Fatura como Emitida");
        alert.setContentText("Tem certeza que deseja marcar a fatura Nº " + fatura.getNumeroFatura() + " da Nota " + fatura.getNumeroNota() + " como emitida?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 1. Marcar a fatura individual como 'Emitida' no banco de dados
                // NOTA: 'marcarFaturaIndividualComoEmitida' e 'todasFaturasDaNotaEmitidas'
                // devem existir ou ser implementados em FaturaDAO.java
                boolean faturaMarcada = new FaturaDAO().marcarFaturaIndividualComoEmitida(fatura.getId());

                if (faturaMarcada) {
                    // 2. Verificar se TODAS as faturas desta nota fiscal estão agora 'Emitida'
                    boolean todasEmitidas = new FaturaDAO().todasFaturasDaNotaEmitidas(fatura.getNotaFiscalId());

                    if (todasEmitidas) {
                        // Se todas as faturas da Nota Fiscal estão emitidas, marcar a Nota Fiscal como arquivada
                        // NOTA: 'marcarComoArquivada' deve existir ou ser implementado em NotaFiscalDAO.java
                        boolean notaFiscalArquivada = new NotaFiscalDAO().marcarComoArquivada(fatura.getNotaFiscalId(), LocalDate.now());
                        if (notaFiscalArquivada) {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Sucesso");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Todas as faturas da Nota Fiscal " + fatura.getNumeroNota() + " foram emitidas e a Nota Fiscal foi arquivada!");
                            successAlert.showAndWait();

                            // Atualizar a lista e navegar para arquivadas
                            atualizarListaFaturas();
                            if (getBtnArquivadas() != null) {
                                getBtnArquivadas().fire(); // Simula o clique para navegar
                            }
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Erro ao arquivar a Nota Fiscal após todas as faturas serem emitidas.");
                            errorAlert.showAndWait();
                        }
                    } else {
                        // Se nem todas as faturas da nota foram emitidas, apenas atualiza a lista atual
                        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                        infoAlert.setTitle("Fatura Emitida");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText("Fatura Nº " + fatura.getNumeroFatura() + " marcada como emitida. Aguardando outras faturas da Nota " + fatura.getNumeroNota() + " para arquivamento.");
                        infoAlert.showAndWait();
                        atualizarListaFaturas(); // Apenas recarrega a lista para refletir a mudança de status
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
        return labelText; // CORRIGIDO: Retornar labelText, não conteudoLabel
    }
}
