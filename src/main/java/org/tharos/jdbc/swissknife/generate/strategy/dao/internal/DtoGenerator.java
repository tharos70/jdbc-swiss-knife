package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import javax.lang.model.element.Modifier;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DtoGenerator {

  private String purifiedName;
  private Table table;

  public DtoGenerator(String purifiedName, Table table) {
    this.purifiedName = purifiedName;
    this.table = table;
  }

  public TypeSpec createDtoTypeSpec() {
    Builder dtoSpecBuilder = TypeSpec
      .classBuilder(
        GeneratorUtils.generateCamelCaseNameFromSnakeCaseString(purifiedName)
      )
      .addModifiers(Modifier.PUBLIC);
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
