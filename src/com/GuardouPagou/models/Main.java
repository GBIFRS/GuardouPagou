package com.GuardouPagou.models;

import com.GuardouPagou.views.MainView;
import com.GuardouPagou.controllers.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {

        MainView mainView = new MainView();
        new MainController(mainView);

        Scene scene = new Scene(mainView.getRoot(), 950, 700);

        primaryStage.getIcons().add(
                new Image(
                        getClass().getResourceAsStream(
                                "/com/GuardouPagou/views/icons/G-Clock(100x100px).png"
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