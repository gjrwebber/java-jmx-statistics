package org.gw.stats;

import org.gw.stats.AveragingStatistic.ROLLING_AVG_WINDOW;
import org.gw.stats.Statistic.LOG_LEVEL;
import org.gw.stats.aop.IncrementStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * {@link StatisticsService} that increments statistics on demand and makes them
 * available via JMX. The stats can also be logged to a file with the message
 * "Statistic logging: {statName} = {count}". This can be changed by setting the
 * logMessage parameter.
 * <p/>
 * This class may be overridden to provide extra functionality.
 *
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class JMXStatisticsService implements StatisticsService {

    private static Logger logger = LoggerFactory
            .getLogger(JMXStatisticsService.class);

    /**
     * The {@link StatisticsDynamicMBean} cache keyed by the {@link Class}
     */
    protected Map<Class<?>, StatisticsDynamicMBean> statsMBeansMap = new ConcurrentHashMap<Class<?>, StatisticsDynamicMBean>();
    /**
     * {@link ExecutorService} used to run command on the various statistics
     * in another {@link Thread}.
     */
    protected ExecutorService executorService;
    /**
     * When logging is turned on for an IncrementStat, then this is the message
     * that is logged. It is formatted using <code>IncrementStat.name()</code>
     * and the statistic as a number.
     */
    private String logMessage = System.getProperty("statistics.log.format",
            "Statistic logging: %s = %s");

    /**
     * Constructor initialising the {@link ExecutorService}.
     */
    public JMXStatisticsService() {
        logger.info("JMXStatisticsService instantiating...");
        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "JMXStatisticsService Thread");
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Returns an unmodifiable version of the <code>stats</code> {@link Map}
     *
     * @return
     */
    public Map<Class<?>, StatisticsDynamicMBean> getStatsMBeansMap() {
        return Collections.unmodifiableMap(statsMBeansMap);
    }

    /**
     * Create and add the given {@link Class} with the given {@link ObjectName}
     * String as an MBean on the MBeanServer. Only if the {@link Class} has at
     * least one method annotated with either {@link IncrementStats} or
     * {@link org.gw.stats.aop.IncrementStat}
     * .
     *
     * @param objectName  The String representing the {@link ObjectName}
     * @param targetClass The {@link Class} to add
     * @return
     */
    public StatisticsDynamicMBean createStatsMBeanForObjectName(
            String objectName, Class<?> targetClass) {

        StatisticsDynamicMBean st = statsMBeansMap.get(targetClass);

        // Create a Stats object for the given ObjectName if one doesn't already
        // exist
        if (st == null) {

            if (logger.isDebugEnabled()) {
                logger.debug(String.format(
                        "Creating StatisticsDynamicMBean for %s as %s",
                        targetClass, objectName));
            }

            st = new StatisticsDynamicMBean(objectName, targetClass);

            // Register the MBean for this Stats object
            st.registerStatsMBean();

            // Add the new Stats object to the stats Map using the ObjectName as
            // the key
            statsMBeansMap.put(targetClass, st);

        }
        return st;
    }

    /**
     * Creates and add an MBean using the {@link ObjectName} from the given
     * targetClass.
     *
     * @param targetClass The target {@link Class}
     */
    protected StatisticsDynamicMBean addClass(Class<?> targetClass) {
        String objectName = targetClass.getPackage().getName() + ".stats:type="
                + targetClass.getSimpleName();
        return createStatsMBeanForObjectName(objectName, targetClass);
    }

    @Override
    public StatisticsDynamicMBean createStatsMBean(String domainName,
                                                   Class<?> targetClass) {
        String appendedObjectName = domainName + ".stats:type="
                + targetClass.getSimpleName();
        return createStatsMBeanForObjectName(appendedObjectName, targetClass);
    }

    @Override
    public void initialiseStats(String statName, Class<?> targetClass,
                                LOG_LEVEL logLevel) {
        initialiseStats(statName, targetClass, null, logLevel);
    }

    @Override
    public void initialiseStats(String statName, Class<?> targetClass,
                                ROLLING_AVG_WINDOW rollingAvgWindow) {
        initialiseStats(statName, targetClass, rollingAvgWindow, LOG_LEVEL.NONE);
    }

    /**
     * initialise a statistic so as to make it accessible via JMX prior to the
     * first increment
     *
     * @param statName
     * @param targetClass
     * @param rollingAvgWindow
     * @param logLevel
     */
    @Override
    public void initialiseStats(String statName, Class<?> targetClass,
                                ROLLING_AVG_WINDOW rollingAvgWindow, LOG_LEVEL logLevel) {

        StatisticsDynamicMBean statistics = null;

        if (logger.isDebugEnabled()) {
            logger.debug("Initialising for " + targetClass.getSimpleName()
                    + ": " + statName + " with rollingAvgWindow="
                    + rollingAvgWindow + ", logLevel=" + logLevel);
        }

        synchronized (statsMBeansMap) {
            statistics = statsMBeansMap.get(targetClass);
            if (statistics == null) {
                statistics = addClass(targetClass);
            }
        }
        if (statistics.getStatistic(statName) == null) {
            statistics.addStat(
                    statName,
                    createStatistic(statName, null, null, rollingAvgWindow,
                            logLevel));
            statistics.registerStatsMBean();
        }
    }

    @Override
    public void initialiseStats(String statName, Class<?> targetClass) {
        initialiseStats(statName, targetClass, null, LOG_LEVEL.NONE);
    }

    /**
     * Increments the {@link StatisticsDynamicMBean} counter mapped by the given
     * name and target Object.
     *
     * @param statName    The name of the statistic as a {@link String}
     * @param targetClass The target {@link Class}
     */
    @Override
    public StatisticsDynamicMBean incrementStats(String statName,
                                                 Class<?> targetClass) {
        return incrementStats(1, statName, targetClass);
    }

    /**
     * @param increment   The amount to increment the statistic
     * @param statName    The name of the statistic as a {@link String}
     * @param targetClass The target {@link Class}
     * @return
     */
    @Override
    public StatisticsDynamicMBean incrementStats(int increment,
                                                 String statName, Class<?> targetClass) {
        StatisticsDynamicMBean statistics = null;
        statistics = statsMBeansMap.get(targetClass);
        if (statistics == null) {
            initialiseStats(statName, targetClass);
            statistics = statsMBeansMap.get(targetClass);
        }
        Statistic statistic = statistics.getStatistic(statName);
        if (statistic == null) {
            // log error. though could throw exception instead
            logger.error("Failed to increment stats for "
                    + targetClass.getSimpleName()
                    + ": "
                    + statName
                    + " because the statistic with the given name could not be found.  Check that the statistic has been initialised.");
        } else {
            statistics.incrementStat(increment, null, statName);

            LOG_LEVEL logLevel = statistic.getLogLevel();
            if (logLevel != LOG_LEVEL.NONE) {
                    /*
                     * Log the new statistic if required
					 */
                log(statName, statistics, logLevel);
            }
        }

        return statistics;
    }

    /**
     * Creates a {@link Statistic}
     *
     * @param statName         The name of the statistic as a {@link String}
     * @param condition        The condition expression as an SPeL
     * @param throwing         The Throwable to increment on
     * @param rollingAvgWindow The rolling average
     * @param logLevel         The logging level
     * @return The newly create {@link Statistic}
     */
    public Statistic createStatistic(String statName, String condition,
                                     Class<? extends Throwable> throwing,
                                     ROLLING_AVG_WINDOW rollingAvgWindow, LOG_LEVEL logLevel) {
        return new AveragingStatistic(statName, condition, throwing, logLevel,
                rollingAvgWindow, executorService);
    }

    /**
     * Logs a message with the new statistic if the given
     * <code>IncrementStat.log()</code> return true.
     *
     * @param statName The name of the statistic as a {@link String}
     * @param stats    The {@link StatisticsDynamicMBean} object
     * @param logLevel The logging level
     */
    private void log(String statName, StatisticsDynamicMBean stats,
                     LOG_LEVEL logLevel) {
        Statistic stat = stats.getStatistic(statName);

        String message = String.format(logMessage, statName, stat.toString());

        if (logLevel == LOG_LEVEL.DEBUG) {
            stats.getLogger().debug(message);
        } else {
            stats.getLogger().info(message);
        }
    }

    /**
     * Resets the stats on the given target {@link Class} with the given
     * statistic name
     *
     * @param statName
     * @param targetClass The target {@link Class}
     */
    @Override
    public void reset(String statName, Class<?> targetClass) {
        StatisticsDynamicMBean statistics = statsMBeansMap.get(targetClass);
        if (statistics == null) {
            logger.warn("No Statistics object found on "
                    + targetClass.getClass().getSimpleName());
            return;
        }
        statistics.reset(statName);
    }

    /**
     * Resets all stats on the given target {@link Class}
     *
     * @param targetClass The target {@link Class}
     */
    @Override
    public void resetAll(Class<?> targetClass) {
        StatisticsDynamicMBean statistics = statsMBeansMap.get(targetClass);
        if (statistics == null) {
            logger.warn("No Statistics object found on "
                    + targetClass.getClass().getSimpleName());
            return;
        }
        statistics.resetAll();

    }

    /**
     * Resets all stats
     */
    @Override
    public void resetAll() {
        for (StatisticsDynamicMBean statistics : statsMBeansMap.values()) {
            statistics.resetAll();
        }
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

}
