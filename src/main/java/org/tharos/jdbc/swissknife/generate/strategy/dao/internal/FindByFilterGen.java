package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.tharos.jdbc.swissknife.core.SQLTypeMap;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class FindByFilterGen {

  public MethodSpec generateFindByFilter(
    Table table,
    TypeSpec dto,
    TypeSpec daoException,
    String basePackage,
    String purifiedName
  )
    throws IllegalArgumentException, IllegalAccessException {
    MethodSpec.Builder findByFilter = MethodSpec
      .methodBuilder("findByFilter")
      .addModifiers(Modifier.PUBLIC)
      .returns(
        ParameterizedTypeName.get(
          ClassName.get(List.class),
          TypeVariableName.get(
            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName)
          )
        )
      )
      .addException(
        ClassName.get(basePackage + ".exception", daoException.name)
      );
    findByFilter.addParameter(
      ClassName.get(
        basePackage + ".dto",
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName)
      ),
      "dto"
    );

    findByFilter.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      "DaoImpl:findByFilter - IN\")"
    );
    findByFilter.addStatement(
      "List<" +
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      "> result = null"
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
    findByFilter.addCode(cbSelectFirstPart);
    CodeBlock.Builder cbSelectFilterPart = CodeBlock.builder();
    int lineCounter = 0;
    for (Column col : table.getColumnList()) {
      cbSelectFilterPart.addStatement(
        "sb.append(\"" +
        (lineCounter > 0 ? " AND " : "") +
        "(" +
        ":" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName()) +
        " is null OR " +
        col.getName() +
        " = :" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName()) +
        ")\")"
      );
      lineCounter++;
    }
    findByFilter.addCode(cbSelectFilterPart.build());
    CodeBlock.Builder cbExecuteQuery = CodeBlock
      .builder()
      .beginControlFlow("try")
      .addStatement(
        "MapSqlParameterSource namedParameters = new MapSqlParameterSource()"
      );

    for (Column col : table.getColumnList()) {
      cbExecuteQuery.addStatement(
        "namedParameters.addValue(\"" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName()) +
        "\" , dto.get" +
        GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(col.getName()) +
        "(), Types." +
        SQLTypeMap.getAllJdbcTypeNames().get(col.getSqlType()) +
        ")"
      );
    }
    cbExecuteQuery
      .addStatement(
        "result = jdbcTemplate.query(sb.toString(), namedParameters,  rowMapper ) "
      )
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:findByFilter -> Record not found\", e)",
        ClassName.get(basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:findByFIlter - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result");
    findByFilter.addCode(cbExecuteQuery.build());
    return findByFilter.build();
  }
}
