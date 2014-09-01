/**
 * RollingAvgWindowStatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.AveragingStatistic;
import org.gw.commons.utils.StringUtils;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class RollingAvgWindowStatisticsAttribute
        extends
        StatisticsAttribute<String, AveragingStatistic> {

    public RollingAvgWindowStatisticsAttribute() {
        nameAddendum = ": Rolling Avg Window";
//		add(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.StatisticsAttribute#getValue(Statistic, java.lang.String)
     */
    @Override
    public String getValue(AveragingStatistic statistic) {
        AveragingStatistic rollingStat = (AveragingStatistic) statistic;
        return StringUtils.convertMillisToString(rollingStat
                .getRollingTimeWindow());
    }

    /* (non-Javadoc)
     * @see org.gw.stats.attributes.StatisticsAttribute#show(Statistic)
     */
    @Override
    public boolean show(AveragingStatistic statistic) {
        return statistic.getEnabled().get() && statistic.isRolling();
    }

}
