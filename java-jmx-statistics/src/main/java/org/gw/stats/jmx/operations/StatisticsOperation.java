/**
 * StatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import javax.management.MBeanParameterInfo;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public abstract class StatisticsOperation<T> {

    protected MBeanParameterInfo[] params;

    public StatisticsOperation() {
    }

    public abstract String getStatisticName(String actionName);

    public abstract void invoke(T stats, Object[] args);

}
