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
public class DisableAllStatisticsOperation extends AllStatisticsOperation {

    public DisableAllStatisticsOperation() {
        name = "Disable All";
    }

    /* (non-Javadoc)
     * @see org.gw.stats.operations.StatisticsOperation#sinvoke(Statistic, java.lang.Object[])
     */
    @Override
    public void invoke(StatisticsDynamicMBean statistics, Object[] args) {
        statistics.disableAll();
    }

}
