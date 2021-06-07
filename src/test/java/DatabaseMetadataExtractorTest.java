import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tharos.jdbc.swissknife.core.DatabaseMetadataExtractor;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.Constants;

class DatabaseMetadataExtractorTest {

  private static DatabaseMetadataExtractor dbme = null;

  @BeforeAll
  static void initialize() {
    dbme =
      new DatabaseMetadataExtractor(
        Constants.JDBC_DEFAULT_POSTGRESQL_DRIVER,
        "jdbc:postgresql://localhost:5432/postgres",
        "postgres",
        "postgres#01",
        "simone"
      );
  }

  @Test
  void tableListIsNotEmpty() {
    List<Table> tableList = dbme.getTablesList();
    assertTrue(
      !tableList.isEmpty(),
      "Expecting table list not to be empty or null"
    );
    assertTrue(
      !tableList.get(0).getColumnList().isEmpty(),
      "Expecting column list not to be empty"
    );
  }
}
