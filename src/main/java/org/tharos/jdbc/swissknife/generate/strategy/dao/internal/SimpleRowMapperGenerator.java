package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.Modifier;
import org.springframework.jdbc.core.RowMapper;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class SimpleRowMapperGenerator {

  private String purifiedName;
  private Table table;
  private String basePackage;

  public SimpleRowMapperGenerator(
    String basePackage,
    String purifiedName,
    Table table
  ) {
    this.purifiedName = purifiedName;
    this.table = table;
    this.basePackage = basePackage;
  }

  public TypeSpec createRowmapperTypeSpec(TypeSpec dto) {
    Builder mapRowImplB = CodeBlock
      .builder()
      .addStatement(
        GeneratorUtils.generateVariableInstantiation(
          CaseFormat.UPPER_UNDERSCORE.to(
            CaseFormat.UPPER_CAMEL,
            this.purifiedName
          ),
          "dto"
        )
      );
    //TODO Volendo si può gestire un flag per scegliere se creare un full dto che legga anche i valori dalle FK
    for (Column col : this.table.getColumnList()) {
      mapRowImplB.addStatement(generateFieldSetterFromResultSet(col));
    }
    CodeBlock mapRowImpl = mapRowImplB.build();

    TypeSpec type = TypeSpec
      .classBuilder(
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "RowMapper"
      )
      .addModifiers(Modifier.PUBLIC)
      .addSuperinterface(
        ParameterizedTypeName.get(
          ClassName.get(RowMapper.class),
          TypeVariableName.get(
            CaseFormat.UPPER_UNDERSCORE.to(
              CaseFormat.UPPER_CAMEL,
              this.purifiedName
            )
          )
        )
      )
      .addMethod(
        MethodSpec
          .methodBuilder("mapRow")
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
          .addParameter(ClassName.get("java.sql", "ResultSet"), "rs")
          .addParameter(ClassName.INT, "rowNum")
          .addException(ClassName.get("java.sql", "SQLException"))
          .returns(
            ClassName.get(
              this.basePackage + ".dto",
              CaseFormat.UPPER_UNDERSCORE.to(
                CaseFormat.UPPER_CAMEL,
                this.purifiedName
              )
            )
          )
          .addCode(mapRowImpl)
          .addStatement("return dto")
          .build()
      )
      .build();
    return type;
  }

  private String generateFieldSetterFromResultSet(Column col) {
    return (
      "dto." +
      GeneratorUtils.generateSetterForColName(col).name +
      "(rs.get" +
      col.getType().getSimpleName() +
      "(\"" +
      col.getName() +
      "\"))"
    );
  }
}
