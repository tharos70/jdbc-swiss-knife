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

public class InsertGen {

  public MethodSpec generateInsert(
    Table table,
    TypeSpec dto,
    TypeSpec daoException,
    String basePackage,
    String purifiedName
  ) {
    MethodSpec.Builder insert = MethodSpec
      .methodBuilder("insert")
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
    insert.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      "DaoImpl:insert - IN\")"
    );
    insert.addStatement(
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      " result = null"
    );
    CodeBlock.Builder insertBlock = CodeBlock
      .builder()
      .addStatement("StringBuilder sb = new StringBuilder()")
      .addStatement(
        "sb.append(\"INSERT INTO  " +
        table.getName() +
        "( " +
        GeneratorUtils.generateColumnStringListForSQL(table.getColumnList()) +
        ") " +
        " VALUES (  \")"
      );
    int lineCounter = 0;
    for (Column pk : table.getColumnList()) {
      insertBlock.addStatement(
        "sb.append(\" :" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        (lineCounter < table.getColumnList().size() - 1 ? "," : " )") +
        " \")"
      );
      lineCounter++;
    }
    insert.addCode(insertBlock.build());

    CodeBlock.Builder cbExecuteInsert = CodeBlock
      .builder()
      .beginControlFlow("try");
    if (table.getPrimaryKeys().size() == 1) {
      cbExecuteInsert
        .beginControlFlow("if (getSequenceName() != null)")
        .addStatement(
          "dto.set" +
          GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(
            table.getPrimaryKeys().get(0).getName()
          ) +
          "(getNextSequenceValue())"
        )
        .endControlFlow();
    } else {
      cbExecuteInsert.addStatement(
        "// TODO Be sure to set all the primary keys on the input DTO"
      );
    }

    cbExecuteInsert.addStatement(
      "Map<String, Object> params = new HashMap<String, Object>()"
    );
    for (Column pk : table.getPrimaryKeys()) {
      cbExecuteInsert.addStatement(
        "params.put(\"" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        "\" , dto.get" +
        GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(pk.getName()) +
        "())"
      );
    }
    cbExecuteInsert
      .addStatement("jdbcTemplate.update(sb.toString(), params) ")
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:insert -> exception \", e)",
        ClassName.get(basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:insert - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result")
      .build();
    insert.addCode(cbExecuteInsert.build());
    return insert.build();
  }
}
