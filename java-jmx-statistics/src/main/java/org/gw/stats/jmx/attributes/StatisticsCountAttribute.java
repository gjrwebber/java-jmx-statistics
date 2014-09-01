/**
 * RollingAvgWindowStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.Statistic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class StatisticsCountAttribute
        extends
        StatisticsAttribute<AtomicLong, Statistic> {

    public StatisticsCountAttribute() {
        nameAddendum = "";
//		add(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public AtomicLong getValue(Statistic statistic) {
        return statistic.getCount();
    }

}
