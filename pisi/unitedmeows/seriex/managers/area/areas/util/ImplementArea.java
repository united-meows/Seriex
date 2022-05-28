package pisi.unitedmeows.seriex.managers.area.areas.util;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import pisi.unitedmeows.seriex.managers.area.areas.Area.Category;

@Retention(RUNTIME)
@Target(TYPE)
public @interface ImplementArea {
	String name();

	Category category();
}
