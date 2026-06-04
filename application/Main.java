package application;

import application.view.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        ViewManager viewManager = new ViewManager(primaryStage);
        viewManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}