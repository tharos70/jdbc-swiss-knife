package org.tharos.jdbc.swissknife.core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tharos.jdbc.swissknife.dto.Column;
import org.tharos.jdbc.swissknife.dto.Table;

public class DatabaseMetadataExtractor {

	private Logger LOGGER = LogManager.getLogger(getClass().getName());

	private String className, URL, user, password;
	private Connection connection;

	private String schema;

	public DatabaseMetadataExtractor(String className, String URL, String user, String password, String schema) {
		this.className = className;
		this.URL = URL;
		this.user = user;
		this.password = password;
		this.connection = null;
		this.schema = schema;
	}

	private Connection initializeConnection() {
		try {
			Class.forName(className);
		} catch (ClassNotFoundException ex) {
			LOGGER.error("Unable to load the class. Terminating the program");
			System.exit(-1);
		}
		try {
			connection = DriverManager.getConnection(URL, user, password);
		} catch (SQLException ex) {
			LOGGER.error("Error getting connection: " + ex.getMessage());
			System.exit(-1);
		} catch (Exception ex) {
			LOGGER.error("Error: " + ex.getMessage());
			System.exit(-1);
		}
		if (connection != null) {
			LOGGER.info("Connected Successfully!");
		}
		return connection;
	}

//    TODO
	public void executeQuery() {
		Scanner scanner = new Scanner(System.in);
		String query = scanner.nextLine();
		ResultSet resultSet = null;
		try {
			Statement stmt = connection.createStatement();
			resultSet = stmt.executeQuery(query);
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnsNumber = metaData.getColumnCount();
			while (resultSet.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					System.out.printf("%-25s",
							(resultSet.getObject(i) != null) ? resultSet.getObject(i).toString() : null);
				}
			}
		} catch (SQLException ex) {
			System.out.println("Exception while executing statement. Terminating program... " + ex.getMessage());
		} catch (Exception ex) {
			System.out.println("General exception while executing query. Terminating the program..." + ex.getMessage());
		}
	}

	public ArrayList<Table> getTablesList() {
		initializeConnection();
		DatabaseMetaData databaseMetaData = null;
		ArrayList<Table> tableList = new ArrayList<Table>();
		try {
			databaseMetaData = connection.getMetaData();
			String[] tableType = new String[] { "TABLE" };
			ResultSet resultSet = databaseMetaData.getTables(null, this.schema, null, tableType);
			while (resultSet.next()) {
				Table tbl = new Table();
				tbl.setName(resultSet.getString("TABLE_NAME"));
				tbl.setColumnList(this.extractColumnsInfo(tbl.getName()));
				tableList.add(tbl);
			}
		} catch (SQLException ex) {
			LOGGER.error("Error while fetching metadata. Terminating program.. " + ex.getMessage());
			System.exit(-1);
		} catch (Exception ex) {
			LOGGER.error("Error while fetching metadata. Terminating program.. " + ex.getMessage());
			System.exit(-1);
		} finally {
			closeConnection();
		}

		return tableList;
	}

	private void closeConnection() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			try {
				if (!this.connection.isClosed()) {

					this.connection.close();
					this.connection = null;
				}
			} catch (Exception ex) {
				LOGGER.error("Cannot close connection. Exit.");
				this.connection = null;
				System.exit(-1);
			}
		}
	}

	public List<Column> extractColumnsInfo(String tableName) {
		Map<String, Column> colsMap = new HashMap<String, Column>();
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null);
			while (columns.next()) {
				Column col = new Column();
				col.setName(columns.getString("COLUMN_NAME"));
      			col.setType(SQLTypeMap.toClass( columns.getInt( "DATA_TYPE" ) ));
				col.setNullable(columns.getBoolean("IS_NULLABLE"));
				col.setAutoincrement(columns.getBoolean("IS_AUTOINCREMENT"));
				col.setSize(columns.getInt("COLUMN_SIZE"));
				col.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
				colsMap.put(col.getName(), col);
			}
			ResultSet PK = databaseMetaData.getPrimaryKeys(null, user, tableName);
			while (PK.next()) {
				String pkCol = PK.getString("COLUMN_NAME");
				if (colsMap.containsKey(pkCol)) {
					colsMap.get(pkCol).setPrimaryKey(true);
				}
			}

			// Get Foreign Keys
			ResultSet FK = databaseMetaData.getImportedKeys(null, user, tableName);
			while (FK.next()) {
				String fkTable = FK.getString("FKTABLE_NAME");
				String fkColumn = FK.getString("FKCOLUMN_NAME");
				String ownColumn = FK.getString("PKCOLUMN_NAME");
				if (colsMap.containsKey(ownColumn)) {
					colsMap.get(ownColumn).setForeignTableName(fkTable);
					colsMap.get(ownColumn).setForeignColumnName(fkColumn);
				}
			}
		} catch (SQLException ex) {
			LOGGER.error("Error while fetching metadata. Terminating program.. " + ex.getMessage());
			System.exit(-1);
		} catch (Exception ex) {
			LOGGER.error("Error while fetching metadata. Terminating program.. " + ex.getMessage());
			System.exit(-1);
		}
		return new ArrayList<Column>(colsMap.values());
	}

}