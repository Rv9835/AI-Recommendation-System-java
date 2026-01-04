package db.mysql;

import db.DataSourceManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class MySQLConnectionIT {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("recommendation")
            .withUsername("root")
            .withPassword("root");

    @AfterAll
    public static void cleanup() {
        try {
            DataSourceManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void smokeTest_RegisterAndLogin_SaveItem() throws Exception {
        // configure system properties so DataSourceManager picks up the test container
        System.setProperty("DB_URL", mysql.getJdbcUrl());
        System.setProperty("DB_USER", mysql.getUsername());
        System.setProperty("DB_PASSWORD", mysql.getPassword());

        // run flyway migrations against container
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        // exercise DAO
        MySQLConnection conn = new MySQLConnection();
        try {
            boolean ok = conn.registerUser("test_user", "pass1234", "T", "User");
            assertTrue(ok, "registerUser should succeed");
            assertTrue(conn.verifyLogin("test_user", "pass1234"));

            // save an item
            entity.Item item = entity.Item.builder()
                    .setId("item123")
                    .setName("Test Item")
                    .setAddress("123 Test")
                    .setImageUrl("http://example.com/img.png")
                    .setUrl("http://example.com/item")
                    .setLat(10.0)
                    .setLon(20.0)
                    .setDescription("desc")
                    .build();
            conn.saveItem(item);

            // favorite operations
            conn.addFavorites("test_user", java.util.Arrays.asList("item123"));
            java.util.Set<String> favs = conn.getFavoriteIds("test_user");
            assertTrue(favs.contains("item123"));

            conn.removeFavorites("test_user", java.util.Arrays.asList("item123"));
            favs = conn.getFavoriteIds("test_user");
            assertFalse(favs.contains("item123"));
        } finally {
            conn.close();
        }
    }
}
