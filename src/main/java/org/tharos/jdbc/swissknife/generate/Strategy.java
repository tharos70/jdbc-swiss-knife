package org.tharos.jdbc.swissknife.generate;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.App;
import org.tharos.jdbc.swissknife.dto.Table;

public abstract class Strategy {

	protected static Logger LOGGER = LogManager.getLogger(App.class.getName());
	private String name;
	
	public void executeStrategy(Table table, String prefixToExclude) throws IOException {
		LOGGER.info("DtoStrategy:"+this.getName()+" - IN");
		this.executeInternalStrategy(table, prefixToExclude);
		LOGGER.info("DtoStrategy:"+this.getName()+" - OUT");
	}
	
	public abstract void executeInternalStrategy(Table table, String prefixToExclude) throws IOException;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
