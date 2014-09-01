/**
 * EnabledStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.Statistic;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class EnabledStatisticsAttribute
        extends
        StatisticsAttribute<AtomicBoolean, Statistic> {

    public EnabledStatisticsAttribute() {
        nameAddendum = ": Enabled";
        descAddendum = ": Is Enabled? Returns true if this is enabled.";
        // add(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public AtomicBoolean getValue(Statistic statistic) {
        return statistic.getEnabled();
    }

}
