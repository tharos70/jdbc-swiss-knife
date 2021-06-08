package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DaoImplGenerator {

  private String purifiedName;
  private Table table;
  private String basePackage;

  public DaoImplGenerator(
    String basePackage,
    String purifiedName,
    Table table
  ) {
    this.purifiedName = purifiedName;
    this.table = table;
    this.basePackage = basePackage;
  }

  public TypeSpec createDaoImplTypeSpec(
    TypeSpec dto,
    TypeSpec rowmapper,
    TypeSpec daoException
  ) {
    FieldSpec rowMapperInstance = generateRowMapperInstantiationStatement(
      rowmapper
    );
    FieldSpec loggerInstance = generateLoggerInstantiationStatement(
      this.basePackage + ".dao.impl"
    );

    MethodSpec contructor = generateDaoImplInitializerBlock();
    MethodSpec tableNameGetter = generateTableNameGetter(table.getName());
    MethodSpec sequenceNameGetter = generateSequenceNameGetter(
      table.getSequenceName()
    );
    MethodSpec findByPrimaryKey = generateFindByPrimaryKey(
      table,
      dto,
      daoException
    );

    MethodSpec findByFilter = generateFindByFilter(table, dto, daoException);
    MethodSpec deleteByKey = generateDeleteByKey(table, dto, daoException);

    TypeSpec daoImplType = TypeSpec
      .classBuilder(
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl"
      )
      .superclass(ClassName.get(this.basePackage, "AbstractDao"))
      .addMethod(contructor)
      .addMethod(tableNameGetter)
      .addMethod(sequenceNameGetter)
      .addMethod(findByPrimaryKey)
      .addMethod(findByFilter)
      .addMethod(deleteByKey)
      .addAnnotation(GeneratorUtils.generateRepositoryAnnotation())
      .addModifiers(Modifier.PUBLIC)
      .addField(rowMapperInstance)
      .addField(loggerInstance)
      .build();

    return daoImplType;
  }

  private MethodSpec generateDeleteByKey(
    Table table2,
    TypeSpec dto,
    TypeSpec daoException
  ) {
    MethodSpec.Builder deleteByKey = MethodSpec
      .methodBuilder("deleteByKey")
      .addModifiers(Modifier.PUBLIC)
      .returns(TypeName.VOID)
      .addException(
        ClassName.get(this.basePackage + "exception", daoException.name)
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

    CodeBlock cbExecuteDelete = CodeBlock
      .builder()
      .beginControlFlow("try")
      .addStatement(
        "result = jdbcTemplate.update(sb.toString(), new Object[]{" +
        generateJdbcMappingsForTablePks(table) +
        "}) "
      )
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl:delete -> Record not found\", e)",
        ClassName.get(this.basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl:delete - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result")
      .build();
    deleteByKey.addCode(cbExecuteDelete);
    return deleteByKey.build();
  }

  private MethodSpec generateFindByFilter(
    Table table,
    TypeSpec dto,
    TypeSpec daoException
  ) {
    MethodSpec.Builder findByFilter = MethodSpec
      .methodBuilder("findByFilter")
      .addModifiers(Modifier.PUBLIC)
      .returns(
        ParameterizedTypeName.get(
          ClassName.get(List.class),
          TypeVariableName.get(
            CaseFormat.UPPER_UNDERSCORE.to(
              CaseFormat.UPPER_CAMEL,
              this.purifiedName + "Dto"
            )
          )
        )
      )
      .addException(
        ClassName.get(this.basePackage + "exception", daoException.name)
      );
    for (Column col : table.getPrimaryKeys()) {
      findByFilter.addParameter(
        col.getType(),
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName())
      );
    }
    findByFilter.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(
        CaseFormat.UPPER_CAMEL,
        this.purifiedName
      ) +
      "DaoImpl:findByFilter - IN\")"
    );
    findByFilter.addStatement(
      "List<" +
      CaseFormat.UPPER_UNDERSCORE.to(
        CaseFormat.UPPER_CAMEL,
        this.purifiedName
      ) +
      "Dto> result = null"
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
      .addStatement("sb.append(\"WHERE \")")
      .build();
    findByFilter.addCode(cbSelectFirstPart);
    CodeBlock.Builder cbSelectFilterPart = CodeBlock.builder();
    int lineCounter = 0;
    for (Column col : table.getColumnList()) {
      cbSelectFilterPart.addStatement(
        "sb.append(\"(" +
        (lineCounter > 0 ? " AND " : "") +
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
    CodeBlock cbExecuteQuery = CodeBlock
      .builder()
      .beginControlFlow("try")
      .addStatement(
        "result = jdbcTemplate.queryForObject(sb.toString(), new Object[]{" +
        generateJdbcMappingsForTablePks(table) +
        "},  rowMapper ) "
      )
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl:findByFilter -> Record not found\", e)",
        ClassName.get(this.basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl:findByFIlter - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result")
      .build();
    findByFilter.addCode(cbExecuteQuery);
    return findByFilter.build();
  }

  private MethodSpec generateFindByPrimaryKey(
    Table table,
    TypeSpec dto,
    TypeSpec daoException
  ) {
    MethodSpec.Builder findByKey = MethodSpec
      .methodBuilder("findByPrimaryKey")
      .addModifiers(Modifier.PUBLIC)
      .returns(
        ClassName.get(
          this.basePackage + ".dto",
          CaseFormat.UPPER_UNDERSCORE.to(
            CaseFormat.UPPER_CAMEL,
            this.purifiedName
          ) +
          "Dto"
        )
      )
      .addException(
        ClassName.get(this.basePackage + "exception", daoException.name)
      );
    for (Column col : table.getPrimaryKeys()) {
      findByKey.addParameter(
        col.getType(),
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(col.getName())
      );
    }
    findByKey.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(
        CaseFormat.UPPER_CAMEL,
        this.purifiedName
      ) +
      "DaoImpl:findByPrimaryKey - IN\")"
    );
    findByKey.addStatement(
      CaseFormat.UPPER_UNDERSCORE.to(
        CaseFormat.UPPER_CAMEL,
        this.purifiedName
      ) +
      "Dto result = null"
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
      .addStatement("sb.append(\"WHERE \")")
      .build();
    findByKey.addCode(cbSelectFirstPart);
    CodeBlock.Builder cbSelectFilterPart = CodeBlock.builder();
    cbSelectFilterPart.addStatement(
      "sb.append(\"" +
      generateSQLFilterStringForTablePks(table, cbSelectFilterPart) +
      ";\")"
    );

    findByKey.addCode(cbSelectFilterPart.build());
    CodeBlock cbExecuteQuery = CodeBlock
      .builder()
      .beginControlFlow("try")
      .addStatement(
        "result = jdbcTemplate.queryForObject(sb.toString(), new Object[]{" +
        generateJdbcMappingsForTablePks(table) +
        "},  rowMapper ) "
      )
      .nextControlFlow("catch ($T e)", Exception.class)
      .addStatement(
        "throw new $T(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl:findByPrimaryKey -> Record having key [" +
        generateJdbcMappingsLoggingForTablePksValues(table) +
        "] not found\", e)",
        ClassName.get(this.basePackage + ".exception", daoException.name)
      )
      .nextControlFlow("finally")
      .addStatement(
        "LOG.info(\"" +
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl:findByPrimaryKey - OUT\")"
      )
      .endControlFlow()
      .addStatement("return result")
      .build();
    findByKey.addCode(cbExecuteQuery);
    return findByKey.build();
  }

  private String generateJdbcMappingsLoggingForTablePksValues(Table table) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
      Column pk = table.getPrimaryKeys().get(i);
      sb.append(
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        " = \" + " +
        GeneratorUtils.generateToStringStatementAccordingToColumnType(
          pk.getType(),
          pk.getName()
        ) +
        " + \"" +
        ((i < (table.getPrimaryKeys().size() - 1)) ? ", " : "")
      );
    }
    return sb.toString();
  }

  private String generateSQLFilterStringForTablePks(
    Table table,
    CodeBlock.Builder cbSelectFilterPart
  ) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
      Column pk = table.getPrimaryKeys().get(i);
      sb.append(
        pk.getName() +
        " = :" +
        GeneratorUtils.generateInstanceNameFromSnakeCaseString(pk.getName()) +
        (i != table.getPrimaryKeys().size() - 1 ? " AND " : "")
      );
    }
    return sb.toString();
  }

  private String generateJdbcMappingsForTablePks(Table table) {
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

  private MethodSpec generateTableNameGetter(String tableName) {
    return MethodSpec
      .methodBuilder("getTableName")
      .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
      .addModifiers(Modifier.PROTECTED)
      .returns(String.class)
      .addStatement("return \"" + tableName + "\"")
      .build();
  }

  private MethodSpec generateSequenceNameGetter(String sequenceName) {
    return MethodSpec
      .methodBuilder("getSequenceName")
      .addAnnotation(GeneratorUtils.generateOverrideAnnotation())
      .addModifiers(Modifier.PROTECTED)
      .returns(String.class)
      .addStatement("return \"" + sequenceName + "\"")
      .build();
  }

  private MethodSpec generateDaoImplInitializerBlock() {
    return MethodSpec
      .constructorBuilder()
      .addAnnotation(GeneratorUtils.generateAutowiredAnnotation())
      .addModifiers(Modifier.PUBLIC)
      .addParameter(DataSource.class, "datasource")
      .addParameter(NamedParameterJdbcTemplate.class, "jdbcTemplate")
      .addStatement("super(datasource, jdbcTemplate)")
      .build();
  }

  private FieldSpec generateLoggerInstantiationStatement(
    String loggingPackage
  ) {
    FieldSpec loggerInstance = FieldSpec
      .builder(
        ClassName.get("org.apache.log4j", "Logger"),
        "LOG",
        Modifier.PRIVATE,
        Modifier.STATIC,
        Modifier.FINAL
      )
      .initializer("Logger.getLogger(" + loggingPackage + ")")
      .build();
    return loggerInstance;
  }

  private FieldSpec generateRowMapperInstantiationStatement(
    TypeSpec rowmapper
  ) {
    FieldSpec rowMapperInstance = FieldSpec
      .builder(
        ClassName.get(basePackage + ".rowmapper", rowmapper.name),
        "rowMapper",
        Modifier.PRIVATE
      )
      .initializer("new " + rowmapper.name + "()")
      .build();
    return rowMapperInstance;
  }
}
