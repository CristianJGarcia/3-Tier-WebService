package controller;

import backend.gateway.PersonGateway;
import backend.gateway.SessionGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.AuditTrail;
import model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PersonDetailController implements Initializable {
    private static Logger logger = LogManager.getLogger();
    List<AuditTrail> audit_list;


    @FXML
    private TextField id;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField dob;
    @FXML
    private TextField age;
    @FXML
    private Button save;
    @FXML
    private TableColumn date;
    @FXML
    private TableColumn by;
    @FXML
    private TableColumn desc;
    @FXML
    private TableView<AuditTrail> auditTrailTableView;

    private Person person;
    public static ObservableList<AuditTrail> allAudits;

    public PersonDetailController(Person p){
        allAudits = FXCollections.observableArrayList();
        audit_list = null;
        person = p;
    }



    @FXML
    void save() throws IOException {
        // Check if current person is empty
        if (person == null) {
            Person newPerson = new Person();
            newPerson.setPersonName(firstName.getText(), lastName.getText());
            String date = dob.getText();
            // month day year
            String[] dateArr = date.split("-");
            int month = Integer.parseInt(dateArr[0]);
            int day = Integer.parseInt(dateArr[1]);
            int year = Integer.parseInt(dateArr[2]);

            LocalDate d;
            // Check if DOB is after current date
            if(LocalDate.of(year, month, day).isAfter(LocalDate.now()))
            {
                d = LocalDate.now();
            }
            else{
                d = LocalDate.of(year, month, day);
            }

            newPerson.setFirstName(firstName.getText());
            newPerson.setLastName(lastName.getText());
            newPerson.setDob(d);
            newPerson.setAge(LocalDate.now().getYear() - year);

            int id = new PersonGateway(SessionGateway.getInstance().getSessionID()).insertPerson(newPerson,SessionGateway.getInstance().getSessionID());

            //System.out.println("From person detail controller: " + id);
            //newPerson.setId(id);
            //PersonListController.allPersons = FXCollections.observableArrayList();
            //PersonListController.getInstance().allPersons.add(newPerson);
            //PersonListController.getInstance().personListView.setItems(PersonListController.getInstance().allPersons);
            //PersonListController.people.add(newPerson);
            //try{
                //SessionGateway.getInstance().getPeople().add(newPerson);
           // }catch (Exception e){
           //     logger.debug("Cannot add person to Session");
            //    e.printStackTrace();
            //}

            logger.debug("CREATING {} {}", firstName.getText(), lastName.getText());

            ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
        }
        else {

            // if not empty
            //if (firstName.getText() != null || lastName.getText() != null)
            //{
                person.setPersonName(firstName.getText(), lastName.getText());
            //}
            //if (dob.getText() != null)
            //{
                String date = dob.getText();
                // month day year
                String[] dateArr = date.split("-");
                int month = Integer.parseInt(dateArr[0]);
                int day = Integer.parseInt(dateArr[1]);
                int year = Integer.parseInt(dateArr[2]);
                person.setDob(LocalDate.of(year, month, day));


            //}
            //if (age.getText() != null)
            //{
                //String date = dob.getText();
                // month day year
                person.setAge(LocalDate.now().getYear() - year);
            //}
            boolean updatable;
            updatable = new PersonGateway(SessionGateway.getInstance().getSessionID()).updatePerson(SessionGateway.getInstance().getSessionID(), person);

            if(updatable){
                logger.debug("UPDATING {} {}",firstName.getText(), lastName.getText());
                // update time
                new PersonGateway(SessionGateway.getInstance().getSessionID()).send_UpdateLastModified(SessionGateway.getInstance().getSessionID(),person, person.getId());

                ViewSwitcher.getInstance().switchView(ViewType.PersonDetailView);
            }
            else{
                System.out.println("*************Cant update that!!!");
                ViewSwitcher.getInstance().switchView(ViewType.SaveError);
            }


        }

    }

    @FXML
    void back(){
        ViewSwitcher.getInstance().switchView(ViewType.PersonListView);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (person != null)
        {
            logger.debug("READING {} {}", person.getFirstName(), person.getLastName());
            id.setText(Integer.toString( person.getId()));
            firstName.setText(person.getFirstName());
            lastName.setText(person.getLastName());
            dob.setText(person.getDob().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
            age.setText(Integer.toString( person.getAge()));

        }

        auditTrailTableView.getItems().clear();


        try{
            audit_list = new PersonGateway(SessionGateway.getInstance().getSessionID()).fetch_Audits(SessionGateway.getInstance().getSessionID(),SessionGateway.getInstance().getUserID(), person.getId());

            SessionGateway.getInstance().setAudits(audit_list);

            allAudits.addAll(audit_list);

            date.setCellValueFactory(new PropertyValueFactory<AuditTrail, String>("when_occurred"));
            by.setCellValueFactory(new PropertyValueFactory<AuditTrail, String>("username"));
            desc.setCellValueFactory(new PropertyValueFactory<AuditTrail, String>("change_msg"));
            //auditTrailTableView.getColumns().addAll(date);

        } catch (Exception e){
            logger.debug("Couldn't retrieve");
            e.printStackTrace();
        }

        auditTrailTableView.setItems(allAudits);
    }
}
