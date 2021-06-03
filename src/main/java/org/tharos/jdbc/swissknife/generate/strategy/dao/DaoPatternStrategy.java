package org.tharos.jdbc.swissknife.generate.strategy.dao;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.DtoGenerator;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.SimpleRowMapperGenerator;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DaoPatternStrategy {

  private String purifiedName;
  private String basePackage;

  public TypeSpec executeInternalStrategy(
    Table table,
    String purifiedName,
    String basePackage
  )
    throws IOException {
    this.purifiedName = purifiedName;
    this.basePackage = basePackage;
    TypeSpec dto = new DtoGenerator(purifiedName, table).createDtoTypeSpec();
    JavaFile dtoJavaFile = JavaFile
      .builder(this.basePackage.concat(".dto"), dto)
      .indent("    ")
      .build();
    dtoJavaFile.writeTo(new File("C:\\workspaces\\jdbc-swiss-knife\\gen")); // TODO portare fuori
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
    rowmapperJavaFile.writeTo(
      new File("C:\\workspaces\\jdbc-swiss-knife\\gen")
    ); // TODO portare fuori
    TypeSpec daoImpl = createDaoImplTypeSpec(dto, rowmapper, table);
    JavaFile daoImplJavaFile = JavaFile
      .builder(this.basePackage.concat(".integration.dao.impl"), daoImpl)
      .indent("    ")
      .build();
    daoImplJavaFile.writeTo(new File("C:\\workspaces\\jdbc-swiss-knife\\gen")); // TODO portare fuori
    return null;
  }

  private TypeSpec createDaoImplTypeSpec(
    TypeSpec dto,
    TypeSpec rowmapper,
    Table table
  ) {
    FieldSpec rowMapperInstance = generateRowMapperInstantiationStatement(
      rowmapper
    );
    FieldSpec loggerInstance = generateLoggerInstantiationStatement(
      this.basePackage + ".dao.impl"
    );

    MethodSpec contructor = generateDaoImplInitializerBlock();
    MethodSpec tableNameGetter = generateTableNameGetter(table.getName());
    MethodSpec sequenceNameGetter = generateSequenceNameGetter(
      table.getSequenceName()
    );

    TypeSpec daoImplType = TypeSpec
      .classBuilder(
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl"
      )
      .superclass(ClassName.get(this.basePackage, "AbstractDao"))
      .addMethod(contructor)
      .addMethod(tableNameGetter)
      .addMethod(sequenceNameGetter)
      .addAnnotation(GeneratorUtils.generateRepositoryAnnotation())
      .addModifiers(Modifier.PUBLIC)
      .addField(rowMapperInstance)
      .addField(loggerInstance)
      .build();
    return daoImplType;
  }

  private MethodSpec generateTableNameGetter(String tableName) {
    return MethodSpec
      .methodBuilder("getTableName")
      .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
      .addModifiers(Modifier.PROTECTED)
      .returns(String.class)
      .addStatement("return \"" + tableName + "\"")
      .build();
  }

  private MethodSpec generateSequenceNameGetter(String sequenceName) {
    return MethodSpec
      .methodBuilder("getSequenceName")
      .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
      .addModifiers(Modifier.PROTECTED)
      .returns(String.class)
      .addStatement("return \"" + sequenceName + "\"")
      .build();
  }

  private MethodSpec generateDaoImplInitializerBlock() {
    return MethodSpec
      .constructorBuilder()
      .addAnnotation(GeneratorUtils.generateAutowiredAnnotation())
      .addModifiers(Modifier.PUBLIC)
      .addParameter(DataSource.class, "datasource")
      .addParameter(NamedParameterJdbcTemplate.class, "jdbcTemplate")
      .addStatement("super(datasource, jdbcTemplate)")
      .build();
  }

  private FieldSpec generateLoggerInstantiationStatement(
    String loggingPackage
  ) {
    FieldSpec loggerInstance = FieldSpec
      .builder(
        ClassName.get("org.apache.log4j", "Logger"),
        "LOG",
        Modifier.PRIVATE,
        Modifier.STATIC,
        Modifier.FINAL
      )
      .initializer("Logger.getLogger(" + loggingPackage + ")")
      .build();
    return loggerInstance;
  }

  private FieldSpec generateRowMapperInstantiationStatement(
    TypeSpec rowmapper
  ) {
    FieldSpec rowMapperInstance = FieldSpec
      .builder(
        ClassName.get(basePackage + ".rowmapper", rowmapper.name),
        "rowMapper",
        Modifier.PRIVATE
      )
      .initializer("new " + rowmapper.name + "()")
      .build();
    return rowMapperInstance;
  }
}
