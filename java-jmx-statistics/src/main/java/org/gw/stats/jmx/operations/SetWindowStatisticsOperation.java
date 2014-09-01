/**
 * ResetStatisticOperation.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.operations;

import org.gw.stats.AveragingStatistic;

import javax.management.MBeanParameterInfo;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class SetWindowStatisticsOperation
        extends
        StatisticOperation<AveragingStatistic> {

    public SetWindowStatisticsOperation() {
        namePrefix = "Set Time Window: ";
        params = new MBeanParameterInfo[]{new MBeanParameterInfo("Seconds",
                "java.lang.Integer", "Time window in Seconds")};
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gw.stats.operations.StatisticsOperation#
     * sinvoke(Statistic,
     * java.lang.Object[])
     */
    @Override
    public void invoke(AveragingStatistic statistic, Object[] params) {
        if (params != null && params.length == 1) {
            Object param = params[0];
            if (param instanceof Integer) {
                int windowSecs = (Integer) param;

                statistic.setRollingTimeWindow(windowSecs * 1000);
                statistic.enableRolling();
            } else {
                throw new IllegalStateException("Could not Set Time Window"
                        + ". Wrong param type. Expected int, received "
                        + param.getClass().getSimpleName());
            }
        } else {
            throw new IllegalStateException("Could not Set Time Window"
                    + ". Wrong number of params.");
        }
    }

    /* (non-Javadoc)
     * @see org.gw.stats.operations.StatisticOperation#reregister(Statistic)
     */
    @Override
    public boolean reRegisterMBean(AveragingStatistic statistic) {
        return true;
    }

}
