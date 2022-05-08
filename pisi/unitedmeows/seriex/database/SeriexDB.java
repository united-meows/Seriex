package pisi.unitedmeows.seriex.database;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.structs.impl.StructPlayer;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection.FieldType;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import pisi.unitedmeows.yystal.sql.YSQLCommand;
import pisi.unitedmeows.yystal.utils.Pair;

public class SeriexDB extends YDatabaseClient {
	public SeriexDB(String username, String password, String database, String host, int port) {
		super(username, password, database, host, port);
	}

	public SeriexDB(String username, String password, String database, String host) {
		super(username, password, database, host);
	}

	public SeriexDB(String username, String password, String database) {
		super(username, password, database);
	}

	public StructPlayer getPlayerW(YSQLCommand command) {
		try {
			Pair<IStruct, Class<? extends IStruct>> table = DatabaseReflection.getTable("player");
			String[] columns = table.item1().getColumns();
			List<Map<String, Object>> query = select(command, columns);
			if (query.isEmpty()) return null;
			Class<? extends IStruct> clazz = table.item2();
			StructPlayer structPlayerW = new StructPlayer();
			for (Pair<String, FieldType> item : DatabaseReflection.getColumnsFromClass(clazz).item2()) {
				String name = item.item1();
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				field.set(structPlayerW, query.get(0).get(field.getName()));
			}
			return structPlayerW;
		}
		catch (Exception e) {
			e.printStackTrace();
			//			Seriex.get().logger().fatal("Couldnt get player!");
			return null;
		}
	}

	public StructPlayer getPlayerW(String username) {
		return getPlayerW(new YSQLCommand("SELECT * FROM player WHERE username=^ LIMIT 1").putString(username));
	}

	public StructPlayer getPlayerWFromToken(String token) {
		return getPlayerW(new YSQLCommand("SELECT * FROM player WHERE token=^ LIMIT 1").putString(token));
	}
}
