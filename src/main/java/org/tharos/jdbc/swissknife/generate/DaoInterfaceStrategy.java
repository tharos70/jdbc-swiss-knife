package org.tharos.jdbc.swissknife.generate;

import java.io.IOException;

import org.tharos.jdbc.swissknife.dto.Table;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public class DaoInterfaceStrategy extends Strategy {

	public DaoInterfaceStrategy() {
		setName("DaoInterfaceStrategy");
	}

	@Override
	public void executeInternalStrategy(Table table, String prefixToExclude, String basePackage) throws IOException {
		MethodSpec findByKey = MethodSpec.methodBuilder("findByKey")
			    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
			    .returns(void.class)
			    .addParameter(String[].class, "args")
			    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
			    .build();

//			TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
//			    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//			    .addMethod(main)
//			    .build();

//			JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
//			    .build();
//
//			javaFile.writeTo(System.out);
	}

	
}
