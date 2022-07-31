package pisi.unitedmeows.seriex.util.unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.exceptions.SeriexError;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import sun.misc.Unsafe;

// todo: find original author
@SuppressWarnings("all")
public class UnsafeReflect {
	private static final Map<String, Field> stringToFieldMap = new HashMap<>();
	private static final Map<String, Long> stringToOffset = new HashMap<>();
	private static final Map<String, Object> stringToObject = new HashMap<>();
	private static final List<String> initCheck = new GlueList<>();
	private static final Unsafe UNSAFE = AccessController.doPrivileged((PrivilegedAction<Unsafe>) () -> {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		}
		catch (Exception exception) {
			throw new SeriexError("Access to Unsafe is unavailable!", exception);
		}
	});

	public static Object getFieldValue(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return getFieldValue0(clazz, fieldName);
		}
		catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() == clazz) throw e;
			else return getFieldValue(clazz.getSuperclass(), fieldName);
		}
	}

	public static void setFieldValue(Class<?> clazz, String fieldName, Object value) throws NoSuchFieldException {
		try {
			setFieldValue0(clazz, fieldName, value);
		}
		catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() == null) throw e;
			else {
				setFieldValue(clazz.getSuperclass(), fieldName, value);
			}
		}
	}

	private static Object getFieldValue0(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		long ms = System.currentTimeMillis();
		String name = clazz.getName();
		if (!initCheck.contains(name)) {
			UNSAFE.ensureClassInitialized(clazz);
			initCheck.add(name);
		}
		Field field = stringToFieldMap.computeIfAbsent(fieldName, fieldName_ -> {
			try {
				Field declaredField = clazz.getDeclaredField(fieldName_);
				declaredField.setAccessible(true);
				return declaredField;
			}
			catch (NoSuchFieldException
						| SecurityException e) {
				e.printStackTrace();
				return null;
			}
		});
		Class<?> type = field.getType();
		long staticFieldOffset = stringToOffset.computeIfAbsent(fieldName, troll -> UNSAFE.staticFieldOffset(field));
		Object staticFieldBase = stringToObject.computeIfAbsent(fieldName, troll -> UNSAFE.staticFieldBase(field));
		if (!type.isPrimitive()) return UNSAFE.getObject(staticFieldBase, staticFieldOffset);
		else if (type.equals(boolean.class)) return UNSAFE.getBoolean(staticFieldBase, staticFieldOffset);
		else if (type.equals(char.class)) return UNSAFE.getChar(staticFieldBase, staticFieldOffset);
		else if (type.equals(byte.class)) return UNSAFE.getByte(staticFieldBase, staticFieldOffset);
		else if (type.equals(short.class)) return UNSAFE.getShort(staticFieldBase, staticFieldOffset);
		else if (type.equals(int.class)) return UNSAFE.getInt(staticFieldBase, staticFieldOffset);
		else if (type.equals(long.class)) return UNSAFE.getLong(staticFieldBase, staticFieldOffset);
		else if (type.equals(float.class)) return UNSAFE.getFloat(staticFieldBase, staticFieldOffset);
		else if (type.equals(double.class)) return UNSAFE.getDouble(staticFieldBase, staticFieldOffset);
		else throw new SeriexException(String.format("Can not get value for %s field %s.%s", type, clazz.getName(), fieldName));
	}

	private static void setFieldValue0(Class<?> clazz, String fieldName, Object value) throws NoSuchFieldException {
		String name = clazz.getName();
		if (!initCheck.contains(name)) {
			UNSAFE.ensureClassInitialized(clazz);
			initCheck.add(name);
		}
		Field field = stringToFieldMap.computeIfAbsent(fieldName, fieldName_ -> {
			try {
				Field declaredField = clazz.getDeclaredField(fieldName_);
				declaredField.setAccessible(true);
				return declaredField;
			}
			catch (NoSuchFieldException
						| SecurityException e) {
				e.printStackTrace();
				return null;
			}
		});
		long staticFieldOffset = stringToOffset.computeIfAbsent(fieldName, troll -> UNSAFE.staticFieldOffset(field));
		Object staticFieldBase = stringToObject.computeIfAbsent(fieldName, troll -> UNSAFE.staticFieldBase(field));
		Class<?> type = field.getType();
		if (!type.isPrimitive()) {
			UNSAFE.putObject(staticFieldBase, staticFieldOffset, validFor(value, field));
		} else if (type.equals(boolean.class) && value instanceof Boolean) {
			UNSAFE.putBoolean(staticFieldBase, staticFieldOffset, (Boolean) value);
		} else if (type.equals(char.class) && value instanceof Character) {
			UNSAFE.putChar(staticFieldBase, staticFieldOffset, ((Character) value));
		} else {
			Number number = (Number) value;
			if (type.equals(byte.class) && value instanceof Number) {
				UNSAFE.putByte(staticFieldBase, staticFieldOffset, number.byteValue());
			} else if (type.equals(short.class) && value instanceof Number) {
				UNSAFE.putShort(staticFieldBase, staticFieldOffset, number.shortValue());
			} else if (type.equals(int.class) && value instanceof Number) {
				UNSAFE.putInt(staticFieldBase, staticFieldOffset, number.intValue());
			} else if (type.equals(long.class) && value instanceof Number) {
				UNSAFE.putLong(staticFieldBase, staticFieldOffset, number.longValue());
			} else if (type.equals(float.class) && value instanceof Number) {
				UNSAFE.putFloat(staticFieldBase, staticFieldOffset, number.floatValue());
			} else if (type.equals(double.class) && value instanceof Number) {
				UNSAFE.putDouble(staticFieldBase, staticFieldOffset, number.doubleValue());
			} else throw new SeriexException(String.format("Can not set %s field %s.%s to %s", type, clazz.getName(), fieldName, value.getClass().getName()));
		}
	}

	private static Object validFor(Object value, Field field) throws NoSuchFieldException {
		if (value != null && !field.getType().isAssignableFrom(value.getClass())) throw new NoSuchFieldException("Cannot assign a " + value.getClass() + " to field " + field.getName());
		return value;
	}

	public static Unsafe getUnsafe() {
		return UNSAFE;
	}
}
