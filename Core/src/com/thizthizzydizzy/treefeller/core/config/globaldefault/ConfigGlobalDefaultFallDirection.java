package com.thizthizzydizzy.treefeller.core.config.globaldefault;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FallDirection;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigGlobalDefaultFallDirection{
    public FallDirection value();
}
