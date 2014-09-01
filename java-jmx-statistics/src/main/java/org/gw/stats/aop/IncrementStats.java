package org.gw.stats.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be added to methods you want more than one set of stats to be
 * kept and incremented when various conditions are met.
 *
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface IncrementStats {

    /**
     * Container for holder as many IncrementStat annotations as necessary for
     * one method
     *
     * @return An array of IncrementStat annotations
     */
    IncrementStat[] value() default {};
}
