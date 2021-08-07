package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import static com.squareup.javapoet.TypeSpec.Builder;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DtoGen {

  private String purifiedName;
  private Table table;

  public DtoGen(String purifiedName, Table table) {
    this.purifiedName = purifiedName;
    this.table = table;
  }

  public TypeSpec createDtoTypeSpec() {
    Builder dtoSpecBuilder = TypeSpec
      .classBuilder(
        GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(purifiedName)
      )
      .addModifiers(Modifier.PUBLIC);
    // .addAnnotation(GeneratorUtils.generateAnnotation(Generated.class));
    for (Column col : table.getColumnList()) {
      Class<?> colType = col.getType();
      FieldSpec columnField = FieldSpec
        .builder(
          colType,
          GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName())
        )
        .addModifiers(Modifier.PRIVATE)
        .initializer("null")
        .build();
      dtoSpecBuilder.addField(columnField);

      dtoSpecBuilder
        .addMethod(GeneratorUtils.generateGetterForColName(col))
        .addMethod(GeneratorUtils.generateSetterForColName(col));
    }
    return dtoSpecBuilder.build();
  }
}
