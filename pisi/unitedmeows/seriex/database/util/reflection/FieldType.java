package pisi.unitedmeows.seriex.database.util.reflection;

public enum FieldType {
	STRING(String.class , "TEXT" , true),
	BOOLEAN(boolean.class , "INT" , false),
	BYTE(byte.class , "TINYINT(10)" , false),
	SHORT(short.class , "SMALLINT(10)" , false),
	INT(int.class , "INT" , false),
	LONG(long.class , "BIGINT(10)" , false),
	FLOAT(float.class , "FLOAT(24)" , false),
	DOUBLE(double.class , "FLOAT(53)" , false),
	// these are here so you can make an Integer with a null value for example.
	UNBOXED_BOOLEAN(Boolean.class , "BOOLEAN" , true),
	UNBOXED_BYTE(Byte.class , "TINYINT(10)" , true),
	UNBOXED_SHORT(Short.class , "SMALLINT(10)" , true),
	UNBOXED_INT(Integer.class , "INT" , true),
	UNBOXED_LONG(Long.class , "BIGINT(10)" , true),
	UNBOXED_FLOAT(Float.class , "FLOAT(24)" , true),
	UNBOXED_DOUBLE(Double.class , "FLOAT(53)" , true);

	final Class<?> type;
	final String sqlType;

	final boolean nullable;

	FieldType(Class<?> clazz, String sqlType, boolean nullable) {
		this.type = clazz;
		this.sqlType = sqlType;
		this.nullable = nullable;
	}

	public boolean isNullable() {
		return nullable;
	}
}
