package db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceManagerTest {

    @Test
    public void testDataSourceNotNull() {
        assertNotNull(DataSourceManager.getDataSource(), "DataSource should not be null");
    }
}
