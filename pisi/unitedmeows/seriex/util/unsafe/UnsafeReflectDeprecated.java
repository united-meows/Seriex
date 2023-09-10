package pisi.unitedmeows.seriex.util.unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.funkemunky.api.utils.com.google.gson.internal.JavaVersion;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.exceptions.SeriexError;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import sun.misc.Unsafe;

@SuppressWarnings("all")
public class UnsafeReflectDeprecated {
	private static final Map<String, Field> stringToFieldMap = new HashMap<>();
	private static final Map<String, Long> stringToOffset = new HashMap<>();
	private static final Map<String, Object> stringToObject = new HashMap<>();
	private static final List<String> initCheck = new GlueList<>();
	private static Class<Unsafe> UNSAFE_CLASS = Unsafe.class;
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
	private static final Field overrideField = getOverrideField(); // this may fail
	private static boolean compatability = JavaVersion.isJava9OrLater();

	/**
	 * @author GSON
	 **/
	public static void setAccessible(AccessibleObject object) {
		if (object.isAccessible())
			return;

		try {
			if (compatability && UNSAFE != null && overrideField != null) {
				Method method = UNSAFE_CLASS.getMethod("objectFieldOffset", Field.class);
				long overrideOffset = (Long) method.invoke(UNSAFE, overrideField);  // long overrideOffset = theUnsafe.objectFieldOffset(overrideField);
				Method putBooleanMethod = UNSAFE_CLASS.getMethod("putBoolean", Object.class, long.class, boolean.class);
				putBooleanMethod.invoke(UNSAFE, object, overrideOffset, true); // theUnsafe.putBoolean(ao, overrideOffset, true); }
			} else {
				object.setAccessible(true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.get().logger().error("Couldnt make object accessible.");
		}
	}

	private static Map<Pair<Class<?>, String>, Field> fieldCache = new HashMap<>();

	public static Field getField(Class<?> clazz, String fieldName) {
		return fieldCache.computeIfAbsent(Pair.of(clazz, fieldName), pair0 -> {
			try {
				Field declaredField = pair0.key().getDeclaredField(pair0.value());
				setAccessible(declaredField);
				return declaredField;
			}
			catch (NoSuchFieldException
						| SecurityException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	public static boolean setField(Field field, Object instance, Object value) {
		try {
			setAccessible(field);
			field.set(instance, value);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static <X> X newInstance(Class<? extends X> klass) {
		try {
			return klass.newInstance();
		}
		catch (Exception e) {
			try {
				return klass.getConstructor().newInstance();
			}
			catch (Exception e2) {
				e2.printStackTrace();
				e.printStackTrace();
				return null;
			}
		}
	}

	public static <T> T getField(Class<?> clazz, String fieldName, Object instance) {
		try {
			Field field = getField(clazz, fieldName);
			setAccessible(field);
			return (T) field.get(instance);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T getField(Field field, Object instance) {
		try {
			setAccessible(field);
			return (T) field.get(instance);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean callMethod(Method method, Object instance, Object... args) {
		try {
			method.invoke(instance, args);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static <T> T invokeMethod(Method method, Object instance, Object... args) {
		try {
			return (T) method.invoke(instance, args);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <X> X createInstance(Class<? extends X> klass) {
		try {
			return klass.newInstance();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * should be used only on static fields, else use getField
	 */
	public static Object getStaticFieldValue(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return getFieldValue0(clazz, fieldName);
		}
		catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() == clazz) throw e;
			else return getStaticFieldValue(clazz.getSuperclass(), fieldName);
		}
	}

	/**
	 * used only on static fields (maybe it also works on final fields), else use setField
	 */
	public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
		try {
			setFieldValue0(clazz, fieldName, value);
		}
		catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() == null)
				Seriex.get().logger().error("Couldnt find field: " + fieldName + " in class " + clazz.getName());
			else {
				setStaticFieldValue(clazz.getSuperclass(), fieldName, value);
			}
		}
	}

	private static Object getFieldValue0(Class<?> clazz, String fieldName) throws NoSuchFieldException {
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
		else throw SeriexException.create(String.format("Can not get value for %s field %s.%s", type, clazz.getName(), fieldName));
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
		} else if (type.equals(boolean.class) && value instanceof Boolean boolValue) {
			UNSAFE.putBoolean(staticFieldBase, staticFieldOffset, boolValue);
		} else if (type.equals(char.class) && value instanceof Character charValue) {
			UNSAFE.putChar(staticFieldBase, staticFieldOffset, charValue);
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
			} else throw SeriexException.create(String.format("Can not set %s field %s.%s to %s", type, clazz.getName(), fieldName, value.getClass().getName()));
		}
	}

	public static boolean doesClassExist(String name) {
		try {
			Class.forName(name, false, Thread.currentThread().getContextClassLoader());
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static Object validFor(Object value, Field field) throws NoSuchFieldException {
		if (value != null && !field.getType().isAssignableFrom(value.getClass())) throw new NoSuchFieldException("Cannot assign a " + value.getClass() + " to field " + field.getName());
		return value;
	}

	public static Unsafe getUnsafe() { return UNSAFE; }

	private static Field getOverrideField() {
		try {
			return AccessibleObject.class.getDeclaredField("override");
		}
		catch (Exception e) {
			return null;
		}
	}
}
