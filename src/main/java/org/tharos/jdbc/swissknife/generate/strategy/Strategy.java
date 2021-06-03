package org.tharos.jdbc.swissknife.generate.strategy;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.App;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.DtoStrategy;

public abstract class Strategy {

  protected static Logger LOGGER = LogManager.getLogger(App.class.getName());
  private String name;

  public void executeStrategy(
    File file,
    Table table,
    String prefixToExclude,
    String basePackage
  )
    throws IOException {
    LOGGER.info("Strategy:" + this.getName() + " - IN");
    String purifiedName = CaseFormat.UPPER_UNDERSCORE.to(
      CaseFormat.UPPER_CAMEL,
      StringUtils.removeStart(table.getName(), prefixToExclude)
    );

    TypeSpec dto = new DtoStrategy()
    .generateDtoTypeSpec(table, purifiedName, basePackage);

    TypeSpec type =
      this.executeInternalStrategy(table, purifiedName, basePackage);
    JavaFile javaFile = JavaFile
      .builder(basePackage.concat(getPackagePostfix()), type)
      .indent("    ")
      .build();
    javaFile.writeTo(file);
    LOGGER.info("Strategy:" + this.getName() + " - OUT");
  }

  protected abstract String getPackagePostfix();

  protected abstract TypeSpec executeInternalStrategy(
    Table table,
    String purifiedName,
    String basePackage
  )
    throws IOException;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
