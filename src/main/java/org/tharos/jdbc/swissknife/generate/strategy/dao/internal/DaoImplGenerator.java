package org.tharos.jdbc.swissknife.generate.strategy.dao.internal;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
  )
    throws IllegalArgumentException, IllegalAccessException {
    FieldSpec rowMapperInstance = generateRowMapperInstantiationStatement(
      rowmapper
    );
    FieldSpec loggerInstance = generateLoggerInstantiationStatement(
      this.basePackage + ".dao.impl"
    );

    // MethodSpec contructor = generateDaoImplInitializerBlock();
    // MethodSpec tableNameGetter = GeneratorUtils.generateTableNameGetter(
    //   table.getName()
    // );
    MethodSpec sequenceNameGetter = GeneratorUtils.generateSequenceNameGetter(
      table.getSequenceName()
    );
    MethodSpec findByPrimaryKey = new FindByPrimaryKeyGen()
    .generateFindByPrimaryKey(
        table,
        dto,
        daoException,
        basePackage,
        purifiedName
      );

    MethodSpec findByFilter = new FindByFilterGen()
    .generateFindByFilter(table, dto, daoException, basePackage, purifiedName);
    MethodSpec deleteByKey = new DeleteGen()
    .generateDeleteByKey(table, dto, daoException, basePackage, purifiedName);

    MethodSpec insert = new InsertGen()
    .generateInsert(table, dto, daoException, basePackage, purifiedName);
    MethodSpec update = new UpdateGen()
    .generateUpdate(table, dto, daoException, basePackage, purifiedName);

    TypeSpec.Builder daoImplType = TypeSpec
      .classBuilder(
        CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.UPPER_CAMEL,
          this.purifiedName
        ) +
        "DaoImpl"
      )
      //.superclass(ClassName.get(this.basePackage, "AbstractDao"))
      //.addMethod(contructor)
      .addField(generateAutowiredDatasource())
      .addField(generateAutowiredJdbcTemplate())
      // .addMethod(tableNameGetter)
      .addMethod(sequenceNameGetter)
      .addMethod(findByPrimaryKey)
      .addMethod(findByFilter)
      .addMethod(deleteByKey)
      .addMethod(insert)
      .addMethod(update)
      .addAnnotation(GeneratorUtils.generateRepositoryAnnotation())
      .addModifiers(Modifier.PUBLIC)
      .addField(rowMapperInstance)
      .addField(loggerInstance);
    if (table.getPrimaryKeys().size() == 1) {
      MethodSpec getNextSequenceValue = generateGetNextSequenceValue(
        table,
        dto,
        daoException
      );
      daoImplType.addMethod(getNextSequenceValue);
    }
    return daoImplType.build();
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

  private FieldSpec generateAutowiredJdbcTemplate() {
    FieldSpec jdbcTemplateInstance = FieldSpec
      .builder(
        ClassName.get(
          "org.springframework.jdbc.core.namedparam",
          "NamedParameterJdbcTemplate"
        ),
        "jdbcTemplate",
        Modifier.PRIVATE
      )
      .addAnnotation(GeneratorUtils.generateAnnotation(Autowired.class))
      .build();
    return jdbcTemplateInstance;
  }

  private FieldSpec generateAutowiredDatasource() {
    FieldSpec datasourceInstance = FieldSpec
      .builder(
        ClassName.get("javax.sql", "DataSource"),
        "datasource",
        Modifier.PRIVATE
      )
      .addAnnotation(GeneratorUtils.generateAnnotation(Autowired.class))
      .build();
    return datasourceInstance;
  }

  private FieldSpec generateLoggerInstantiationStatement(
    String loggingPackage
  ) {
    FieldSpec loggerInstance = FieldSpec
      .builder(
        ClassName.get("org.apache.logging.log4j", "Logger"),
        "LOG",
        Modifier.PRIVATE,
        Modifier.STATIC,
        Modifier.FINAL
      )
      .initializer(
        "org.apache.logging.log4j.LogManager.getLogger(\"" +
        loggingPackage +
        "\")"
      )
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

  private MethodSpec generateGetNextSequenceValue(
    Table table,
    TypeSpec dto,
    TypeSpec daoException
  ) {
    if (table.getPrimaryKeys().size() != 1) {
      throw new UnsupportedOperationException(
        "Cannot create getNextSequenceValue method"
      );
    }
    MethodSpec.Builder getNextSequenceValueMethod = MethodSpec
      .methodBuilder("getNextSequenceValue")
      .addModifiers(Modifier.PUBLIC)
      .returns(table.getPrimaryKeys().get(0).getType())
      .addException(
        ClassName.get(this.basePackage + ".exception", daoException.name)
      );
    getNextSequenceValueMethod.addStatement(
      "LOG.info(\"" +
      CaseFormat.UPPER_UNDERSCORE.to(
        CaseFormat.UPPER_CAMEL,
        this.purifiedName
      ) +
      "DaoImpl:getNextSequenceValue - IN\")"
    );
    CodeBlock.Builder sequenceNextValBlock = CodeBlock
      .builder()
      .addStatement(
        "return jdbcTemplate.queryForObject(\"SELECT nextval('" +
        table.getSchemaName() +
        ".\"+getSequenceName()+\"')\", new HashMap<>(), " +
        table.getPrimaryKeys().get(0).getType().getSimpleName() +
        ".class)"
      );
    getNextSequenceValueMethod.addCode(sequenceNextValBlock.build());

    return getNextSequenceValueMethod.build();
  }
}
