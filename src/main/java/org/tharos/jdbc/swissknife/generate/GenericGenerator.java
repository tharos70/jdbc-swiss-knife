package org.tharos.jdbc.swissknife.generate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.App;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.Strategy;
import org.tharos.jdbc.swissknife.generate.strategy.dao.DaoPatternStrategy;

public class GenericGenerator {

  private static Logger LOGGER = LogManager.getLogger(App.class.getName());

  private List<Strategy> strategies = null;
  private List<Table> tables = null;

  private String prefixToExclude = null;

  private String basePackage;

  private File file;

  public GenericGenerator(
    File file,
    List<Table> tables,
    String prefixToEsclude,
    String basePackage
  ) {
    this.file = file;
    this.tables = tables;
    this.prefixToExclude = prefixToEsclude;
    this.basePackage = basePackage;
  }

  public void executeStrategies() throws IOException {
    LOGGER.info("GenericGenerator:executeStrategies - IN");
    for (Table table : this.tables) {
      new DaoPatternStrategy()
      .executeStrategy(
          this.file,
          table,
          this.prefixToExclude,
          this.basePackage
        );
    }
    LOGGER.info("GenericGenerator:executeStrategies - OUT");
  }
}
