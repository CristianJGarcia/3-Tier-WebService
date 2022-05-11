package controller;

import backend.gateway.LoginGatewayMySQL;
import backend.gateway.SessionGateway;
import backend.services.LoginControllerServiceJDBC;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LoginController implements Initializable {
    private static Logger logger = LogManager.getLogger();
    String user, psw;
    String sessionKEY;
    int status;
    private static final SessionGateway sessionGateway = SessionGateway.getInstance();

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginButton;

    // Model
    private Person person;

    public LoginController(Person person){ this.person = person; }

    @FXML
    void login(ActionEvent event) throws IOException {
        user = username.getText();
        psw = password.getText();
        try {
            status = sessionGateway.authenticate(user,psw);

            switch (status){
                case 200:
                    logger.debug("{} LOGGED IN", username.getText());
                    ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
                    break;
                case 401:
                    logger.debug("UNAUTHORIZED");
                    break;
            }
        } catch (RuntimeException | IOException e){
            logger.debug("Error.");
            e.printStackTrace();
        }


    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


}
