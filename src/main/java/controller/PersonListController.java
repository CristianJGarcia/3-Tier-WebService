package controller;

import backend.gateway.PersonGateway;
import backend.gateway.SessionGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import model.AuditTrail;
import model.FetchResults;
import model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PersonListController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    private static final String WS_URL = "http://localhost:8080";
    private static String sessionID;
    ArrayList<Person> people_list;
    List<AuditTrail> audit_list;

    @FXML
    private TextField searchbar;
    @FXML
    private Text notfound, fetchText;
    @FXML
    private Button add;
    @FXML
    private Button edit;
    @FXML
    private Button delete;
    @FXML
    private Button first, prev, next, last;
    @FXML
    private ListView<Person> personListView;

    public static ObservableList<Person> allPersons;
    private FetchResults results;

    private Person person;

    public PersonListController() {
        allPersons = FXCollections.observableArrayList();
        people_list = null;
        //allPersons.addAll(people_list);
    }

    public void fetchPeople(String search, int pageNum){
        allPersons = FXCollections.observableArrayList();
        try {

            results = new PersonGateway(SessionGateway.getInstance().getSessionID()).fetch_People_Search(SessionGateway.getInstance().getSessionID(),search, pageNum);

            people_list = results.getPeople();
            allPersons.addAll(people_list);
            SessionGateway.getInstance().setPeople(people_list);

            displayFetchText(people_list.size());
            visibilityOfButtons(people_list);

            personListView.refresh();
            personListView.setItems(allPersons);

            notfound.setVisible(allPersons.isEmpty());
            if(notfound.isVisible()){
                next.setDisable(true);
                last.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("couldn't retrieve");
        }
    }

    private void visibilityOfButtons(List<Person> people_list){
        if (people_list.size() == 0){
            first.setDisable(true);
            last.setDisable(true);
            //prev.setDisable(true);
        }
        else {
            first.setDisable(false);
            last.setDisable(false);
        }

        if (people_list.size() < 10)
        {
            last.setDisable(true);
            next.setDisable(true);
        }
        else{
            last.setDisable(false);
            next.setDisable(false);
        }

        if (results.getCurrentPage() == 0 || people_list.size() == 0){
            prev.setDisable(true);
        }
        else {
            prev.setDisable(false);
        }

        if (results.getNumRows() % 10 == 0){
            if(((results.getNumRows() / 10) - 1) == results.getCurrentPage()){
                next.setDisable(true);
                last.setDisable(true);
            }
            else {
                next.setDisable(false);
                last.setDisable(false);
            }
        }
        else{
            if((int)Math.ceil((float)(results.getNumRows() / 10)) == results.getCurrentPage()){
                next.setDisable(true);
                last.setDisable(true);
            }
            else {
                next.setDisable(false);
                last.setDisable(false);
            }
        }


    }

    private void displayFetchText(int numRecords){
        int x = 0;
        if (numRecords > 0)
            x = 1;
        int beg = results.getCurrentPage() * results.getPageSize() + x;
        int end = beg + results.getPageSize() - x;
        if( end > results.getNumRows())
            end = results.getNumRows();
        int total = results.getNumRows();
        fetchText.setText("Fetched records " + beg + " to " + end + " out of  " + total + " records");

    }


    @FXML
    void search() throws IOException, URISyntaxException {

        if (searchbar.getText().isBlank()){
            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        }else{
            fetchPeople(searchbar.getText(),0);
//            System.out.println("Trying to execute search result");
//
//            allPersons = FXCollections.observableArrayList();
//
//            try {
//                //personListView.getItems().clear();
//                //people_list.clear();
//
//                people_list = new PersonGateway(SessionGateway.getInstance().getSessionID()).fetch_People_Search(SessionGateway.getInstance().getSessionID(), searchbar.getText(), 1);
//                System.out.println(people_list.size());
//
//                allPersons.addAll(people_list);
//
//
//
//                personListView.refresh();
//                personListView.setItems(allPersons);
//
//                notfound.setVisible(allPersons.isEmpty());
//
//            }catch (Exception e){
//                logger.debug("Couldn't retrieve");
//                e.printStackTrace();
//            }
        }

    }

    @FXML
    void addFunc(ActionEvent event) {
        personListView.getSelectionModel().clearSelection();
        PersonParameters.setPersonParam(null);
        ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
    }
    @FXML
    void editFunc(ActionEvent event) {
        if(!personListView.getSelectionModel().isEmpty()) {
            PersonParameters.setPersonParam(personListView.getSelectionModel().getSelectedItem());
        }else {
            PersonParameters.setPersonParam(null);
        }
        ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
    }
    @FXML
    void deleteFunc(ActionEvent event) {
        String fname = personListView.getSelectionModel().getSelectedItem().getFirstName();
        String lname = personListView.getSelectionModel().getSelectedItem().getLastName();

        // Get index to remove
        int index = personListView.getSelectionModel().getSelectedIndex();
        try {
            Person toDel = personListView.getSelectionModel().getSelectedItem();
            logger.debug("DELETING {} {}", fname, lname);
            new PersonGateway(SessionGateway.getInstance().getSessionID()).deletePerson(SessionGateway.getInstance().getSessionID(), toDel.getId());
        }catch (IOException e){
            e.printStackTrace();
            logger.debug("Could not delete.");
        }
        System.out.println(personListView.getItems().toString());
        // Remove
        personListView.getItems().remove(index);
        System.out.println(personListView.getItems().toString());
    }

    public ObservableList<Person> getAllPersons() {
        return allPersons;
    }

    public void setAllPersons(ObservableList<Person> allPersons) {
        this.allPersons = allPersons;
    }

    @FXML
    public void getFirstPull(){
        fetchPeople(searchbar.getText(), 0);
    }

    @FXML
    public void getPrev(){
        results.setCurrentPage(results.getCurrentPage() - 1);
        next.setDisable(false);
        fetchPeople(searchbar.getText(), results.getCurrentPage());
    }

    @FXML
    public void getNext(){
        results.setCurrentPage(results.getCurrentPage() + 1);
        fetchPeople(searchbar.getText(), results.getCurrentPage());
    }

    @FXML
    public void getLast(){

        if (results.getNumRows() % 10 == 0){
            fetchPeople(searchbar.getText(),(results.getNumRows() / 10) - 1);
        }
        else{
            fetchPeople(searchbar.getText(),(int)Math.ceil((float)(results.getNumRows() / 10)));
        }
        //next.setDisable(true);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("*********Updating list view**********");

        //personListView.setItems(allPersons);
        getFirstPull();
//        personListView.getItems().clear();
//
//        try{
//            people_list = new PersonGateway(SessionGateway.getInstance().getSessionID()).fetch_People(SessionGateway.getInstance().getSessionID());
//
//            SessionGateway.getInstance().setPeople(people_list);
//            allPersons.addAll(people_list);
//
//        } catch (Exception e){
//            logger.debug("Couldn't retrieve");
//            e.printStackTrace();
//        }
//
//        personListView.setItems(allPersons);
//        people_list.clear();
    }
}
