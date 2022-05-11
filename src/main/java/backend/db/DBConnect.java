package backend.db;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnect {

    public static Connection connectToDB() throws IOException, SQLException {
        //connect to data source and create a connection instance
        Properties props = getConfig("/db.properties");

        //create the datasource
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(props.getProperty("MYSQL_DB_URL"));
        dataSource.setUser(props.getProperty("MYSQL_DB_USERNAME"));
        dataSource.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

        //create the connection
        return dataSource.getConnection();
    }

    private static Properties getConfig(String propsFileName) throws IOException {
        Properties props = new Properties();

        BufferedInputStream propsFile = (BufferedInputStream) DBConnect.class.getResourceAsStream(propsFileName);
        props.load(propsFile);
        propsFile.close();

        return props;
    }
}

