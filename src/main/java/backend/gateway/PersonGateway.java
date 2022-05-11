package backend.gateway;

import controller.PersonException;
import model.AuditTrail;
import model.FetchResults;
import model.Person;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PersonGateway {
    private static final String WS_URL = "http://localhost:8080";
    private static final Logger logger = LogManager.getLogger();
    private String sessionID;

    public PersonGateway(String sessionKEY){
        sessionID = sessionKEY;
    }

    // Create
    public int insertPerson(Person person,String authToken) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        int status_code = 0;

        client = HttpClients.createDefault();

        response = send_InsertRequest(client, authToken, person);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {}", status_code);
                logger.debug("{} added by {}", person.getFirstName(), SessionGateway.getInstance().getUsername());
                return 200;//extract_InsertRequest(response);
            case 400:
                logger.debug("Status code {}", status_code);
                break;
        }

        return -1;
    }

    private static CloseableHttpResponse send_InsertRequest(CloseableHttpClient client, String sessionID, Person person) throws IOException {
        HttpPost POST = new HttpPost(WS_URL + "/people");
        POST.setHeader("Authorization", sessionID);
        JSONObject created_person = new JSONObject();
        created_person.put("firstName", person.getFirstName());
        created_person.put("lastName", person.getLastName());
        created_person.put("dateOfBirth", person.getDob().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
        StringEntity entity = new StringEntity(created_person.toString());
        POST.setHeader("Content-type", "application/json");
        POST.setEntity(entity);
        CloseableHttpResponse response = client.execute(POST);

        return response;
    }

    private int extract_InsertRequest(CloseableHttpResponse response) throws IOException{
        HttpEntity entity = response.getEntity();
        JSONObject jsonObj = new JSONObject(EntityUtils.toString(entity, StandardCharsets.UTF_8));

        return jsonObj.getInt("id");
    }

    // Read
    public ArrayList<Person> fetch_People(String authToken) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        int status_code = 0;

        client = HttpClients.createDefault();

        response = send_FetchRequest(client, authToken);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {}", status_code);
                return extract_FetchRequest(response);
            case 401:
                logger.debug("Status code {}", status_code);
                throw new PersonException("Unauthorized");
        }
        return null;
    }

    private static CloseableHttpResponse send_FetchRequest(CloseableHttpClient client, String sessionID) throws IOException {
        HttpGet GET = new HttpGet(WS_URL + "/people");
        GET.setHeader("Authorization", sessionID);
        CloseableHttpResponse response = client.execute(GET);

        return response;
    }
    private static ArrayList<Person> extract_FetchRequest(CloseableHttpResponse response) throws IOException{
        HttpEntity entity = response.getEntity();
        String JSON_String = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        ArrayList<Person> people_list = new ArrayList<>();

        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for(Object obj: new JSONArray(JSON_String)){
            JSONObject jsonObject = (JSONObject) obj;
            people_list.add(new Person(jsonObject.getInt("id"), jsonObject.getString("firstName"), jsonObject.getString("lastName"), LocalDate.parse(jsonObject.getString("dateOfBirth")), jsonObject.getInt("age"),
                    LocalDateTime.parse(jsonObject.getString("last_modified"))));
        }

        return people_list;
    }

    // Update
    public boolean updatePerson(String authToken, Person person) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        int status_code = 0;

        client = HttpClients.createDefault();

        response = send_UpdateRequest(client, authToken, person, person.getId());

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {} updated", status_code);
                return true;
            case 400:
                logger.debug("Status code {}", status_code);
                return false;
            case 401:
                logger.debug("Status code {}", status_code);
                return false;
            case 404:
                logger.debug("Status code {}", status_code);
                return false;
        }

        return false;
    }

    private static CloseableHttpResponse send_UpdateRequest(CloseableHttpClient client, String sessionID, Person person, int id) throws IOException {
        HttpGet GET = new HttpGet(WS_URL + "/people/" + id);
        GET.setHeader("Authorization", sessionID);
        CloseableHttpResponse response = client.execute(GET);
        LocalDateTime currTime;
        currTime = LocalDateTime.parse(extract_UpdateRequest(response));

        if(currTime.isAfter(person.getLastModified())){
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            return response;
        }

        HttpPut PUT = new HttpPut(WS_URL + "/people/" + id);
        PUT.setHeader("Authorization", sessionID);
        PUT.setHeader("Content-type", "application/json");
        JSONObject obj = new JSONObject();
        obj.put("firstName", person.getFirstName());
        obj.put("lastName", person.getLastName());
        obj.put("dateOfBirth", person.getDob().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
        StringEntity entity = new StringEntity(obj.toString());
        PUT.setEntity(entity);

        return client.execute(PUT);
    }

    private static String extract_UpdateRequest(CloseableHttpResponse response) throws IOException{
        HttpEntity entity = response.getEntity();
        JSONObject jsonObj = new JSONObject(EntityUtils.toString(entity, StandardCharsets.UTF_8));

        return jsonObj.getString("last_modified");
    }

    // Delete
    public void deletePerson(String authToken, int id) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        int status_code = 0;

        client = HttpClients.createDefault();

        response = send_DeleteRequest(client, authToken, id);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {} deleted!", status_code);
                break;
            case 401:
                logger.debug("Status code {}", status_code);
                break;
            case 404:
                logger.debug("Status code {}", status_code);
                break;
        }
    }

    private static CloseableHttpResponse send_DeleteRequest(CloseableHttpClient client, String sessionID, int id) throws IOException {
        HttpDelete DELETE = new HttpDelete(WS_URL + "/people/" + id);
        DELETE.setHeader("Authorization", sessionID);

        return client.execute(DELETE);
    }

    public ArrayList<AuditTrail> fetch_Audits(String authToken, int userid, int personID) throws IOException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        int status_code = 0;

        client = HttpClients.createDefault();

        response = send_FetchAuditRequest(client, authToken, personID);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {}", status_code);
                return extract_FetchAuditRequest(response);
            case 401:
                logger.debug("Status code {}", status_code);
                throw new PersonException("Unauthorized");
        }
        return null;
    }

    private static CloseableHttpResponse send_FetchAuditRequest(CloseableHttpClient client, String sessionID, int personID) throws IOException {
        HttpGet GET = new HttpGet(WS_URL + "/people/" + personID + "/audittrail");
        GET.setHeader("Authorization", sessionID);
        CloseableHttpResponse response = client.execute(GET);

        return response;
    }
    private static ArrayList<AuditTrail> extract_FetchAuditRequest(CloseableHttpResponse response) throws IOException{
        HttpEntity entity = response.getEntity();
        String JSON_String = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        ArrayList<AuditTrail> audit_list = new ArrayList<>();

        for(Object obj: new JSONArray(JSON_String)){
            JSONObject jsonObject = (JSONObject) obj;
            audit_list.add(new AuditTrail(jsonObject.getString("change_msg"), jsonObject.getInt("changed_by"), jsonObject.getInt("person_id"), jsonObject.getString("when_occurred"),
                    SessionGateway.getInstance().sendWhoIsRequest(jsonObject.getInt("changed_by"))));
        }

        return audit_list;
    }

    public void send_UpdateLastModified(String sessionID, Person person, int id) throws IOException {
        CloseableHttpClient client = null;
        client = HttpClients.createDefault();
        HttpGet GET = new HttpGet(WS_URL + "/people/" + id);
        GET.setHeader("Authorization", sessionID);
        CloseableHttpResponse response = client.execute(GET);
        LocalDateTime currTime;
        currTime = LocalDateTime.parse(extract_UpdateRequest(response));

        person.setLastModified(currTime);
    }

    public FetchResults fetch_People_Search(String authToken, String criteria, int pageNum) throws IOException, URISyntaxException {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        int status_code = 0;

        client = HttpClients.createDefault();

        response = send_FetchRequest_Search(client, authToken, criteria, pageNum);

        status_code = response.getStatusLine().getStatusCode();

        switch (status_code){
            case 200:
                logger.debug("Status code {}", status_code);
                return extract_FetchRequest_Search(response);
            case 401:
                logger.debug("Status code {}", status_code);
                throw new PersonException("Unauthorized");
        }
        return null;
    }

    private static CloseableHttpResponse send_FetchRequest_Search(CloseableHttpClient client, String sessionID, String criteria, int pageNum) throws IOException, URISyntaxException {
        URIBuilder builder = new URIBuilder(WS_URL + "/people");
        builder.setParameter("pageNum", Integer.toString(pageNum)).setParameter("lastName", criteria);
        HttpGet GET = new HttpGet(builder.build());

        GET.setHeader("Authorization", sessionID);
        GET.setHeader("content-type", "application/json");

        return client.execute(GET);
    }

    private static FetchResults extract_FetchRequest_Search(CloseableHttpResponse response) throws IOException{
        HttpEntity entity = response.getEntity();
        String JSON_String = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        ArrayList<Person> people_list = new ArrayList<>();
        FetchResults results = new FetchResults();
        JSONObject jsonObject = new JSONObject(JSON_String);

        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        results.setNumRows(jsonObject.getInt("numRows"));
        results.setPageSize(jsonObject.getInt("pageSize"));
        results.setCurrentPage(jsonObject.getInt("currentPage"));

        for(Object obj: jsonObject.getJSONArray("people")){
            JSONObject o = (JSONObject) obj;
            Person person = new Person(o.getInt("id"),
                                       o.getString("firstName"),
                                       o.getString("lastName"),
                                       LocalDate.parse(o.getString("dob")),
                                       o.getInt("age"),
                                       LocalDateTime.parse(o.getString("lastModified")));
            results.addPerson(person);
        }

        return results;
    }
}
