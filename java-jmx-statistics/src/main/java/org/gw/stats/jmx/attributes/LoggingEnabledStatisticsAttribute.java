/**
 * LoggingEnabledStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.Statistic;
import org.gw.stats.Statistic.LOG_LEVEL;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class LoggingEnabledStatisticsAttribute
        extends
        StatisticsAttribute<String, Statistic> {

    public LoggingEnabledStatisticsAttribute() {
        nameAddendum = ": Logging Level";
        descAddendum = ": Returns the logging level (DEBUG, INFO).";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public String getValue(Statistic statistic) {
        return statistic.getLogLevel().toString();
    }

    /* (non-Javadoc)
     * @see org.gw.stats.attributes.StatisticsAttribute#show(Statistic)
     */
    @Override
    public boolean show(Statistic statistic) {
        return statistic.getLogLevel() != LOG_LEVEL.NONE;
    }

}
