package backend.services;

import backend.db.DBConnect;
import backend.gateway.GatewayException;
import backend.gateway.LoginGatewayMySQL;
import backend.gateway.PersonGatewayMySQL;
import model.FetchResults;
import model.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
public class PersonControllerServiceJDBC {
    private static final Logger logger = LogManager.getLogger();
    private Connection connection;

    // create a connection on startup
    @PostConstruct
    public void startup() {
        try {
            connection = DBConnect.connectToDB();
            logger.info("*** MySQL connection created");
        } catch (SQLException | IOException e){
            logger.info("*** " + e);
        }
    }

    //close a connection on shutdown
    @PreDestroy
    public void cleanup(){
        try {
            connection.close();
            logger.info("*** MySQL connection closed");
        } catch (SQLException e){
          logger.error("*** " + e);
        }
    }



    @GetMapping(value = "/people", produces = "application/json")
    public ResponseEntity<String> fetchPeople(@RequestHeader Map<String, String> headers, @RequestParam(defaultValue = "0") int pageNum, @RequestParam(defaultValue = "") String lastName) throws SQLException {
        // check if token is real
        if(!validSessionToken(headers)){
            return new ResponseEntity<String>("{\"Authorization needed.\"}", HttpStatus.UNAUTHORIZED);
        }

        FetchResults results = new FetchResults();
        results.setCurrentPage(pageNum);



        try {
            results = new PersonGatewayMySQL(connection).fetchPeople(results, lastName);
            JSONObject obj = new JSONObject(results);

            return new ResponseEntity<String>(obj.toString(), HttpStatus.OK);
        } catch(GatewayException e) {
            return new ResponseEntity<String>("{\"Nothing in database.\"}", HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping(value = "/people", produces = "application/json")
    public ResponseEntity<String> insertPerson(@RequestBody Map <String, String> body, @RequestHeader Map<String, String> headers) throws SQLException {
        // check if token is real
        if(!validSessionToken(headers)){
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<String> status = new PersonGatewayMySQL(connection).insertPerson(body);

        if (status.getStatusCode().equals(HttpStatus.UNAUTHORIZED))
            return new ResponseEntity<String>("{\"Cannot create person no Authorization\"}", HttpStatus.UNAUTHORIZED);
        else if (status.getStatusCode().equals(HttpStatus.BAD_REQUEST))
            return new ResponseEntity<String>("{\"Cannot create person bad request\"}", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<String>(HttpStatus.OK);
    }


    @GetMapping(value = "/people/{id}", produces = "application/json")
    public ResponseEntity<String> fetchPerson(@PathVariable("id") int id, @RequestHeader Map<String, String> headers) throws SQLException {

        if(!validSessionToken(headers)){
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }

        try{
            Person person = new PersonGatewayMySQL(connection).fetchPerson(id);
            JSONObject obj = new JSONObject();
            obj.put("id", person.getId());
            obj.put("firstName", person.getFirstName());
            obj.put("lastName", person.getLastName());
            obj.put("dateOfBirth", person.getDob());
            obj.put("age", person.getAge());
            obj.put("last_modified", person.getLastModified());

            return new ResponseEntity<String>(obj.toString(), HttpStatus.OK);

        } catch(GatewayException e){
            return new ResponseEntity<String>("{\"Person " + id + " not found\"}", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/people/{id}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> updatePerson(@PathVariable("id") int id, @RequestBody Map<String,String> body, @RequestHeader Map<String, String> headers) throws SQLException {

        if(!validSessionToken(headers)){
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }

        try{
            ResponseEntity<String> status = new PersonGatewayMySQL(connection).updatePerson(id, body, headers.get("authorization"));
            if (status.getStatusCode().equals(HttpStatus.NOT_FOUND))
                return new ResponseEntity<String>("{\"Cannot update person " + id + " not found\"}", HttpStatus.NOT_FOUND);
            else if (status.getStatusCode().equals(HttpStatus.UNAUTHORIZED))
                return new ResponseEntity<String>("{\"Cannot update person " + id + " no Authorization\"}", HttpStatus.NOT_FOUND);
            else if (status.getStatusCode().equals(HttpStatus.BAD_REQUEST))
                return new ResponseEntity<String>("{\"Cannot update person " + id + " bad request\"}", HttpStatus.BAD_REQUEST);

            return new ResponseEntity<String>(HttpStatus.OK);
        } catch(GatewayException e){
            return new ResponseEntity<String>("{\"Cannot update person " + id + " not found!\"}", HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/people/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<String> deletePerson(@PathVariable("id") int id, @RequestHeader Map<String, String> headers) throws SQLException {
        if(!validSessionToken(headers)){
            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        }
        try{
            new PersonGatewayMySQL(connection).deletePerson(id);

            return new ResponseEntity<String>(HttpStatus.OK);

        } catch(GatewayException e){
            return new ResponseEntity<String>("{\"Cannot delete person " + id + " not found\"}", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/people/{id}/audittrail", produces = "application/json")
    public ResponseEntity<String> fetchAuditTrailbyID(@PathVariable("id") int id, @RequestHeader Map<String, String> headers) throws SQLException {

        // check if token is real
        if(!validSessionToken(headers)){
            return new ResponseEntity<String>("{\"Authorization needed.\"}", HttpStatus.UNAUTHORIZED);
        }

        try {
            List<JSONObject> objs = new PersonGatewayMySQL(connection).fetchPeopleAudit(id);

            return new ResponseEntity<String>(objs.toString(), HttpStatus.OK);
        } catch(GatewayException e) {
            return new ResponseEntity<String>("{\"Nothing in database.\"}", HttpStatus.NOT_FOUND);
        }
    }


    @RequestMapping(value = "/test", produces = "application/json")
    public String fetchTest(){
        return "{\"Greetings from Spring Boot!\"}";
    }

    private boolean validSessionToken(Map<String, String> headers) throws SQLException {

        for(String key : headers.keySet()){
            if (key.equalsIgnoreCase("Authorization"))
            {
                return new LoginGatewayMySQL(connection).checkSessionToken(headers.get(key));
            }
        }
        return false;
    }

    @PostMapping(value = "/who", produces = "application/json")
    public ResponseEntity<String> whoIsUser(@RequestBody Map <String, String> body) throws SQLException {

        try{
            String username = new PersonGatewayMySQL(connection).whoIsUser(Integer.parseInt(body.get("id")));
            JSONObject obj = new JSONObject();
            obj.put("username", username);

            return new ResponseEntity<String>(obj.toString(), HttpStatus.OK);
        } catch(GatewayException e) {
            return new ResponseEntity<String>("{\"Nothing found.\"}", HttpStatus.NOT_FOUND);
        }
    }
}
