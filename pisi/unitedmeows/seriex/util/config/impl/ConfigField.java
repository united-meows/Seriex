package pisi.unitedmeows.seriex.util.config.impl;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// this sucks because:
// 1 - it might not be async-compatible
// 2 - might fuck up in java 8+
// 3 - we cant add every primitive & non-primitive
// (boolean, char, byte, short, int, long, float , double, String, arrays, etc...) data type
// i wish @ConfigField(key = "anan", defaultValue = type) was possible ;(
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigField {
}
