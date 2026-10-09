package com.thizthizzydizzy.treefeller.core.config.legacy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/** A legacy Bukkit option accepted by the importer.
 * Place matches on their destination configuration field. The importer derives
 * the destination from that field's path, with an optional nested suffix.
 * Only annotate settings with supported imports. Unmatched input values are
 * reported as warnings by the importer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(LegacyBukkitOptions.class)
public @interface LegacyBukkitOption{
    String value();
    Scope[] scopes();
    /** Infer ordinary conversions from the destination type. Specify a handler
     * only for special semantics or a destination whose type is ambiguous. */
    Conversion conversion() default Conversion.AUTO;
    /** Nested destination within this field, such as ".min" or ".whitelist".
     * Item definition internals are platform-owned, so their paths are declared
     * here on the shared ToolConfiguration.item field as well.
     */
    String suffix() default "";
    enum Scope{GLOBAL, TOOL, TREE}
    enum Conversion{
        BOOLEAN, INTEGER, FLOAT, FLOAT32, LIST, INVERSE, SPAWN, SAPLING,
        BEHAVIOR, HURT, DIRECTION, CONVERSIONS, EFFECTS, DECORATIONS,
        TREE_SELECTOR, BIOMES, AUTO
    }
}
