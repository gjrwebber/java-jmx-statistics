/**
 * StatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.StatisticsDynamicMBean;

import javax.management.MBeanOperationInfo;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public abstract class AllStatisticsOperation
        extends
        StatisticsOperation<StatisticsDynamicMBean> {

    protected String name;

    protected String desc;

    public AllStatisticsOperation() {
    }

    public MBeanOperationInfo getMBeanOperationinfo() {
        return new MBeanOperationInfo(name, desc, params,
                "void", MBeanOperationInfo.ACTION);
    }

    public String getStatisticName(String actionName) {
        String statName = actionName;
        if (actionName.equals(name)) {
            statName = actionName.substring(name.length());
        }
        return statName;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean reRegisterAll() {
        return false;
    }
}
