package org.tharos.jdbc.swissknife;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.core.JDBCUtil;
import org.tharos.jdbc.swissknife.dto.Table;

public class App {

	private static Logger LOGGER = LogManager.getLogger(App.class.getName());

	public static void main(String[] args) {
		JDBCUtil jdbcUtil = new JDBCUtil("org.postgresql.Driver",
				"jdbc:postgresql://tst-territorio-vdb01.territorio.csi.it:5432/PGISOLATST", "geoconf", "mypass");
		jdbcUtil.getConnection();
		ArrayList<Table> tableList = jdbcUtil.getTablesList();
		for (int i = 0; i < tableList.size(); i++) {
			LOGGER.debug(i + ". " + tableList.get(i).toString());
		}
	}

}
