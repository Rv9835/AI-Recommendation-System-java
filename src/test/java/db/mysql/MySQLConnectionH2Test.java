package db.mysql;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLConnectionH2Test {

    @AfterAll
    public static void cleanup() {
        try {
            db.DataSourceManager.close();
        } catch (Exception ignore) {}
    }

    @Test
    public void h2_inMemory_smokeTest() throws Exception {
        // configure H2 in-memory DB
        String url = "jdbc:h2:mem:recommendation;DB_CLOSE_DELAY=-1";
        System.setProperty("DB_URL", url);
        System.setProperty("DB_USER", "sa");
        System.setProperty("DB_PASSWORD", "");

        // run flyway migrations against H2
        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        MySQLConnection conn = new MySQLConnection();
        try {
            boolean ok = conn.registerUser("h2_user", "password1", "H2", "User");
            assertTrue(ok);
            assertTrue(conn.verifyLogin("h2_user", "password1"));

            // duplicate registration should fail
            Exception ex = null;
            try {
                conn.registerUser("h2_user", "password1", "H2", "User");
            } catch (Exception e) {
                ex = e;
            }
            assertNotNull(ex, "Duplicate register should throw an exception");

            entity.Item item = entity.Item.builder()
                    .setId("h2item")
                    .setName("H2 Item")
                    .setAddress("Nowhere")
                    .setLat(1.0)
                    .setLon(2.0)
                    .setDescription("desc")
                    .build();
            conn.saveItem(item);
            conn.addFavorites("h2_user", java.util.Arrays.asList("h2item"));
            java.util.Set<String> favs = conn.getFavoriteIds("h2_user");
            assertTrue(favs.contains("h2item"));
        } finally {
            conn.close();
        }
    }
}
