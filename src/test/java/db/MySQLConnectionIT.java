package db;

import db.mysql.MySQLConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class MySQLConnectionIT {
    private static MySQLContainer<?> mysql;

    @BeforeAll
    public static void setup() throws Exception {
        mysql = new MySQLContainer<>("mysql:8.0.33")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        mysql.start();

        // expose JDBC settings to DataSourceManager via system properties
        System.setProperty("DB_URL", mysql.getJdbcUrl());
        System.setProperty("DB_USER", mysql.getUsername());
        System.setProperty("DB_PASSWORD", mysql.getPassword());

        DataSource ds = DataSourceManager.getDataSource();
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users (user_id VARCHAR(100) PRIMARY KEY, password VARCHAR(255), first_name VARCHAR(100), last_name VARCHAR(100))");
            s.execute("CREATE TABLE IF NOT EXISTS items (id VARCHAR(200) PRIMARY KEY, name VARCHAR(255))");
            s.execute("CREATE TABLE IF NOT EXISTS item_categories (item_id VARCHAR(200), category VARCHAR(200), PRIMARY KEY(item_id, category))");
            s.execute("CREATE TABLE IF NOT EXISTS history (user_id VARCHAR(100), item_id VARCHAR(200), PRIMARY KEY(user_id, item_id))");
        }
    }

    @AfterAll
    public static void teardown() {
        if (mysql != null) mysql.stop();
    }

    @Test
    public void testRegisterAndVerify() {
        MySQLConnection conn = new MySQLConnection();
        String user = "integration_user";
        // ensure register returns true
        boolean ok = conn.registerUser(user, "Pass1234", "Int", "User");
        assertTrue(ok, "registerUser should return true");
        // verify login
        boolean verified = conn.verifyLogin(user, "Pass1234");
        assertTrue(verified, "verifyLogin should succeed for registered user");
        conn.close();
    }
}
