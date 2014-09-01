/**
 * StatsSource.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.aop;

import java.util.Map;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public interface AnnotatedStatisticsSource {

    /**
     * Returns a mapping of MBean ObjectName string to target {@link Class}.
     *
     * @return
     */
    Map<String, Class<?>> getStatsBeans();
}
