package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class SpringConfigurationGenerator {

  private String basePackage;
  private File outputFolder;
  private ArrayList<Table> tableList;

  public SpringConfigurationGenerator(
    ArrayList<Table> tableList,
    String basePackage,
    File outputFolder
  ) {
    this.basePackage = basePackage;
    this.outputFolder = outputFolder;
    this.tableList = tableList;
  }

  public void generateSpringConfigurationTypeSpec() throws IOException {
    Builder springConfigSpecBuilder = TypeSpec
      .classBuilder("SpringConfig")
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(GeneratorUtils.generateAnnotation(Configuration.class))
      .addMethod(generateDatasourceMethod())
      .addMethod(generateJdbcTemplateMethod(generateDatasourceMethod()));
    for (Table table : this.tableList) {
      springConfigSpecBuilder.addMethod(generateDaoMethod(table)); //TODO
    }
    TypeSpec springConfigutartion = springConfigSpecBuilder.build();
    JavaFile springConfigJavaFile = JavaFile
      .builder(this.basePackage.concat(".config"), springConfigutartion)
      .indent("    ")
      .build();
    springConfigJavaFile.writeTo(outputFolder);
  }

  private MethodSpec generateDatasourceMethod() {
    MethodSpec datasourceMethod = MethodSpec
      .methodBuilder("datasource")
      .addModifiers(Modifier.PUBLIC)
      .addException(ClassName.get("java.sql", "SQLException"))
      .addAnnotation(GeneratorUtils.generateAnnotation(Bean.class))
      .addStatement(
        "$T datasource = new $T()",
        DriverManagerDataSource.class,
        DriverManagerDataSource.class
      )
      .returns(ClassName.get(DataSource.class))
      .addStatement("datasource.setDriverClassName(\"org.postgresql.Driver\")")
      .addStatement("datasource.setUrl(\"TODO\")")
      .addStatement("datasource.setSchema(\"TODO\")")
      .addStatement("datasource.setUsername(\"TODO\")")
      .addStatement("datasource.setPassword(\"TODO\")")
      .addStatement("return datasource")
      .build();
    return datasourceMethod;
  }

  private MethodSpec generateJdbcTemplateMethod(MethodSpec datasource) {
    MethodSpec jdbcTemplateMethod = MethodSpec
      .methodBuilder("jdbcTemplate")
      .addModifiers(Modifier.PUBLIC)
      .addException(ClassName.get("java.sql", "SQLException"))
      .addAnnotation(GeneratorUtils.generateAnnotation(Bean.class))
      .returns(ClassName.get(NamedParameterJdbcTemplate.class))
      .addStatement(
        "return new $T($N())",
        DriverManagerDataSource.class,
        datasource
      )
      .build();
    return jdbcTemplateMethod;
  }

  private MethodSpec generateDaoMethod(Table table) {
    MethodSpec daoMethod = MethodSpec
      .methodBuilder(
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(
          table.getName()
        ) +
        "Dao"
      )
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(GeneratorUtils.generateAnnotation(Bean.class))
      .returns(
        ClassName.get(
          this.basePackage + ".dao.impl",
          GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(
            table.getName()
          ) +
          "DaoImpl"
        )
      )
      .addStatement(
        "return new $T()",
        ClassName.get(
          this.basePackage + ".dao.impl",
          GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(
            table.getName()
          ) +
          "DaoImpl"
        )
      )
      .build();
    return daoMethod;
  }
}
