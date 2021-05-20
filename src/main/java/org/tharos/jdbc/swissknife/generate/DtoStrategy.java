package org.tharos.jdbc.swissknife.generate;

import java.io.IOException;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class DtoStrategy extends Strategy {

	public DtoStrategy() {
		setName("DtoStrategy");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void executeInternalStrategy(Table table, String prefixToExclude) throws IOException {
		
		String purifiedName = WordUtils.capitalizeFully(StringUtils.removeStart(table.getName(), prefixToExclude)); 
		LOGGER.info("purifiedName ["+purifiedName+"]");
		Builder dtoSpecBuilder = TypeSpec
				  .classBuilder(purifiedName) //TODO gestire il camel case
				  .addModifiers(Modifier.PUBLIC);
		for(Column col: table.getColumnList()) {
			String colType = col.getType();
			switch (colType) {
			case "TODO":
				
				break;

			default:
				break;
			} 

			FieldSpec columnField = FieldSpec
					  .builder(String.class, col.getName()) //TODO gestire correttamente il tipo
					  .addModifiers(Modifier.PRIVATE)
					  .initializer("null")
					  .build();
			dtoSpecBuilder.addField(columnField);
			
			
			
			
			dtoSpecBuilder.addMethod(MethodSpec
					    .methodBuilder("get"+WordUtils.capitalize(col.getName()))
					    .addModifiers(Modifier.PUBLIC)
					    .returns(String.class)
					    .addStatement("return this."+col.getName())
					    .build())
					  .addMethod(MethodSpec
					    .methodBuilder("set"+WordUtils.capitalize(col.getName()))
					    .addParameter(String.class, col.getName())
					    .addModifiers(Modifier.PUBLIC)
					    .returns(String.class)
					    .addStatement("this."+col.getName()+" = "+col.getName())
					    .build());
					  
			TypeSpec dtoType = dtoSpecBuilder.build();
			
			JavaFile javaFile = JavaFile
					  .builder("com.baeldung.javapoet.person", dtoType)
					  .indent("    ")
					  .build();
			javaFile.writeTo(System.out);
			
		}
		
		
		
	}


	
}
