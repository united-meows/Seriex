package test;

import java.lang.reflect.Field;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;
import pisi.unitedmeows.seriex.util.config.impl.server.DatabaseConfig;

public class ConfigTest {
	public static void main(String... args) throws Exception {
		DatabaseConfig o = new DatabaseConfig(null);
		o.loadDefaultValues();
		Field[] fields = ((Class<? extends Config>) o.getClass()).getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			field.setAccessible(true);
			if (field.getAnnotation(ConfigField.class) != null) {
				ConfigValue<?> fieldValue = (ConfigValue) field.get(o);
				Seriex.logger().debug("field %s, key %s, value %s", field.getName(), fieldValue.key(), fieldValue.value());
			}
		}
	}
}
