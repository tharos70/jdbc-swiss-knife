package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DeleteGen {

  public MethodSpec generateDeleteByKey(
    Table table,
    TypeSpec dto,
    TypeSpec daoException,
    String basePackage,
    String purifiedName
  ) {
    MethodSpec.Builder deleteByKey = MethodSpec
      .methodBuilder("deleteByKey")
      .addModifiers(Modifier.PUBLIC);
    for (Column pk : table.getPrimaryKeys()) {
      deleteByKey.addParameter(
        pk.getType(),
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName())
      );
    }
    deleteByKey
      .returns(TypeName.VOID)
      .addException(
        ClassName.get(basePackage + ".exception", daoException.name)
      );

    deleteByKey.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
      "DaoImpl:deleteByKey - IN\")"
    );

    CodeBlock.Builder deleteBlock = CodeBlock
      .builder()
      .addStatement("StringBuilder sb = new StringBuilder()")
      .addStatement(
        "sb.append(\"DELETE FROM " + table.getName() + " WHERE \")"
      );
    int lineCounter = 0;
    for (Column pk : table.getPrimaryKeys()) {
      deleteBlock.addStatement(
        "sb.append(\" " +
        (lineCounter > 0 ? " AND " : "") +
        "" +
        pk.getName() +
        " = :" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        "\")"
      );
      lineCounter++;
    }
    deleteByKey.addCode(deleteBlock.build());

    CodeBlock.Builder cbExecuteDelete = CodeBlock
      .builder()
      .beginControlFlow("try")
      .addStatement(
        "Map<String, Object> params = new HashMap<String, Object>()"
      );
    for (Column pk : table.getPrimaryKeys()) {
      cbExecuteDelete.addStatement(
        "params.put(\"" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        "\" , " +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        ")"
      );
    }

    cbExecuteDelete
      .addStatement("jdbcTemplate.update(sb.toString(), params) ")
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:delete -> Record not found\", e)",
        ClassName.get(basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, purifiedName) +
        "DaoImpl:delete - OUT\")"
      )
      .endControlFlow();
    deleteByKey.addCode(cbExecuteDelete.build());
    return deleteByKey.build();
  }
}
