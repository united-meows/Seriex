package pisi.unitedmeows.seriex.database.util.reflection;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.matcher.FieldMatcher;
import io.github.classgraph.ClassGraph;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

public class DatabaseReflection {
	// class to fields cache
	private static final Map<Class<? extends IStruct>, List<DatabaseField>> FIELD_CACHE = new HashMap<>();
	// class to table name
	private static final BiMap<Class<? extends IStruct>, String> TABLES = HashBiMap.create();
	// field_types
	private static final FieldType[] FIELD_TYPES = FieldType.values();
	// to avoid initializing more than once
	private static boolean initialized;

	private DatabaseReflection() {}

	public static void init(SeriexDB database) {
		if (initialized) {
			Seriex.get().logger().error("DatabaseReflection already initialized.");
			return;
		}

		try (var result = new ClassGraph().enableAllInfo().acceptPackages("pisi.unitedmeows.seriex.database.structs.impl").scan(8)) {
			result.getClassesImplementing(IStruct.class)
						.stream()
						.filter(info -> info.hasAnnotation(Struct.class))
						.map(info -> (Class<? extends IStruct>) info.loadClass())
						.forEach((Class<? extends IStruct> clazz) -> {
				var tableName = clazz.getAnnotation(Struct.class).name();
				TABLES.put(clazz, tableName);
				database.execute(createStruct(clazz));
			});
			initialized = true;
		}
	}

	public static <X> X get(String tableName, YSQLCommand command, SeriexDB db, X struct) {
		try {
			var pair = DatabaseReflection.sendRequest(command, tableName, db);
			var query = pair.key(); // we get our result
			if (query.isEmpty()) { // no results
				Seriex.get().logger().error("Couldnt get data from the table {} from the database! ({})", tableName, command.getHooked());
				return null;
			}
			DatabaseReflection.setFields(query, pair.value(), (IStruct) struct); // set fields based on results
			return struct;
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.get().logger().error("Couldnt get data from the table {} from the database! ({})", tableName, command.getHooked());
			return null;
		}
	}

	private static Pair<List<Map<String, Object>>, Class<? extends IStruct>> sendRequest(YSQLCommand command, String tableName, SeriexDB db) {
		var reverseTable = DatabaseReflection.getReverseTable(tableName); // get fields from tableName
		var columns = reverseTable.parallelStream().map(DatabaseField::name).toArray(String[]::new); // get only field names
		return Pair.of(db.select(command, columns), reverseTable.get(0).ownerClass());
	}

	private static void setFields(List<Map<String, Object>> query, Class<? extends IStruct> clazz, IStruct struct) {
		var list = DatabaseReflection.getColumnsFromClass(clazz);
		for (DatabaseField item : list) {
			var name = item.name();
			var field = item.field();
			var value = query.get(0).get(field.getName());
			if (value == null && !item.type().nullable) {
				Seriex.get().logger().error("The value {} is not nullable and the value for {} in the database is null!", name, name);
				continue;
			}

			if(item.type() == FieldType.BOOLEAN && value instanceof Integer integer)
				value = integer != 0;

			Reflexion.unreflectField(field).setValue(struct, value);
		}
	}

	public static List<DatabaseField> getColumnsFromClass(Class<? extends IStruct> input) {
		return FIELD_CACHE.computeIfAbsent(input, clazz -> {
			List<DatabaseField> columns = new GlueList<>();
			Reflexion.on(clazz)
						.findFields(FieldMatcher.newMatcher().and(f -> f.isAnnotationPresent(Column.class)))
						.stream().sorted((a0, a1) -> {
							var allFields = Arrays.asList(clazz.getDeclaredFields());
							return Integer.compare(allFields.indexOf(a0.getMember()), allFields.indexOf(a1.getMember()));
						})
						.forEachOrdered(accessor -> {
				Field field = accessor.getMember();
				var annotation = field.getAnnotation(Column.class);
				var name = field.getName();
				FieldType foundFieldType = null;
				Class<?> type = field.getType();
				for (FieldType fieldType : FIELD_TYPES) {
					if (type.isAssignableFrom(fieldType.type)) {
						foundFieldType = fieldType;
						break;
					}
				}
				columns.add(DatabaseField.create(name, field, foundFieldType, input, annotation.primaryKey(), annotation.discriminator()));
			});
			return columns;
		});
	}



	public static YSQLCommand createStruct(Class<? extends IStruct> clazz) {
		var builder = new StringBuilder(MessageFormat.format("CREATE TABLE IF NOT EXISTS {0} (", TABLES.get(clazz)));
		for (DatabaseField field : getColumnsFromClass(clazz)) {
			var type = field.primaryKey() ? "INTEGER PRIMARY KEY AUTOINCREMENT" : field.type().sqlType;
			builder.append(String.format("%s %s,", field.name(), type));
		}
		builder.deleteCharAt(builder.length() - 1); // remove the last comma
		builder.append(");");
		return new YSQLCommand(builder.toString());
	}

	public static List<DatabaseField> getReverseTable(String name) {
		return FIELD_CACHE.get(TABLES.inverse().get(name));
	}

	public static String getTable(Class<? extends IStruct> clazz) {
		return TABLES.get(clazz);
	}

	public static Collection<String> tables() {
		return TABLES.values();
	}
}
