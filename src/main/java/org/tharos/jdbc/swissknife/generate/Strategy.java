package org.tharos.jdbc.swissknife.generate;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.App;
import org.tharos.jdbc.swissknife.dto.Table;

public abstract class Strategy {

	protected static Logger LOGGER = LogManager.getLogger(App.class.getName());
	private String name;
	
	public void executeStrategy(File file, Table table, String prefixToExclude, String basePackage) throws IOException {
		LOGGER.info("Strategy:"+this.getName()+" - IN");
		this.executeInternalStrategy(file, table, prefixToExclude, basePackage);
		LOGGER.info("Strategy:"+this.getName()+" - OUT");
	}
	
	protected abstract void executeInternalStrategy(File file,Table table, String prefixToExclude, String basePackage) throws IOException;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
