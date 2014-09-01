/**
 * ResetStatisticOperation.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.StatisticsDynamicMBean;

import javax.management.MBeanParameterInfo;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class SetAllWindowStatisticsOperation extends AllStatisticsOperation {

    public SetAllWindowStatisticsOperation() {
        name = "Set Time Window For All";
        params = new MBeanParameterInfo[]{new MBeanParameterInfo("Seconds",
                "java.lang.Integer", "Time window in Seconds")};
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.operations.StatisticsOperation#
     * sinvoke(Statistic,
     * java.lang.Object[])
     */
    @Override
    public void invoke(StatisticsDynamicMBean statistics, Object[] params) {
        if (params != null && params.length == 1) {
            Object param = params[0];
            if (param instanceof Integer) {
                int windowSecs = (Integer) param;

                statistics.setTimeWindowForAll(windowSecs);
            } else {
                throw new IllegalStateException(
                        "Could not Set Time Window For All"
                                + ". Wrong param type. Expected int, received "
                                + param.getClass().getSimpleName());
            }
        } else {
            throw new IllegalStateException("Could not Set Time Window For All"
                    + ". Wrong number of params.");
        }
    }

}
