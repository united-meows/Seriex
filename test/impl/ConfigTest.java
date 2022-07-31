package test.impl;

import java.lang.reflect.Field;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.server.BanActionsConfig;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;
import test.Test;
import test.TestSettings;
import test.TestState;

@TestSettings(hasArguments = false)
public class ConfigTest extends Test {
	@Override
	public TestState run() {
		try {
			BanActionsConfig o = new BanActionsConfig();
			o.loadDefaultValues();
			Field[] fields = ((Class<? extends Config>) o.getClass()).getDeclaredFields();
			boolean noAnnotationInSomeFields = false;
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				field.setAccessible(true);
				if (field.getAnnotation(ConfigField.class) != null) {
					ConfigValue<?> fieldValue = (ConfigValue) field.get(o);
					Seriex.logger().debug("field %s, key %s, value %s", field.getName(), fieldValue.key(), fieldValue.value());
				} else {
					if (!noAnnotationInSomeFields) {
						Seriex.logger().fatal("Field %s doesnt have annotation...", field.getName());
						noAnnotationInSomeFields = true;
					}
				}
			}
			return noAnnotationInSomeFields ? TestState.FAIL : TestState.SUCCESS;
		}
		catch (Exception exception) {
			message(exception);
			return TestState.FATAL_ERROR;
		}
	}
}
