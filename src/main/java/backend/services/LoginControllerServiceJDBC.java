package backend.services;

import backend.db.DBConnect;
import backend.gateway.LoginGatewayMySQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class LoginControllerServiceJDBC {
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

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body){
        LoginGatewayMySQL session = new LoginGatewayMySQL(connection);

        ResponseEntity<Integer> status = session.authenticate(body);

        if (status.getStatusCode().equals(HttpStatus.UNAUTHORIZED))
            return new ResponseEntity<String>("{\"Not found.\": 404}", HttpStatus.NOT_FOUND);

        JSONObject authToken = new JSONObject();
        authToken.put("Authorization", status.getBody()+session.getSessionID());

        return new ResponseEntity<String>(authToken.toString(), HttpStatus.OK);
    }
}
