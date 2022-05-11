package controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;

public class SaveErrorController {

        @FXML
        private Button back;

        @FXML
        void back(){
            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        }


}
