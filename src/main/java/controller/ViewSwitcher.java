package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import model.Person;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ViewSwitcher implements Initializable {
    private static ViewSwitcher instance = null;

    @FXML
    private BorderPane rootPane;

    private ViewSwitcher() {

    }

    //Singleton
    public static ViewSwitcher getInstance(){
        if (instance == null){
            instance = new ViewSwitcher();
        }
        return instance;
    }

    public void switchView(ViewType viewType){
        FXMLLoader loader = null;

        try {
            switch (viewType){
                case LoginView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/LoginView.fxml"));
                    loader.setController(new LoginController(PersonParameters.getPersonParam()));
                    break;

                case PersonListView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/PersonListView.fxml"));
                    loader.setController(new PersonListController());
                    break;

                case PersonDetailView:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/PersonDetailView.fxml"));
                    loader.setController(new PersonDetailController(PersonParameters.getPersonParam()));
                    break;

                case SaveError:
                    loader = new FXMLLoader(ViewSwitcher.class.getResource("/SaveError.fxml"));
                    loader.setController(new SaveErrorController());
                    break;

                default:
                    break;
            }
            Parent rootNode = loader.load();
            rootPane.setCenter(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        switchView(ViewType.LoginView);
    }
}
