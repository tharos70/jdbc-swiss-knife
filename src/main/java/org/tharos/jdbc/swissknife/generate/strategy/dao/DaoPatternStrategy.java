package org.tharos.jdbc.swissknife.generate.strategy.dao;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.DaoImplGenerator;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.DtoGen;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.SimpleRowMapperGenerator;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DaoPatternStrategy {

  private String purifiedName;
  private String basePackage;

  public void generate(Table table, String basePackage, File outputFolder)
    throws IOException, IllegalArgumentException, IllegalAccessException {
    this.purifiedName = table.getName();
    this.basePackage = basePackage;
    TypeSpec dto = new DtoGen(purifiedName, table).createDtoTypeSpec();
    JavaFile dtoJavaFile = JavaFile
      .builder(this.basePackage.concat(".dto"), dto)
      .indent("    ")
      .build();
    dtoJavaFile.writeTo(outputFolder);

    TypeSpec rowmapper = new SimpleRowMapperGenerator(
      basePackage,
      purifiedName,
      table
    )
    .createRowmapperTypeSpec(dto);
    JavaFile rowmapperJavaFile = JavaFile
      .builder(
        this.basePackage.concat(".integration.dao.impl.rowmapper"),
        rowmapper
      )
      .indent("    ")
      .build();
    rowmapperJavaFile.writeTo(outputFolder);

    TypeSpec daoException = GeneratorUtils.createExceptionTypeSpec(
      "DaoException"
    );
    JavaFile daoExceptionJavaFile = JavaFile
      .builder(this.basePackage.concat(".excepion"), daoException)
      .indent("    ")
      .build();
    daoExceptionJavaFile.writeTo(outputFolder);

    TypeSpec daoImpl = new DaoImplGenerator(basePackage, purifiedName, table)
    .createDaoImplTypeSpec(dto, rowmapper, daoException);
    JavaFile daoImplJavaFile = JavaFile
      .builder(this.basePackage.concat(".integration.dao.impl"), daoImpl)
      .indent("    ")
      .build();
    daoImplJavaFile.writeTo(outputFolder);
  }
}
