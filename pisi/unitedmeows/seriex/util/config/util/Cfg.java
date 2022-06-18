package pisi.unitedmeows.seriex.util.config.util;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Cfg {
	String name();

	boolean multi();

	boolean manual();
}
