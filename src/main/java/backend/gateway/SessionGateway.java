package backend.gateway;

import model.AuditTrail;
import model.Person;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SessionGateway {
    private static final String WS_URL = "http://localhost:8080";
    private static final Logger logger = LogManager.getLogger();
    private static SessionGateway instance = null;
    private ArrayList<Person> people;
    private ArrayList<AuditTrail> audits;
    private static String sessionID;
    private static String username = "";
    private static int userID;

    public static SessionGateway getInstance(){
        if (instance == null){
            instance = new SessionGateway();
        }
        return instance;
    }

    public int authenticate(String username, String password) throws IOException {
        this.username = username;
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        int status_code = 0;

        client = HttpClients.createDefault();

        response = sendRequest(client, username, password);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {}", status_code);
                sessionID = extractToken(response);
                return 200;
            case 401:
                logger.debug("Status code {}", status_code);
                return 401;
        }
        return 500;
    }

    private static CloseableHttpResponse sendRequest(CloseableHttpClient client, String username, String password) throws IOException {
        CloseableHttpResponse response = null;

        JSONObject credentials = new JSONObject();
        credentials.put("username", username);
        credentials.put("password", password);
        StringEntity entity = new StringEntity(credentials.toString());

        HttpPost POST = new HttpPost(WS_URL + "/login");
        POST.setHeader("Content-type", "application/json");
        POST.setEntity(entity);


        response = client.execute(POST);

        return response;
    }

    private static String extractToken(CloseableHttpResponse response) throws IOException{
        HttpEntity entity = response.getEntity();
        JSONObject obj;
        String responseToString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        obj = new JSONObject(responseToString);
        // TODO: store toke globally
        //instance.setUserID();
        //System.out.println("This is what substring returns: " + obj.getString("Authorization").charAt(0));
        instance.setUserID(Character.getNumericValue(obj.getString("Authorization").charAt(0)));

        return obj.getString("Authorization").substring(1, obj.getString("Authorization").length());
    }

    public String getSessionID() {
        return sessionID;
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public String getUsername() { return username; }

    public void setUsername(String u) { username = u; }

    public void setPeople(List<Person> people) {
        this.people = new ArrayList<Person>(people);
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        SessionGateway.userID = userID;
    }

    public ArrayList<AuditTrail> getAudits() {
        return audits;
    }

    public void setAudits(List<AuditTrail> audits) throws IOException {
        this.audits = new ArrayList<AuditTrail>(audits);

        String tmp = null;

        for (AuditTrail audit : this.audits) {
            tmp = sendWhoIsRequest(audit.getChanged_byID());
            System.out.println(tmp);
            System.out.println("****************************PREV"+audit.getUsername());
            audit.setUsername(tmp);
            System.out.println("****************************Curr"+audit.getUsername());

        }
    }
    public String sendWhoIsRequest(int ID) throws IOException {
        System.out.println("****************************RAN FUNCTION");
        CloseableHttpClient client;
        CloseableHttpResponse response = null;

        JSONObject credentials = new JSONObject();
        credentials.put("id", ID);
        StringEntity entity = new StringEntity(credentials.toString());

        HttpPost POST = new HttpPost(WS_URL + "/who");
        POST.setHeader("Content-type", "application/json");
        POST.setEntity(entity);

        int status_code = 0;

        client = HttpClients.createDefault();

        response = client.execute(POST);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {}", status_code);
                // TODO: Extract who is the person
                HttpEntity ent = response.getEntity();
                String responseToString = EntityUtils.toString(ent, StandardCharsets.UTF_8);

                JSONObject obj = new JSONObject(responseToString);

                return obj.getString("username");
            case 404:
                logger.debug("Status code {}", status_code);
                return "Error retrieving";
        }
        return "Error";
    }
}
