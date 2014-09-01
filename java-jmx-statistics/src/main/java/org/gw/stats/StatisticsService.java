/**
 * StatisticsService.java (c) Copyright 2013 Graham Webber
 */
package org.gw.stats;

import java.util.Map;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public interface StatisticsService {

    /**
     * create the statistics MBean under a specified domain name
     *
     * @param domainName  the MBean domain name
     * @param targetClass the MBean's class
     * @return the MBean that was created
     * @note this call is optional; if not called prior to initialiseStats, the
     * default objectName will be used, ie. the package name + targetClass
     * + .stats
     */
    StatisticsDynamicMBean createStatsMBean(String domainName,
                                            Class<?> targetClass);

    /**
     * initialise a statistic for the given MBean and log each increment
     *
     * @param statName    the name of the statistic
     * @param targetClass the MBean's class
     * @param logLevel    the level of logging to perform at each increment
     */
    void initialiseStats(String statName, Class<?> targetClass,
                         Statistic.LOG_LEVEL logLevel);

    /**
     * initialise a statistic for the given MBean and calculate a rolling
     * average
     *
     * @param statName         the name of the statistic
     * @param targetClass      the MBean's class
     * @param rollingAvgWindow the sliding window used to calculate the average
     */
    void initialiseStats(String statName, Class<?> targetClass,
                         AveragingStatistic.ROLLING_AVG_WINDOW rollingAvgWindow);

    /**
     * initialise a statistic for the given MBean, calculate a rolling average,
     * and log each increment
     *
     * @param statName         the name of the statistic
     * @param targetClass      the MBean's class
     * @param rollingAvgWindow the sliding window used to calculate the average
     * @param logLevel         the level of logging to perform at each increment
     */
    void initialiseStats(String statName, Class<?> targetClass,
                         AveragingStatistic.ROLLING_AVG_WINDOW rollingAvgWindow, Statistic.LOG_LEVEL logLevel);

    /**
     * initialise a statistic for the given MBean
     *
     * @param statName    the name of the statistic
     * @param targetClass the MBean's class
     */
    void initialiseStats(String statName, Class<?> targetClass);

    /**
     * increment the statistic by one
     *
     * @param statName    the name of the statistic to increment
     * @param targetClass the MBean's class
     * @return the MBean that was incremented
     */
    StatisticsDynamicMBean incrementStats(String statName, Class<?> targetClass);

    /**
     * increment the statistic by a specified amount
     *
     * @param increment   the amount to increment
     * @param statName    the name of the statistic to increment
     * @param targetClass the MBean's class
     * @return the MBean that was incremented
     */
    StatisticsDynamicMBean incrementStats(int increment, String statName,
                                          Class<?> targetClass);

    /**
     * Returns the {@link StatisticsDynamicMBean} map keyed by the target
     * {@link Class}
     *
     * @return
     */
    Map<Class<?>, StatisticsDynamicMBean> getStatsMBeansMap();

    /**
     * reset all stats
     */
    void resetAll();

    /**
     * reset all stats in the specified class
     *
     * @param targetClass
     */
    void resetAll(Class<?> targetClass);

    /**
     * reset the specified stat
     *
     * @param statName
     * @param targetClass
     */
    void reset(String statName, Class<?> targetClass);
}
