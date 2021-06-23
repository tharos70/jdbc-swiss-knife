package org.tharos.jdbc.swissknife.generate.strategy.dao.util;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;

public class GeneratorUtils {

  public static String generateVariableInstantiation(
    String typeName,
    String instanceName
  ) {
    StringBuilder sb = new StringBuilder();
    sb.append(typeName);
    sb.append(" ");
    sb.append(instanceName);
    sb.append(" = new ");
    sb.append(typeName);
    sb.append("()");
    return sb.toString();
  }

  public static MethodSpec generateSequenceNameGetter(String sequenceName) {
    return MethodSpec
      .methodBuilder("getSequenceName")
      //  .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
      .addModifiers(Modifier.PRIVATE)
      .returns(String.class)
      .addStatement("return \"" + sequenceName + "\"")
      .build();
  }

  public static MethodSpec generateTableNameGetter(String tableName) {
    return MethodSpec
      .methodBuilder("getTableName")
      // .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
      .addModifiers(Modifier.PRIVATE)
      .returns(String.class)
      .addStatement("return \"" + tableName + "\"")
      .build();
  }

  public static String generateJdbcMappingsForTablePks(Table table) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
      Column pk = table.getPrimaryKeys().get(i);
      sb.append(
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        ((i < (table.getPrimaryKeys().size() - 1)) ? ", " : "")
      );
    }
    return sb.toString();
  }

  public static String generateVariableDeclaration(
    String typeName,
    String instanceName
  ) {
    StringBuilder sb = new StringBuilder();
    sb.append(typeName);
    sb.append(" ");
    sb.append(instanceName);
    sb.append(";");
    return sb.toString();
  }

  public static String generateCamelCaseNameFromSnakeCaseString(
    final String snake
  ) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, snake);
  }

  public static String generateInstanceNameFromSnakeCaseString(
    final String snake
  ) {
    return decapitalize(generateCamelCaseNameFromSnakeCaseString(snake));
  }

  public static String decapitalize(final String string) {
    if (string == null || string.length() == 0) {
      return string;
    }

    char c[] = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);

    return new String(c);
  }

  public static String capitalize(final String string) {
    if (string == null || string.length() == 0) {
      return string;
    }

    char c[] = string.toCharArray();
    c[0] = Character.toUpperCase(c[0]);

    return new String(c);
  }

  public static MethodSpec generateGetterForColName(Column col) {
    return MethodSpec
      .methodBuilder(
        "get" + generateCamelCaseNameFromSnakeCaseString(col.getName())
      )
      .addModifiers(Modifier.PUBLIC)
      .returns(col.getType())
      .addStatement(
        "return this." + generateInstanceNameFromSnakeCaseString(col.getName())
      )
      .build();
  }

  public static MethodSpec generateGetterForField(
    Class<?> fieldType,
    String fieldName
  ) {
    return MethodSpec
      .methodBuilder("get" + capitalize(fieldName))
      .addModifiers(Modifier.PUBLIC)
      .returns(fieldType)
      .addStatement("return this." + fieldName)
      .build();
  }

  public static MethodSpec generateSetterForColName(Column col) {
    return MethodSpec
      .methodBuilder(
        "set" + generateCamelCaseNameFromSnakeCaseString(col.getName())
      )
      .addParameter(
        col.getType(),
        generateInstanceNameFromSnakeCaseString(col.getName())
      )
      .addModifiers(Modifier.PUBLIC)
      .returns(TypeName.VOID)
      .addStatement(
        "this." +
        generateInstanceNameFromSnakeCaseString(col.getName()) +
        " = " +
        generateInstanceNameFromSnakeCaseString(col.getName())
      )
      .build();
  }

  public static AnnotationSpec generateRepositoryAnnotation() {
    return AnnotationSpec.builder(Repository.class).build();
  }

  public static AnnotationSpec generateAutowiredAnnotation() {
    return AnnotationSpec.builder(Autowired.class).build();
  }

  public static AnnotationSpec generateOverrideAnnotation() {
    return AnnotationSpec.builder(Override.class).build();
  }

  public static AnnotationSpec generateAnnotation(Class<?> clazz) {
    return AnnotationSpec.builder(clazz).build();
  }

  public static String generateColumnStringListForSQL(List<Column> columnList) {
    StringBuilder sb = new StringBuilder();
    for (Column column : columnList) {
      sb.append(column.getName());
      sb.append(", ");
    }
    sb.deleteCharAt(sb.length() - 2);
    return sb.toString();
  }

  public static TypeSpec createExceptionTypeSpec(String name) {
    FieldSpec throwable = FieldSpec
      .builder(Throwable.class, "cause", Modifier.PRIVATE)
      .build();
    MethodSpec simpleConstructor = MethodSpec
      .constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "message")
      .addStatement("super(message)")
      .build();
    MethodSpec twoParamsConstructor = MethodSpec
      .constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addParameter(String.class, "message")
      .addParameter(Throwable.class, "throwable")
      .addStatement("super(message)")
      .addStatement("this.cause = throwable")
      .build();

    Builder exceptionSpecBuilder = TypeSpec
      .classBuilder(name)
      .addModifiers(Modifier.PUBLIC)
      .superclass(Exception.class)
      // .addAnnotation(generateAnnotation(Generated.class))
      .addField(throwable)
      .addMethod(simpleConstructor)
      .addMethod(twoParamsConstructor)
      .addMethod(generateGetterForField(Throwable.class, "cause"));
    return exceptionSpecBuilder.build();
  }

  public static String generateToStringStatementAccordingToColumnType(
    Class<?> type,
    String name
  ) {
    switch (type.getName()) {
      case "java.lang.String":
        return name;
      case "java.math.BigDecimal":
      case "java.lang.Boolean":
      case "java.lang.Byte":
      case "java.lang.Byte[]":
      case "java.lang.Short":
      case "java.lang.Integer":
      case "java.lang.Long":
      case "java.lang.Float":
      case "java.lang.Double":
        return name.toString();
      default:
        throw new UnsupportedOperationException(
          "Type not supported so far... [" + type.getName() + "]"
        );
    }
  }
}
