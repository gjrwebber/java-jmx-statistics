/**
 * StatsManagedResource.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.aop;

import javax.management.ObjectName;
import java.lang.annotation.*;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StatsManagedResource {
    /**
     * String representation of an {@link ObjectName} of this
     * {@link StatsManagedResource}
     *
     * @return The {@link ObjectName} as a {@link String}
     */
    String value() default "";
}
