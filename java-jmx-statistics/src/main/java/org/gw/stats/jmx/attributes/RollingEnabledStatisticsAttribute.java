/**
 * RollingAvgWindowStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.AveragingStatistic;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class RollingEnabledStatisticsAttribute
        extends
        StatisticsAttribute<AtomicBoolean, AveragingStatistic> {

    public RollingEnabledStatisticsAttribute() {
        nameAddendum = ": Rolling Enabled";
        descAddendum = ": Rolling Enabled. Returns true if this is enabled for Rolling Average.";
        // add(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public AtomicBoolean getValue(AveragingStatistic statistic) {
        return statistic.getRollingEnabled();
    }

    /* (non-Javadoc)
     * @see org.gw.stats.attributes.StatisticsAttribute#show(Statistic)
     */
    @Override
    public boolean show(AveragingStatistic statistic) {
        return statistic.getRollingEnabled().get();
    }

}
