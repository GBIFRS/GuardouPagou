package com.GuardouPagou.models;

import com.GuardouPagou.views.MainView;
import com.GuardouPagou.controllers.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // 🅰️ Carrega a fonte Poppins antes de qualquer tela
        Font poppinsRegular = Font.loadFont(
                Main.class.getClassLoader().getResourceAsStream("fonts/Poppins-Regular.ttf"), 12
        );

        Font poppinsBold = Font.loadFont(
                Main.class.getClassLoader().getResourceAsStream("fonts/Poppins-Bold.ttf"),
                12
        );
        Font poppinsMedium = Font.loadFont(
                Main.class.getClassLoader().getResourceAsStream("fonts/Poppins-Medium.ttf"),
                12
        );

        // Opcional: saída de confirmação
        System.out.println("Fonte Poppins Regular: " + (poppinsRegular != null ? "OK" : "Erro"));
        System.out.println("Fonte Poppins Bold: " + (poppinsMedium != null ? "OK" : "Erro"));
        System.out.println("Fonte Poppins Bold: " + (poppinsBold != null ? "OK" : "Erro"));

        // Cria visual e controller
        MainView mainView = new MainView();
        new MainController(mainView);

        Scene scene = new Scene(mainView.getRoot(), 950, 700);

        // Ícone da janela
        primaryStage.getIcons().add(
                new Image(
                        getClass().getResourceAsStream(
                                "/icons/G-Clock(100x100px).png"
                        )
                )
        );

        primaryStage.setTitle("GuardouPagou - Sistema de Notas e Faturas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
