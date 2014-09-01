/**
 * RollingAvgWindowStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.AveragingStatistic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class RollingAvgStatisticsAttribute
        extends
        StatisticsAttribute<AtomicLong, AveragingStatistic> {

    public RollingAvgStatisticsAttribute() {
        nameAddendum = ": Rolling Avg";
        descAddendum = ": Rolling Average. Returns the number of invocations on "
                + "average over the time window since the application started.";
        // add(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public AtomicLong getValue(AveragingStatistic statistic) {
        AveragingStatistic rollingStat = (AveragingStatistic) statistic;
        return rollingStat.getRollingAvg();
    }

    /* (non-Javadoc)
     * @see org.gw.stats.attributes.StatisticsAttribute#show(Statistic)
     */
    @Override
    public boolean show(AveragingStatistic statistic) {
        return statistic.getEnabled().get() && statistic.isRolling();
    }

}
