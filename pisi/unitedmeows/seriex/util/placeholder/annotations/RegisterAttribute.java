package pisi.unitedmeows.seriex.util.placeholder.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

@Target(FIELD)
@Retention(RUNTIME)
public @interface RegisterAttribute {
    PlayerW.Attributes Enum();
}
