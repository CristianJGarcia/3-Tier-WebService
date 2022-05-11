package backend.gateway;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class LoginGatewayMySQL {
    //private static LoginGatewayMySQL instance = null;
    private final Connection connection;
    private static String sessionID;
    private static final SessionGateway instance = SessionGateway.getInstance();

    /*public static LoginGatewayMySQL getInstance(Connection connection){
        if (instance == null){
            instance = new LoginGatewayMySQL(connection);
        }
        return instance;
    }*/

    public LoginGatewayMySQL(Connection connection) {
        this.connection = connection;
    }

    public ResponseEntity<Integer> authenticate(Map<String,String> body){
        PreparedStatement st = null;
        ResultSet resultSet;

        try {
            st = connection.prepareStatement("select * from Users where username = ? and password = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);

            st.setString(1, body.get("username"));
            st.setString(2, body.get("password"));
            resultSet = st.executeQuery();
            resultSet.first();
            instance.setUserID(resultSet.getInt("id"));

            // Check if exists if not then return 401
            if (!resultSet.first()){
                return new ResponseEntity<>(HttpStatus.valueOf(401));
            }



            generateSessionId(body.get("username"));

        } catch (SQLException | NoSuchAlgorithmException e1) {
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
        return new ResponseEntity<>(instance.getUserID(), HttpStatus.OK);
    }

    private void generateSessionId(String username) throws NoSuchAlgorithmException, SQLException {
        PreparedStatement st;
        ResultSet resultSet;
        UUID uuid = UUID.randomUUID();
        String salted = uuid.toString() + username;


        try {
            st = connection.prepareStatement("select * from Sessions where username = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);

            st.setString(1, username);
            resultSet = st.executeQuery();

            // Check if exists if not then insert
            if (!resultSet.first()){
                st = connection.prepareStatement("insert into Sessions (session_id, username)  values (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                sessionID = genHash(salted);
                st.setString(1, sessionID);
                st.setString(2, username);
                st.executeUpdate();
            }
            else{
                // Get session token
                sessionID = resultSet.getString("session_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        }
    }

    private static String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedInput = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        BigInteger digestBigInt = new BigInteger(1, hashedInput);
        return digestBigInt.toString(16);
    }

    public String getSessionID(){
        return sessionID;
    }

    public boolean checkSessionToken(String token) throws SQLException {
        PreparedStatement st = null;
        ResultSet resultSet = null;

        if (token.equals(sessionID))
            return true;

        try {
            st = connection.prepareStatement("select * from Sessions where session_id = ?", PreparedStatement.RETURN_GENERATED_KEYS);

            st.setString(1, token);
            resultSet = st.executeQuery();

            // Check if exists
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        } finally {
            try{
                if(resultSet != null)
                    resultSet.close();
                if(st != null)
                    st.close();
            } catch(SQLException e){
                e.printStackTrace();
        }
    }

}
}
