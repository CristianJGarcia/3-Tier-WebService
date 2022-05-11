package main;

import controller.ViewSwitcher;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//CS 4743 Assignment 2 by Cristian Garcia and Christopher Stromer
public class Main extends Application {
    public int myNum = 0;

    public static void main(String [] args) {
        // if configuration is not working CMD: java -cp .\target\assignment1-1.0-SNAPSHOT.jar main.Main
        // if jars plugin is not working CMD2: java -cp target\classes;target\libs\log4j-api-2.11.0.jar;target\libs\log4j-core-2.11.0.jar main.Main
        // CMD to run if everything works CMD3: java -jar .\target\assignment1-1.0-SNAPSHOT.jar
        // mvn spring-boot:run

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/mainview.fxml"));
        loader.setController(ViewSwitcher.getInstance());
        Parent rootNode = loader.load();
        Scene scene = new Scene(rootNode);
        stage.setScene(scene);
        stage.setTitle("People");
        stage.show();
    }

    public int getMyNum(){
        return myNum;
    }

    public void setMyNum(int n) {
        myNum = n;
    }
}
