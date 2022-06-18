package pisi.unitedmeows.seriex.util.config.util;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// this sucks because:
// 1 - it might not be async-compatible
// 2 - might fuck up in java 8+
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigField {
}
