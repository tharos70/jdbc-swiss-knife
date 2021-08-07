package org.tharos.jdbc.swissknife.generate.strategy.dao;

import static com.squareup.javapoet.TypeSpec.Builder;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import org.apache.commons.text.WordUtils;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DtoStrategy {

  public DtoStrategy() {}

  public TypeSpec generateDtoTypeSpec(
    Table table,
    String purifiedName,
    String basePackage
  )
    throws IOException {
    Builder dtoSpecBuilder = TypeSpec
      .classBuilder(purifiedName)
      .addModifiers(Modifier.PUBLIC)
      .addAnnotation(GeneratorUtils.generateAnnotation(Generated.class));
    for (Column col : table.getColumnList()) {
      Class<?> colType = col.getType();

      FieldSpec columnField = FieldSpec
        .builder(
          colType,
          CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, col.getName())
        )
        .addModifiers(Modifier.PRIVATE)
        .initializer("null")
        .build();
      dtoSpecBuilder.addField(columnField);

      dtoSpecBuilder
        .addMethod(
          MethodSpec
            .methodBuilder("get" + WordUtils.capitalize(col.getName()))
            .addModifiers(Modifier.PUBLIC)
            .returns(colType)
            .addStatement("return this." + col.getName())
            .build()
        )
        .addMethod(
          MethodSpec
            .methodBuilder("set" + WordUtils.capitalize(col.getName()))
            .addParameter(String.class, col.getName())
            .addModifiers(Modifier.PUBLIC)
            .returns(colType)
            .addStatement("this." + col.getName() + " = " + col.getName())
            .build()
        );
    }
    return dtoSpecBuilder.build();
  }

  protected String getPackagePostfix() {
    return ".dto";
  }
}
