package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.tharos.jdbc.swissknife.core.SQLTypeMap;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class UpdateGen {

  public MethodSpec generateUpdate(
    Table table,
    TypeSpec dto,
    TypeSpec daoException,
    String basePackage,
    String purifiedName
  )
    throws IllegalArgumentException, IllegalAccessException {
    MethodSpec.Builder update = MethodSpec
      .methodBuilder("update")
      .addParameter(ClassName.get(basePackage + ".dto", dto.name), "dto")
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
    update.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      "DaoImpl:update - IN\")"
    );
    update.addStatement(
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      " result = null"
    );
    CodeBlock.Builder updateBlock = CodeBlock
      .builder()
      .addStatement("StringBuilder sb = new StringBuilder()")
      .addStatement("sb.append(\"UPDATE " + table.getName() + " SET \")");
    CodeBlock.Builder cbUpdateColumnsPart = CodeBlock.builder();
    int lineCounter = 0;
    for (Column col : table.getColumnList()) {
      if (!col.isPrimaryKey()) {
        cbUpdateColumnsPart.addStatement(
          "sb.append(\"" +
          col.getName() +
          " = :" +
          GeneratorUtils.generateInstanceNameFromSnakeCaseString(
            col.getName()
          ) +
          (
            lineCounter <
              (table.getColumnList().size() - table.getPrimaryKeys().size() - 1)
              ? ", "
              : ""
          ) +
          " \")"
        );
        lineCounter++;
      }
    }
    CodeBlock.Builder cbUpdateWherePart = CodeBlock.builder();
    lineCounter = 0;
    for (Column col : table.getPrimaryKeys()) {
      cbUpdateColumnsPart.addStatement(
        "sb.append(\"" +
        (lineCounter == 0 ? " WHERE " : "") +
        col.getName() +
        " = :" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName()) +
        (lineCounter < (table.getPrimaryKeys().size() - 1) ? " AND " : "") +
        " \")"
      );
      lineCounter++;
    }

    update.addCode(updateBlock.build());
    update.addCode(cbUpdateColumnsPart.build());
    update.addCode(cbUpdateWherePart.build());

    // TODO Manca la dichiarazione dell'oggetto restituito

    CodeBlock.Builder cbExecuteUpdate = CodeBlock
      .builder()
      .beginControlFlow("try");

    cbExecuteUpdate.addStatement(
      "MapSqlParameterSource namedParameters = new MapSqlParameterSource()"
    );
    for (Column col : table.getColumnList()) {
      cbExecuteUpdate.addStatement(
        "namedParameters.addValue(\"" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName()) +
        "\" , dto.get" +
        GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(col.getName()) +
        "(), Types." +
        SQLTypeMap.getAllJdbcTypeNames().get(col.getSqlType()) +
        ")"
      );
    }

    cbExecuteUpdate
      .addStatement("jdbcTemplate.update(sb.toString(), namedParameters)")
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:update -> exception \", e)",
        ClassName.get(basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:update - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result")
      .build();
    update.addCode(cbExecuteUpdate.build());
    return update.build();
  }
}
