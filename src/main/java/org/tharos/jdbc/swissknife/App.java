package org.tharos.jdbc.swissknife;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.core.JDBCUtil;
import org.tharos.jdbc.swissknife.dto.Table;

public class App {
	
	private static Logger LOGGER = LogManager.getLogger(App.class.getName());
	
    
	public static void main(String[] args) {
		LOGGER.debug("Debug Message Logged !!!");
        LOGGER.info("Info Message Logged !!!");
        LOGGER.error("Error Message Logged !!!", new NullPointerException("NullError"));
		StringBuffer URL = new StringBuffer();
        URL.append("jdbc:postgresql://");
        URL.append("tst-territorio-vdb01.territorio.csi.it:5432/PGISOLATST");
		JDBCUtil jdbcUtil = new JDBCUtil("org.postgresql.Driver",
										 URL.toString(), 
										 "geoconf", 
										 "mypass");
        jdbcUtil.getConnection();
		ArrayList<Table> tableList = jdbcUtil.getTablesList();
		for (int i = 0; i < tableList.size(); i++) {
			LOGGER.debug(i + ". " + tableList.get(i).toString());
		}
	}

	

}
