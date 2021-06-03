package org.tharos.jdbc.swissknife.generate.strategy.dao.util;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.Modifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.tharos.jdbc.swissknife.dto.Column;

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

  public static MethodSpec generateSetterForColName(Column col) {
    return MethodSpec
      .methodBuilder(
        "set" + generateCamelCaseNameFromSnakeCaseString(col.getName())
      )
      .addParameter(
        String.class,
        generateInstanceNameFromSnakeCaseString(col.getName())
      )
      .addModifiers(Modifier.PUBLIC)
      .returns(col.getType())
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
}
