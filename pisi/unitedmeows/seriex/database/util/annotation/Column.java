package pisi.unitedmeows.seriex.database.util.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
/**
 * you can use
 * <br>
 *
 * @Column(name = "anan") <br>
 *              <br>
 *              to override the name <br>
 *              it gets field name as default
 *              <br>
 *              if somehow it doesnt have any name for some reason default is "empty"
 */
public @interface Column {
	String name() default "empty";
}
