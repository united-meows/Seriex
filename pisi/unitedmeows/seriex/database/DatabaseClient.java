package pisi.unitedmeows.seriex.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.yystal.sql.YSQLCommand;
import pisi.unitedmeows.yystal.utils.IDisposable;

public class DatabaseClient implements IDisposable {
	private boolean connected;
	protected Connection connection;
	private final Object actionLock = new Object();
	private HashMap<String, List<String>> tableColumnsCache;

	public DatabaseClient(String databaseName) {
		try {
			synchronized (this) {
				if (connection != null && !connection.isClosed()) return;

				var seriexDir = new File(Seriex.get().fileManager().pluginDirectory(), databaseName + ".db");
				DriverManager.setLoginTimeout(0);
				connection = DriverManager.getConnection("jdbc:sqlite://" + seriexDir.getAbsolutePath());
				connected = true;
			} 
			tableColumnsCache = new HashMap<>();
		}
		catch (Exception ex) {
			connected = false;
			ex.printStackTrace();
		}
	}

	public boolean execute(String sql) {
		System.out.println("SQL: " + sql);
		try (var command = connection.prepareStatement(sql)) {
			return command.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.get().logger().error("Error in SQL: " + sql);
			return false;
		}
	}

	public boolean execute(YSQLCommand sql) {
		return execute(sql.getHooked());
	}

	public List<Map<String, Object>> select(YSQLCommand sqlCommand, String... columnNames) {
		return select(sqlCommand.getHooked(), columnNames);
	}

	public List<Map<String, Object>> select(String sql, String... columnNames) {
		synchronized (actionLock) {
			try (var command = connection.prepareStatement(sql);
						var resultSet = command.executeQuery()) {
				List<Map<String, Object>> list = new ArrayList<>();
				while (resultSet.next()) {
					Map<String, Object> dataMap = new HashMap<>();
					for (String column : columnNames) {
						dataMap.put(column, resultSet.getObject(column));
					}
					list.add(dataMap);
				}
				return list;
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
	}

	public boolean connected() {
		return connected;
	}

	public Connection connection() {
		return connection;
	}

	@Override
	public void close() {
		try {
			connection.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		connected = false;
		tableColumnsCache.clear();
	}



	@Deprecated
	private List<String> dbColumnsNoCache(String table) {
		synchronized (actionLock) {
			var select = select(new YSQLCommand("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=^").putString(table), "TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME",
						"COLUMN_NAME");
			List<String> columns = new ArrayList<>();
			for (var i = 0; i < select.size(); i++) {
				columns.add((String) select.get(i).get("COLUMN_NAME"));
			}
			return columns;
		}
	}

	@Deprecated
	private List<String> dbColumns(String table) {
		return tableColumnsCache.computeIfAbsent(table, this::dbColumnsNoCache);
	}

	@Deprecated
	private List<List<Object>> select(YSQLCommand sql) {
		return select(sql.getHooked());
	}

	@Deprecated
	private List<List<Object>> select(String sql) {
		synchronized (actionLock) {
			try (var command = connection.prepareStatement(sql);
						var resultSet = command.executeQuery()) {
				List<List<Object>> list = new ArrayList<>();
				final var columnCount = command.getMetaData().getColumnCount();
				while (resultSet.next()) {
					List<Object> dataList = new ArrayList<>();
					for (var i = 0; i < columnCount; i++) {
						dataList.add(i, resultSet.getObject(i));
					}
					list.add(dataList);
				}
				return list;
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
	}
}
