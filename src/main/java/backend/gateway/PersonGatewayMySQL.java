package backend.gateway;

import model.AuditTrail;
import model.FetchResults;
import model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonGatewayMySQL {
    private final Connection connection;
    private static final Logger logger = LogManager.getLogger();
    private static final SessionGateway instance = SessionGateway.getInstance();
    public PersonGatewayMySQL(Connection connection) {
        this.connection = connection;
    }

    private int sizeOfResponse(String search){
        PreparedStatement st = null;
        ResultSet rows = null;
        try{
            st = connection.prepareStatement("select count(*) from People where lastName like ?", PreparedStatement.RETURN_GENERATED_KEYS );
            st.setString(1, search);
            rows = st.executeQuery();
            rows.first();
            return rows.getInt("count(*)");
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            try{
                if(rows != null)
                    rows.close();
                if(st != null)
                    st.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    public FetchResults fetchPeople(FetchResults results, String search) throws GatewayException {
        search += "%";

        ArrayList<Person> people = new ArrayList<Person>();
        List<JSONObject> jsonPeople = new ArrayList<JSONObject>();

        PreparedStatement st = null;
        ResultSet rows = null;

        try {
            results.setNumRows(sizeOfResponse(search));
            st = connection.prepareStatement("select * from People where lastName like ? Order by id Limit ?,?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setString(1,search);
            st.setInt(2, results.getPageSize() * results.getCurrentPage());
            st.setInt(3, results.getPageSize());
            rows = st.executeQuery();
            LocalDate d;
            LocalDateTime l;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            while (rows.next()) {
                d = LocalDate.parse(rows.getString("dateOfBirth"));
                l = LocalDateTime.parse(rows.getString("last_modified"), formatter);

                int age;
                age = LocalDate.now().getYear() - d.getYear();

                Person person = new Person(rows.getInt("id"),
                        rows.getString("firstName"),
                        rows.getString("lastName"),
                        d,
                        age,
                        l);

                people.add(person);
                JSONObject personJs = new JSONObject();
                personJs.put("id", person.getId());
                personJs.put("firstName", person.getFirstName());
                personJs.put("lastName", person.getLastName());
                personJs.put("dateOfBirth", person.getDob());
                personJs.put("age", person.getAge());
                personJs.put("last_modified", person.getLastModified());


                jsonPeople.add(personJs);
                results.addPerson(person);
            }

            return results;

        } catch (SQLException e1) {
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(rows != null)
                    rows.close();
                if(st != null)
                    st.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }


    public Person fetchPerson(int personID) throws GatewayException{
        PreparedStatement st = null;
        ResultSet rows = null;

        try {
            st = connection.prepareStatement("select * from People where id = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(1, personID);
            rows = st.executeQuery();
            rows.first();
            LocalDate d;
            LocalDateTime l;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            d = LocalDate.parse(rows.getString("dateOfBirth"));
            l = LocalDateTime.parse(rows.getString("last_modified"), formatter);


            Person person = new Person(rows.getInt("id"),
                                       rows.getString("firstName"),
                                       rows.getString("lastName"),
                                       d,
                                       rows.getInt("age"),
                                       l);
            return person;
        } catch (SQLException e1){
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(rows != null)
                    rows.close();
                if(st != null)
                    st.close();
            } catch(SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    public ResponseEntity<String> updatePerson(int id, Map<String, String> changes, String key) throws GatewayException {

        Person updatingPerson = fetchPerson(id);
        String currFname = updatingPerson.getFirstName(), currLname = updatingPerson.getLastName(), currDob = updatingPerson.getDob().toString();

        PreparedStatement st = null;
        int res;
        boolean p1 = false,p2 = false,p3 = false;

        try {
            st = connection.prepareStatement("update People set firstName = ?, lastName = ?, dateOfBirth = ?, age = ? where id = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);

            for(String param : changes.keySet()){

                if (param.equals("firstName")) {
                    updatingPerson.setPersonName(changes.get(param), updatingPerson.getLastName());
                    updatingPerson.setFirstName(changes.get(param));
                    p1 = true;
                }
                if (param.equals("lastName")) {
                    updatingPerson.setPersonName(updatingPerson.getFirstName(), changes.get(param));
                    updatingPerson.setLastName(changes.get(param));
                    p2 = true;
                }
                if (param.equals("dateOfBirth")){
                    System.out.println(changes);
                    String date = changes.get(param);
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
                        return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
                    }
                    else{
                        d = LocalDate.of(year, month, day);
                    }
                    updatingPerson.setDob(d);
                    p3 = true;
                }
            }

            String date = updatingPerson.getDob().toString();
            // month day year
            String[] dateArr = date.split("-");
            int month = Integer.parseInt(dateArr[1]);
            int day = Integer.parseInt(dateArr[2]);
            int year = Integer.parseInt(dateArr[0]);
            updatingPerson.setAge(LocalDate.now().getYear() - year);

            st.setString(1, updatingPerson.getFirstName());
            st.setString(2, updatingPerson.getLastName());
            st.setString(3, updatingPerson.getDob().toString());
            st.setInt(4, updatingPerson.getAge());
            st.setInt(5, id);
            res = st.executeUpdate();

            if (res > 0){
                PreparedStatement st3 = connection.prepareStatement("insert into Audit (change_msg, changed_by, person_id) values(?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

                // TODO: add correct change by here
                PreparedStatement st4 = connection.prepareStatement("select * from Sessions where session_id = ?", PreparedStatement.RETURN_GENERATED_KEYS);
                System.out.println(key);
                st4.setString(1, key);
                ResultSet rows = null;
                rows = st4.executeQuery();
                rows.first();
                instance.setUsername(rows.getString("username"));

                PreparedStatement st5 = connection.prepareStatement("select * from Users where username = ?", PreparedStatement.RETURN_GENERATED_KEYS);
                st5.setString(1, instance.getUsername());
                ResultSet rows2 = null;
                rows2 = st5.executeQuery();
                rows2.first();
                instance.setUserID(rows2.getInt("id"));

                // Firstname changed
                if (!currFname.equals(updatingPerson.getFirstName())){
                    st3.setString(1,"firstName changed from " + currFname + " to " + updatingPerson.getFirstName());
                    st3.setInt(2, instance.getUserID());
                    st3.setInt(3, updatingPerson.getId());
                    st3.executeUpdate();
                }
                // Lastname changed
                if (!currLname.equals(updatingPerson.getLastName())){
                    st3.setString(1,"lastName changed from " + currLname + " to " + updatingPerson.getLastName());
                    st3.setInt(2, instance.getUserID());
                    st3.setInt(3, updatingPerson.getId());
                    st3.executeUpdate();
                }
                // date of birth changed
                if (!currDob.equals(updatingPerson.getDob().toString())){
                    st3.setString(1,"dateOfBirth changed from " + currDob + " to " + updatingPerson.getDob().toString());
                    st3.setInt(2, instance.getUserID());
                    st3.setInt(3, updatingPerson.getId());
                    st3.executeUpdate();
                }



            }


        } catch (SQLException e1) {
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(st != null)
                    st.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        return new ResponseEntity<String>(HttpStatus.OK);
    }


    public void deletePerson(int personID) throws GatewayException{
        PreparedStatement st = null;

        try {
            st = connection.prepareStatement("delete from People where id = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(1, personID);
            int r = st.executeUpdate();
            if (r == 0){
                throw new SQLException();
            }


        } catch (SQLException e1){
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(st != null)
                    st.close();
            } catch(SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    public ResponseEntity<String> insertPerson(Map<String, String> body) {
        Person newPerson = new Person();

        PreparedStatement st = null;
        int res;
        boolean p1 = false, p2 = false, p3 = false;

        // Validating the input JSON
        for(String key : body.keySet()){

            if(body.get(key).isEmpty()){
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            switch (key) {
                case "firstName":
                    p1 = true;
                    break;
                case "lastName":
                    p2 = true;
                    break;
                case "dateOfBirth":
                    p3 = true;
                    break;
            }
        }

        // Check all param validations
        if (!p1 || !p2 || !p3){
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        try {

            st = connection.prepareStatement("insert into People (firstName, lastName, dateOfBirth, age) values(?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);

            newPerson.setPersonName(body.get("firstName"), body.get("lastName"));

            String date = body.get("dateOfBirth");

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
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }
            else{
                d = LocalDate.of(year, month, day);
            }

            newPerson.setDob(d);
            newPerson.setAge(LocalDate.now().getYear() - year);

            st.setString(1, newPerson.getFirstName());
            st.setString(2, newPerson.getLastName());
            st.setString(3, newPerson.getDob().toString());
            st.setInt(4, newPerson.getAge());
            res = st.executeUpdate();

            if (res > 0){
                PreparedStatement st2 = null;
                ResultSet rows = null;

                st2 = connection.prepareStatement("select * from People", PreparedStatement.RETURN_GENERATED_KEYS);
                rows = st2.executeQuery();
                if (rows.last()){
                    PreparedStatement st3 = connection.prepareStatement("insert into Audit (change_msg, changed_by, person_id) values(?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    st3.setString(1,"added");
                    st3.setInt(2, instance.getUserID());
                    st3.setInt(3, rows.getInt("id"));
                    st3.executeUpdate();
                }

            }

        } catch (SQLException e1) {
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(st != null)
                    st.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        return new ResponseEntity<String>(HttpStatus.OK);

    }

    public List<JSONObject> fetchPeopleAudit(int id) throws GatewayException {
        ArrayList<AuditTrail> audits = new ArrayList<AuditTrail>();
        List<JSONObject> jsonAudit = new ArrayList<JSONObject>();

        PreparedStatement st = null;
        ResultSet rows = null;

        try {
            st = connection.prepareStatement("select * from Audit where person_id = ?", PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(1,id);
            rows = st.executeQuery();

            while (rows.next()) {

                AuditTrail audit = new AuditTrail(rows.getString("change_msg"),
                        rows.getInt("changed_by"),
                        rows.getInt("person_id"),
                        rows.getString("when_occurred"),
                        SessionGateway.getInstance().getUsername());

                audits.add(audit);

                JSONObject auditJs = new JSONObject();
                auditJs.put("change_msg", audit.getChange_msg());
                auditJs.put("changed_by", audit.getChanged_byID());
                auditJs.put("person_id", audit.getPerson_ID());
                auditJs.put("when_occurred", audit.getWhen_occurred());

                jsonAudit.add(auditJs);
            }

            return jsonAudit;

        } catch (SQLException e1) {
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(rows != null)
                    rows.close();
                if(st != null)
                    st.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    public String whoIsUser(int id) throws GatewayException{
        PreparedStatement st = null;
        ResultSet rows = null;

        try {
            st = connection.prepareStatement("select * from Users where id = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            st.setInt(1, id);
            rows = st.executeQuery();
            rows.first();

            return rows.getString("username");
        } catch (SQLException e1){
            e1.printStackTrace();
            throw new GatewayException(e1);
        } finally {
            try {
                if(rows != null)
                    rows.close();
                if(st != null)
                    st.close();
            } catch(SQLException e2) {
                e2.printStackTrace();
            }
        }
    }
}
