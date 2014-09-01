/**
 * ResetStatisticOperation.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.AveragingStatistic;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class DisableRollingStatisticsOperation extends StatisticOperation<AveragingStatistic> {

    public DisableRollingStatisticsOperation() {
        namePrefix = "Disable Rolling: ";
    }

    /* (non-Javadoc)
     * @see org.gw.stats.operations.StatisticsOperation#sinvoke(Statistic, java.lang.Object[])
     */
    @Override
    public void invoke(AveragingStatistic statistic, Object[] args) {
        statistic.disableRolling();
    }

    /* (non-Javadoc)
     * @see org.gw.stats.operations.StatisticOperation#reRegisterMBean(Statistic)
     */
    @Override
    public boolean reRegisterMBean(AveragingStatistic statistic) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.gw.stats.operations.StatisticOperation#show(Statistic)
     */
    @Override
    public boolean show(AveragingStatistic statistic) {
        return statistic.getRollingTimeWindow() <= 0;
    }

}
