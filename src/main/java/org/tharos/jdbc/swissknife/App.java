package org.tharos.jdbc.swissknife;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.core.DatabaseMetadataExtractor;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.DaoPatternStrategy;

public class App {

  private static Logger LOGGER = LogManager.getLogger(App.class.getName());

  public static void main(String[] args) {
    LOGGER.info("App - IN");

    DatabaseMetadataExtractor dbme = new DatabaseMetadataExtractor(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      "postgres#01",
      "simone"
    );

    ArrayList<Table> tableList = dbme.getTablesList();
    try {
      // new GenericGenerator(
      //   new File("C:\\workspaces\\jdbc-swiss-knife\\gen"),
      //   new DaoPatternStrategy(),
      //   tableList,
      //   "geoapi_t",
      //   "com.tharos.jdbc.swissknife.out"
      // )
      //   .executeStrategies();
      new DaoPatternStrategy()
      .generate(
          tableList.get(0),
          "com.tharos.jdbc.swissknife.out",
          new File("C:\\workspaces\\jdbc-swiss-knife\\gen")
        );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
