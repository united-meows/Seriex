package pisi.unitedmeows.seriex.database.util;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.seriex.util.lists.GlueList;
import pisi.unitedmeows.yystal.sql.YSQLCommand;
import pisi.unitedmeows.yystal.utils.Pair;
import pisi.unitedmeows.yystal.utils.kThread;

public class DatabaseReflection {

	/**
	 * <br>
	 * how it works:
	 * <br>
	 * <br>
	 * the map`s key is a class that implements IStruct to cache
	 * <br>
	 * the map`s value is: <br>
	 * A pair that stores all columns` names (String[])
	 * A list that stores all field`s names & fieldTypes in a pair
	 * 
	 */
	private static Map<Class<? extends IStruct>, Pair<String[], List<Pair<String, FieldType>>>> cache = new HashMap<>();
	private static Map<Class<? extends IStruct>, String> tables = new HashMap<>();
	private static Map<String, Pair<IStruct, Class<? extends IStruct>>> reverseTables = new HashMap<>();

	public static void init() {
		try {
			Reflections reflections = new Reflections("pisi.unitedmeows.seriex.database.structs.impl");
			Set<Class<? extends IStruct>> classes = reflections.getSubTypesOf(IStruct.class);
			for (Class<? extends IStruct> clazz : classes) {
				boolean annotationPresent = clazz.isAnnotationPresent(Struct.class);
				if (!annotationPresent) {
					System.out.println("Skipping class " + clazz.getName() + " no annotation found.");
					continue;
				}
				Struct column = clazz.getAnnotation(Struct.class);
				String name = column.name();
				tables.put(clazz, name);
				reverseTables.put(name, new Pair<>(clazz.newInstance(), clazz));
				Seriex.get().database().execute(create(clazz));
				YSQLCommand[] commands = setAndGetColumns(clazz);
				for (int i = 0; i < commands.length; i++) {
					Seriex.get().database().execute(commands[i]);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.logger().fatal("DatabaseReflection gave an exception! (%s)", e.getMessage());
		}
	}

	public static <X> X get(String tableName, YSQLCommand command, SeriexDB db, X struct) {
		try {
			Pair<List<Map<String, Object>>, Class<? extends IStruct>> pair = DatabaseReflection.sendRequest(command, tableName, db);
			List<Map<String, Object>> query = pair.item1();
			if (query.isEmpty()) return null;
			DatabaseReflection.setFields(query, pair.item2(), (IStruct) struct);
			return struct;
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.logger().fatal("Couldnt get data from the table %s from the database!", tableName);
			return null;
		}
	}

	private static Pair<List<Map<String, Object>>, Class<? extends IStruct>> sendRequest(YSQLCommand command, String tableName, SeriexDB db) {
		Pair<IStruct, Class<? extends IStruct>> table = DatabaseReflection.getReverseTable(tableName);
		List<Map<String, Object>> query = db.select(command, table.item1().getColumns());
		return new Pair<>(query, table.item2());
	}

	private static void setFields(List<Map<String, Object>> query, Class<? extends IStruct> clazz, IStruct struct) {
		try {
			List<Pair<String, FieldType>> list = DatabaseReflection.getColumnsFromClass(clazz).item2();
			for (int i = 0; i < list.size(); i++) {
				Pair<String, FieldType> item = list.get(i);
				String name = item.item1();
				FieldType fieldType = item.item2();
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				Object value = query.get(0).get(field.getName());
				if (value == null && !fieldType.nullable) {
					Seriex.logger().fatal("The value %s is not nullable and the value for %s in the database is null!", name, name);
					continue;
				}
				field.set(struct, value);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Pair<String[], List<Pair<String, FieldType>>> getColumnsFromClass(Class<? extends IStruct> input) {
		return cache.computeIfAbsent(input, clazz -> {
			Field[] fields = clazz.getFields();
			List<Pair<String, FieldType>> columns = new GlueList<>();
			List<String> names = new GlueList<>();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				field.setAccessible(true);
				Column annotation = field.getAnnotation(Column.class);
				if (annotation == null) {
					continue;
				}
				String name = annotation.name();
				if ("empty".equals(name)) {
					name = field.getName();
				}
				FieldType found = null;
				for (FieldType fieldType : FieldType.values()) {
					if (field.getType().isAssignableFrom(fieldType.type)) {
						found = fieldType;
						break;
					}
				}
				columns.add(new Pair<>(name, found));
				names.add(name);
			}
			String[] array = names.stream().toArray(String[]::new);
			return new Pair<>(array, columns);
		});
	}

	public static YSQLCommand[] setAndGetColumns(Class<? extends IStruct> clazz) {
		List<YSQLCommand> commands = new GlueList<>();
		List<Pair<String, FieldType>> list = getColumnsFromClass(clazz).item2();
		for (int i = 0; i < list.size(); i++) {
			Pair<String, FieldType> pair = list.get(i);
			String string = "ALTER TABLE ^ ADD IF NOT EXISTS ^ ^";
			commands.add(new YSQLCommand(string).putRaw(tables.get(clazz)).putRaw(pair.item1()).putRaw(pair.item2().mySQL));
		}
		return commands.stream().toArray(YSQLCommand[]::new);
	}

	public static YSQLCommand create(Class<? extends IStruct> clazz) {
		StringBuilder builder = new StringBuilder(MessageFormat.format("CREATE TABLE IF NOT EXISTS {0} (", tables.get(clazz)));
		List<Pair<String, FieldType>> list = getColumnsFromClass(clazz).item2();
		String firstItem = "";
		for (int i = 0; i < list.size(); i++) {
			Pair<String, FieldType> pair = list.get(i);
			if (i == 0) {
				firstItem = pair.item1();
				builder.append(String.format("%s %s %s,", firstItem, pair.item2().mySQL, " NOT NULL AUTO_INCREMENT"));
			} else {
				builder.append(String.format("%s %s,", pair.item1(), pair.item2().mySQL));
			}
		}
		builder.append(String.format(" PRIMARY KEY (%s)", firstItem));
		builder.append(");");
		return new YSQLCommand(builder.toString());
	}

	public static YSQLCommand remove() {
		return null;
	}

	public static Pair<IStruct, Class<? extends IStruct>> getReverseTable(String name) {
		return reverseTables.get(name);
	}

	public static String getTable(Class<? extends IStruct> clazz) {
		return tables.get(clazz);
	}

	public enum FieldType {

		/*
		 * based on
		 * https://www.w3schools.com/sql/sql_datatypes.asp
		 */
		STRING(String.class, "TEXT", true),
		BOOLEAN(boolean.class, "BOOLEAN", false),
		BYTE(byte.class, "TINYINT(10)", false),
		SHORT(short.class, "SMALLINT(10)", false),
		INT(int.class, "INT(10)", false),
		LONG(long.class, "BIGINT(10)", false),
		FLOAT(float.class, "FLOAT(24)", false),
		DOUBLE(double.class, "FLOAT(53)", false),
		// these are here so you can make an Integer with a null value for example.
		UNBOXED_BOOLEAN(Boolean.class, "BOOLEAN", true),
		UNBOXED_BYTE(Byte.class, "TINYINT(10)", true),
		UNBOXED_SHORT(Short.class, "SMALLINT(10)", true),
		UNBOXED_INT(Integer.class, "INT(10)", true),
		UNBOXED_LONG(Long.class, "BIGINT(10)", true),
		UNBOXED_FLOAT(Float.class, "FLOAT(24)", true),
		UNBOXED_DOUBLE(Double.class, "FLOAT(53)", true);

		Class<?> type;
		String mySQL;
		public boolean nullable;

		FieldType(Class<?> clazz, String mySQLType, boolean nullable) {
			this.type = clazz;
			this.mySQL = mySQLType;
			this.nullable = nullable;
		}
	}

	public static void cleanup() {
		cache.clear();
		tables.clear();
		reverseTables.clear();
		Seriex.get().logger().info("Waiting 200 ms for DatabaseReflection to clean up properly.");
		kThread.sleep(200L);
	}
}
