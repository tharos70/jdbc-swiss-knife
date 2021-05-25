package org.tharos.jdbc.swissknife.generate;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.App;
import org.tharos.jdbc.swissknife.dto.Table;

public class GenericGenerator {

  private static Logger LOGGER = LogManager.getLogger(App.class.getName());

  private List<Strategy> strategies = null;
  private List<Table> tables = null;

  private String prefixToExclude = null;

  private String basePackage;

  public GenericGenerator(
    List<Strategy> strategies,
    List<Table> tables,
    String prefixToEsclude,
    String basePackage
  ) {
    this.strategies = strategies;
    this.tables = tables;
    this.prefixToExclude = prefixToEsclude;
    this.basePackage = basePackage;
  }

  public void executeStrategies() throws IOException {
    LOGGER.info("GenericGenerator:executeStrategies - IN");
    for (Strategy strategy : this.strategies) {
      for (Table table : this.tables) {
        strategy.executeStrategy(table, this.prefixToExclude, this.basePackage);
      }
    }
    LOGGER.info("GenericGenerator:executeStrategies - OUT");
  }
}
