/**
 * StatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.Statistic;

import javax.management.MBeanOperationInfo;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public abstract class StatisticOperation<T extends Statistic>
        extends
        StatisticsOperation<T> {

    protected String namePrefix;

    protected String descAddendum;

    public StatisticOperation() {
    }

    public MBeanOperationInfo getMBeanOperationinfo(String statName) {
        return new MBeanOperationInfo(namePrefix + statName, descAddendum
                + statName, params, "void", MBeanOperationInfo.ACTION);
    }

    public String getStatisticName(String actionName) {
        String statName = actionName;
        if (actionName.startsWith(namePrefix)) {
            statName = actionName.substring(namePrefix.length());
        }
        return statName;
    }

    public abstract void invoke(T statistic, Object[] args);

    public String getNamePrefix() {
        return namePrefix;
    }

    public String getDescAddendum() {
        return descAddendum;
    }

    public boolean show(T statistic) {
        return true;
    }

    public boolean reRegisterMBean(T statistic) {
        return false;
    }
}
