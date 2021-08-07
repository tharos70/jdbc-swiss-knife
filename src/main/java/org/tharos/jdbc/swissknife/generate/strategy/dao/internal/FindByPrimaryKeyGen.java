package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class FindByPrimaryKeyGen {

  public MethodSpec generateFindByPrimaryKey(
    Table table,
    TypeSpec dto,
    TypeSpec daoException,
    String basePackage,
    String purifiedName
  ) {
    MethodSpec.Builder findByKey = MethodSpec
      .methodBuilder("findByPrimaryKey")
      .addModifiers(Modifier.PUBLIC)
      .returns(
        ClassName.get(
          basePackage + ".dto",
          CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName)
        )
      )
      .addException(
        ClassName.get(basePackage + ".exception", daoException.name)
      );
    for (Column col : table.getPrimaryKeys()) {
      findByKey.addParameter(
        col.getType(),
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName())
      );
    }
    findByKey.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      "DaoImpl:findByPrimaryKey - IN\")"
    );
    findByKey.addStatement(
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      " result = null"
    );
    CodeBlock cbSelectFirstPart = CodeBlock
      .builder()
      .addStatement("StringBuilder sb = new StringBuilder()")
      .addStatement(
        "sb.append(\"SELECT " +
        GeneratorUtils.generateColumnStringListForSQL(table.getColumnList()) +
        "\")"
      )
      .addStatement("sb.append(\"FROM " + table.getName() + "\" )")
      .addStatement("sb.append(\" WHERE \")")
      .build();
    findByKey.addCode(cbSelectFirstPart);
    CodeBlock.Builder cbSelectFilterPart = CodeBlock.builder();
    cbSelectFilterPart.addStatement(
      "sb.append(\"" + generateSQLFilterStringForTablePks(table) + ";\")"
    );

    findByKey.addCode(cbSelectFilterPart.build());
    CodeBlock.Builder cbExecuteQuery = CodeBlock
      .builder()
      .beginControlFlow("try")
      .addStatement(
        "Map<String, Object> params = new HashMap<String, Object>()"
      );
    for (Column pk : table.getPrimaryKeys()) {
      cbExecuteQuery.addStatement(
        "params.put(\"" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        "\" , " +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        ")"
      );
    }
    cbExecuteQuery
      .addStatement(
        "result = jdbcTemplate.queryForObject(sb.toString(), params,  rowMapper ) "
      )
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:findByPrimaryKey -> Record having key [" +
        generateJdbcMappingsLoggingForTablePksValues(table) +
        "] not found\", e)",
        ClassName.get(basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:findByPrimaryKey - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result");
    findByKey.addCode(cbExecuteQuery.build());
    return findByKey.build();
  }

  private String generateSQLFilterStringForTablePks(Table table) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
      Column pk = table.getPrimaryKeys().get(i);
      sb.append(
        pk.getName() +
        " = :" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        (i != table.getPrimaryKeys().size() - 1 ? " AND " : "")
      );
    }
    return sb.toString();
  }

  private String generateJdbcMappingsLoggingForTablePksValues(Table table) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
      Column pk = table.getPrimaryKeys().get(i);
      sb.append(
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        " = \" + " +
        GeneratorUtils.generateToStringStatementAccordingToColumnType(
          pk.getType(),
          pk.getName()
        ) +
        " + \"" +
        ((i < (table.getPrimaryKeys().size() - 1)) ? ", " : "")
      );
    }
    return sb.toString();
  }
}
