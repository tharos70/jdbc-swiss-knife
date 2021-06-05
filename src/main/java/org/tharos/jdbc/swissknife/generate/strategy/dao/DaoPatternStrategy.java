package org.tharos.jdbc.swissknife.generate.strategy.dao;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.DtoGenerator;
import org.tharos.jdbc.swissknife.generate.strategy.dao.internal.SimpleRowMapperGenerator;
import org.tharos.jdbc.swissknife.generate.strategy.dao.util.GeneratorUtils;

public class DaoPatternStrategy {

  private String purifiedName;
  private String basePackage;

  public TypeSpec executeInternalStrategy(
    Table table,
    String purifiedName,
    String basePackage
  )
    throws IOException {
    this.purifiedName = purifiedName;
    this.basePackage = basePackage;
    TypeSpec dto = new DtoGenerator(purifiedName, table).createDtoTypeSpec();
    JavaFile dtoJavaFile = JavaFile
      .builder(this.basePackage.concat(".dto"), dto)
      .indent("    ")
      .build();
    dtoJavaFile.writeTo(new File("C:\\workspaces\\jdbc-swiss-knife\\gen")); // TODO portare fuori

    TypeSpec rowmapper = new SimpleRowMapperGenerator(
      basePackage,
      purifiedName,
      table
    )
    .createRowmapperTypeSpec(dto);
    JavaFile rowmapperJavaFile = JavaFile
      .builder(
        this.basePackage.concat(".integration.dao.impl.rowmapper"),
        rowmapper
      )
      .indent("    ")
      .build();
    rowmapperJavaFile.writeTo(
      new File("C:\\workspaces\\jdbc-swiss-knife\\gen")
    ); // TODO portare fuori

    TypeSpec daoException = GeneratorUtils.createExceptionTypeSpec(
      "DaoException"
    );
    JavaFile daoExceptionJavaFile = JavaFile
      .builder(this.basePackage.concat(".excepion"), daoException)
      .indent("    ")
      .build();
    daoExceptionJavaFile.writeTo(
      new File("C:\\workspaces\\jdbc-swiss-knife\\gen")
    ); // TODO portare fuori

    TypeSpec daoImpl = createDaoImplTypeSpec(
      dto,
      rowmapper,
      table,
      daoException
    );
    JavaFile daoImplJavaFile = JavaFile
      .builder(this.basePackage.concat(".integration.dao.impl"), daoImpl)
      .indent("    ")
      .build();
    daoImplJavaFile.writeTo(new File("C:\\workspaces\\jdbc-swiss-knife\\gen")); // TODO portare fuori
    return null;
  }

  private TypeSpec createDaoImplTypeSpec(
    TypeSpec dto,
    TypeSpec rowmapper,
    Table table,
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
      .addAnnotation(GeneratorUtils.generateRepositoryAnnotation())
      .addModifiers(Modifier.PUBLIC)
      .addField(rowMapperInstance)
      .addField(loggerInstance)
      .build();
    return daoImplType;
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
        "DaoImpl:findByPrimaryKey -> Record con chiave [" +
        generateJdbcMappingsLoggingForTablePksValues(table) +
        "] non trovato\", e)",
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
    // protected <T> List<T> executeQueryStatement(String query, T dto, RowMapper<T> rm) throws DaoException{
    //   try {
    //   List<T> result =  jdbcTemplate.query(query,  getParametersAndLogQuery(query, dto), rm);
    //   return result;
    //   }
    //   catch (Exception e) {
    //   logger.error(getClass().getSimpleName()+".executeQueryStatement exception",e);
    //   throw new DaoException("Query failed", e);
    //   }
    //   }
    // public AtterrTQueueDTO findByPrimaryKey(Long idTQueue) throws DaoException {
    //   AtterrTQueueDTO dtoWithKey = new AtterrTQueueDTO();
    //   dtoWithKey.setIdTQueue(idTQueue);
    //   String sql = "select " + "filter," + "session_id," + "id_t_queue, fk_status, dt_insert, num_retry, api_result "
    //   + "from " + "atterr_t_queue " + "where " + "id_t_queue = :idTQueue ";
    //   List<AtterrTQueueDTO> res = executeQueryStatement(sql, dtoWithKey, this);

    //   if (res == null || res.size() != 1) {
    //   throw new DaoException("Coda item avente id ["+idTQueue+"] non trovato");
    //   }
    //   return res.get(0);
    //   }
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
