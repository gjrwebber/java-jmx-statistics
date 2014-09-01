/**
 * ResetStatisticOperation.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.Statistic;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class EnableStatisticsOperation extends StatisticOperation<Statistic> {

    public EnableStatisticsOperation() {
        namePrefix = "Enable: ";
    }

    /* (non-Javadoc)
     * @see org.gw.stats.operations.StatisticsOperation#sinvoke(Statistic, java.lang.Object[])
     */
    @Override
    public void invoke(Statistic statistic, Object[] args) {
        statistic.enable();
    }

    /* (non-Javadoc)
     * @see org.gw.stats.operations.StatisticOperation#reRegisterMBean(Statistic)
     */
    @Override
    public boolean reRegisterMBean(Statistic statistic) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.gw.stats.operations.StatisticOperation#show(Statistic)
     */
    @Override
    public boolean show(Statistic statistic) {
        return !statistic.getEnabled().get();
    }

}
