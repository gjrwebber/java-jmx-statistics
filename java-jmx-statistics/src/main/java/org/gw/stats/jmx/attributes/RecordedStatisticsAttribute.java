/**
 * RollingAvgWindowStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.Statistic;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class RecordedStatisticsAttribute
        extends
        StatisticsAttribute<String, Statistic> {

    public RecordedStatisticsAttribute() {
        nameAddendum = ": Recorded";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public String getValue(Statistic stat) {
        return stat.getRecordedHistory().toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.attributes.StatisticsAttribute#
     * show(Statistic)
     */
    @Override
    public boolean show(Statistic statistic) {
        return statistic.getEnabled().get() && statistic.isRecording();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.attributes.StatisticsAttribute#
     * getNameAddendum(Statistic)
     */
    public String getNameAddendum(Statistic stat) {
        return nameAddendum + " (" + stat.getRecordingExpression() + ")";
    }

}
