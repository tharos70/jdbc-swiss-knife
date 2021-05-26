package org.tharos.jdbc.swissknife;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.core.DatabaseMetadataExtractor;
import org.tharos.jdbc.swissknife.dto.Table;
import org.tharos.jdbc.swissknife.generate.DtoStrategy;
import org.tharos.jdbc.swissknife.generate.GenericGenerator;
import org.tharos.jdbc.swissknife.generate.Strategy;

public class App {

	private static Logger LOGGER = LogManager.getLogger(App.class.getName());

	public static void main(String[] args) {
		LOGGER.info("App - IN");
		//DatabaseMetadataExtractor dbme = new DatabaseMetadataExtractor("org.postgresql.Driver",
		//		"jdbc:postgresql://tst-territorio-vdb01.territorio.csi.it:5432/PGISOLATST", "geoconf", "mypass");

		DatabaseMetadataExtractor dbme = new DatabaseMetadataExtractor("org.postgresql.Driver",
				"jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres#01", "simone");

		ArrayList<Table> tableList = dbme.getTablesList();
		ArrayList<Strategy> strategies = new ArrayList<Strategy>();
		Strategy dtoStrategy = new DtoStrategy();
		strategies.add(dtoStrategy);
		try {
			new GenericGenerator(new File("C:\\workspaces\\jdbc-swiss-knife\\gen"),strategies, tableList, "geoapi_t","com.tharos.jdbc.swissknife.out").executeStrategies();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}

}
