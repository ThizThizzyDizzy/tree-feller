package com.thizthizzydizzy.treefeller.core.config.legacy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/** Container for repeated legacy option annotations on a configuration field. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LegacyBukkitOptions{
    LegacyBukkitOption[] value();
}
