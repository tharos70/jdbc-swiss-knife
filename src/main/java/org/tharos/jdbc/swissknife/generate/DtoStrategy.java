package org.tharos.jdbc.swissknife.generate;

import java.io.IOException;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class DtoStrategy extends Strategy {

	public DtoStrategy() {
		setName("DtoStrategy");
	}

	@Override
	public void executeInternalStrategy(Table table, String prefixToExclude, String basePackage) throws IOException {
		String purifiedName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, StringUtils.removeStart(table.getName(), prefixToExclude));
		LOGGER.info("1 purifiedName ["+purifiedName+"]");
		Builder dtoSpecBuilder = TypeSpec
				  .classBuilder(purifiedName) 
				  .addModifiers(Modifier.PUBLIC);
		for(Column col: table.getColumnList()) {
			Class colType = col.getType();
			

			FieldSpec columnField = FieldSpec
					  .builder(colType, CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, col.getName()))
					  .addModifiers(Modifier.PRIVATE)
					  .initializer("null")
					  .build();
			dtoSpecBuilder.addField(columnField);
			
			dtoSpecBuilder.addMethod(MethodSpec
					    .methodBuilder("get"+WordUtils.capitalize(col.getName()))
					    .addModifiers(Modifier.PUBLIC)
					    .returns(colType)
					    .addStatement("return this."+col.getName())
					    .build())
					  .addMethod(MethodSpec
					    .methodBuilder("set"+WordUtils.capitalize(col.getName()))
					    .addParameter(String.class, col.getName())
					    .addModifiers(Modifier.PUBLIC)
					    .returns(colType)
					    .addStatement("this."+col.getName()+" = "+col.getName())
					    .build());
					  
			TypeSpec dtoType = dtoSpecBuilder.build();
			
			JavaFile javaFile = JavaFile
					  .builder(basePackage+".dto", dtoType)
					  .indent("    ")
					  .build();
			javaFile.writeTo(System.out);
			
		}
		
		
		
	}


	
}
