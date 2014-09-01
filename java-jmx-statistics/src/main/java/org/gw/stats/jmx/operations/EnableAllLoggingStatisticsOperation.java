/**
 * ResetStatisticOperation.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.StatisticsDynamicMBean;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class EnableAllLoggingStatisticsOperation extends AllStatisticsOperation {

    public EnableAllLoggingStatisticsOperation() {
        name = "Enable All Logging";
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
    public void invoke(StatisticsDynamicMBean statistic, Object[] args) {
        statistic.enableAllLogging();
    }

    public boolean reRegisterAll() {
        return true;
    }
}
