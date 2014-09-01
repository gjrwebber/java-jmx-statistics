package org.gw.stats.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.gw.stats.AveragingStatistic;
import org.gw.stats.JMXStatisticsService;
import org.gw.stats.Statistic;
import org.gw.stats.StatisticsDynamicMBean;
import org.gw.commons.utils.expression.TargetMethodCachedConditionalExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.ObjectName;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aspectj @Aspect that looks for all MBeans on the platforms mbean server whose
 * methods are annotated with {@link IncrementStat} or {@link IncrementStats}.
 * Those beans are then added to the platforms mbean server with ".stats" added
 * to the original domain of the MBean.
 *
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@Component
@Aspect
public class AnnotationDrivenJMXStatisticsService
        extends
        JMXStatisticsService {

    private static Logger logger = LoggerFactory
            .getLogger(AnnotationDrivenJMXStatisticsService.class);
    private final char namedExpressionPrefix = '#';
    private final Pattern namedExpression = Pattern.compile("(\\" + namedExpressionPrefix + ".*?)(\\s|$)");
    private final Map<String, List<String>> expressionCache = new HashMap<String, List<String>>();
    /**
     * The {@link TargetMethodCachedConditionalExpressionEvaluator} used in
     * evaluating conditional stats.
     */
    private TargetMethodCachedConditionalExpressionEvaluator evaluator = new TargetMethodCachedConditionalExpressionEvaluator();
    /**
     * A list of {@link AnnotatedStatisticsSource} for finding stats beans.
     */
    @Autowired(required = false)
    private List<AnnotatedStatisticsSource> statsSources = new ArrayList<AnnotatedStatisticsSource>();
    private AtomicBoolean initialised = new AtomicBoolean();

    /**
     * Initialises a {@link org.gw.stats.StatisticsDynamicMBean} object to track the
     * statistics of all mbeans on the running mbean server. The mbean classes
     * methods must be annotated with one of {@link IncrementStats} or
     * {@link IncrementStat}.
     */
//    @PostConstruct
    public void init() {
        if (!initialised.getAndSet(true)) {
            logger.info("Initialising AnnotationDrivenJMXStatisticsService...");

            if (statsSources.isEmpty()) {

                logger.info("No StatisticsSources configured. Adding MBeanServerStatisticsSource and StatsManagedResourceStatisticsSource");

                statsSources.add(new MBeanServerStatisticsSource());
                statsSources.add(new StatsManagedResourceStatisticsSource());
            }

			/*
             * Go through each StatsSource and add its Map of stats beans.
			 */
            for (AnnotatedStatisticsSource statsSource : statsSources) {

                logger.info(String.format("Adding Statistic sources form %s",
                        statsSource.getClass().getSimpleName()));

                for (String objName : statsSource.getStatsBeans().keySet()) {
                    createStatsMbeanForObjectNameIfAnnotated(objName,
                            statsSource.getStatsBeans().get(objName));
                }
            }
        }
    }

    /**
     * Add the given {@link Class} with the given {@link ObjectName} String only
     * if the Class has methods annotated with either {@link IncrementStats} or
     * {@link IncrementStat}.
     *
     * @param objectName  The String representing the {@link ObjectName}
     * @param targetClass The {@link Class} to add
     */
    private StatisticsDynamicMBean createStatsMbeanForObjectNameIfAnnotated(
            String objectName, Class<?> targetClass) {

        // get existing mbean if it exists
        StatisticsDynamicMBean statistics = statsMBeansMap.get(targetClass);
        boolean mbeanCreated = false;
        if (statistics == null) {
            // no mbean yet - create it
            statistics = new StatisticsDynamicMBean(objectName,
                    targetClass);
            mbeanCreated = true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String
                    .format("Looking to see if %s has any correctly annotated methods...",
                            targetClass));
        }

        createStatsForAnnotations(statistics, targetClass);

        if (statistics.getStatisticsCount() > 0) {

            // Create the MBean for this Stats object

            statistics.registerStatsMBean();

            // only add to map if newly created
            if (mbeanCreated) {
                // Add the new Stats object to the stats Map using the
                // ObjectName as the key
                statsMBeansMap.put(targetClass, statistics);
            }

        }
        return statistics;

    }

    private void createStatsForAnnotations(StatisticsDynamicMBean statistics, Class<?> targetClass) {

        // Go through each method in the bean and find the relevant stats
        // annotations
        for (Method method : targetClass.getDeclaredMethods()) {

            //Find all methods annotated with @IncrementStats
            IncrementStats stats = method.getAnnotation(IncrementStats.class);
            if (stats != null) {

                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Found %s on %s in %s", stats
                            .getClass().getSimpleName(), method, targetClass));
                }

				/*
                 * For each IncrementStat annotation in the array
				 */
                for (IncrementStat stat : stats.value()) {

                    if (stat != null) {

                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Found %s on %s in %s",
                                    stat.getClass().getSimpleName(), method,
                                    targetClass));
                        }

						/*
                         * Get the name of the stat and create it on the
						 * Statistics object
						 */
                        statistics.addStat(
                                stat.value(),
                                createStatistic(stat.value(), stat.condition(),
                                        stat.throwing(),
                                        stat.rollingAvgWindow(),
                                        stat.logLevel(), stat.recordExp(),
                                        stat.maxRecording()));
                    }
                }
            }

			/*
             * Find all methods annotated with @IncrementStat
			 */
            IncrementStat stat = method.getAnnotation(IncrementStat.class);

            if (stat != null) {

                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Found %s on %s in %s", stat

                            .getClass().getSimpleName(), method, targetClass));
                }

				/*
				 * Get the name of the stat and create it on the Statistics
				 * object
				 */
                statistics.addStat(
                        stat.value(),
                        createStatistic(stat.value(), stat.condition(),
                                stat.throwing(), stat.rollingAvgWindow(),
                                stat.logLevel(), stat.recordExp(),
                                stat.maxRecording()));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String
                    .format("Creating annotated StatisticsDynamicMBean for %s as %s",
                            targetClass, statistics.getObjectName()));
        }

        // Recursively call for parent class.
        Class<?> parentClass = targetClass.getSuperclass();
        if (!parentClass.equals(Object.class)) {
            createStatsForAnnotations(statistics, parentClass);
        }

    }

    /**
     * Creates either a {@link org.gw.stats.AveragingStatistic}
     *
     * @param statName
     * @param condition
     * @param throwing
     * @param rollingAvgWindow
     * @param logLevel
     * @param record
     * @param maxRecording
     * @return
     */
    public Statistic createStatistic(String statName, String condition,
                                     Class<? extends Throwable> throwing,
                                     AveragingStatistic.ROLLING_AVG_WINDOW rollingAvgWindow, Statistic.LOG_LEVEL logLevel, String record, int maxRecording) {
        return new AveragingStatistic(statName, condition, throwing,
                logLevel, rollingAvgWindow, executorService, record, maxRecording);
    }

    /**
     * Increments the stats mapped to the given targetClass. Only if the
     * condition of the stat met.
     *
     * @param targetClass The target {@link Class} of the stat
     * @param name        The name of the stat
     * @param method      The {@link Method}
     * @param args        The arguments of the method
     * @param retVal      The return value of the method
     * @return
     */
    public boolean incrementStats(Class<?> targetClass, String name,
                                  Method method, Object[] args, Object retVal,
                                  IncrementStat incrementStat) {

        if (!initialised.get()) {
            init();
        }

        StatisticsDynamicMBean statMBean = statsMBeansMap.get(targetClass);
        if (statMBean == null) {
            initialiseStats(name, targetClass);
            statMBean = statsMBeansMap.get(targetClass);
            createStatsMbeanForObjectNameIfAnnotated(statMBean.getObjectName(), targetClass);
        }

        Statistic stat = findStatistic(statMBean, name, method, args,
                targetClass, retVal, incrementStat);
        if (stat == null) {
            return false;
        }
        String condition = stat.getCondition();
        String history = incrementStat.recordExp();

        try {
            if (condition != null && condition.length() > 0) {

					/*
                     * If this is a condition
					 */
                if (evaluator.evaluate(condition, method, args,
                        targetClass, targetClass.getClass(), retVal)) {

                    Object capturedHistory = null;
                    if (history != null && history.length() > 0) {
                        capturedHistory = evaluator.getObject(history, method, args, args, targetClass, retVal);
                    }

                    statMBean.incrementStat(1, capturedHistory, stat);
                    return true;
                }
            } else {

                Object capturedHistory = null;
                if (history != null && history.length() > 0) {
                    capturedHistory = evaluator.getObject(history, method, args, args, targetClass, retVal);
                }

					/*
					 * If this is not a conditional
					 */
                statMBean.incrementStat(1, capturedHistory, stat);
                return true;
            }

        } catch (Exception e) {
            logger.warn(
                    "Caught exception while trying to increment stats on "
                            + targetClass.getClass().getSimpleName() + "."
                            + method.toGenericString(), e);
        }

        return false;
    }

    private Statistic findStatistic(StatisticsDynamicMBean statMBean,
                                    String name, Method method, Object[] args, Class<?> targetClass,
                                    Object retVal, IncrementStat incrementStat) {

		/*
		 * If the name does not have an expression in it, just return whats in
		 * the StatisticsDynamicMBean
		 */
        if (name.indexOf(namedExpressionPrefix) < 0) {
            return statMBean.getStatistic(name);
        }

        List<String> expressions = expressionCache.get(name);
        if (expressions == null) {
            expressions = new ArrayList<String>();
            Matcher m = namedExpression.matcher(name);
            while (m.find()) {
                expressions.add(m.group(1));
            }
            expressionCache.put(name, expressions);
        }
        for (String expression : expressions) {
            Object obj = evaluator.getObject(expression, method, args, null,
                    targetClass, retVal);
            name = name.replace(expression, obj.toString());
        }
        // String key = generateKey(name, expresson);
        Statistic stat = statMBean.getStatistic(name);
        if (stat == null) {
            stat = createStatistic(name, incrementStat.condition(),
                    incrementStat.throwing(), incrementStat.rollingAvgWindow(),
                    incrementStat.logLevel(), incrementStat.recordExp(), incrementStat.maxRecording());
            statMBean.addStat(name, stat);
            statMBean.registerStatsMBean();
        }

        return stat;

    }

    /**
     * Increments the array of stats after the proceeding method call or an
     * exception is thrown.
     *
     * @param jp
     * @param bean
     * @param stats The IncrementStats annotation holding an array of
     *              IncrementStat annotations
     * @return
     */
    @Around(value = "execution(* *(..)) && target(bean) && @annotation(stats)", argNames = "jp, bean, stats")
    public Object incrementStatsAroundMethod(ProceedingJoinPoint jp,
                                             Object bean, IncrementStats stats) throws Throwable {

        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass == null) {
            targetClass = bean.getClass();
        }

        try {
			/*
			 * Proceed with the target method call
			 */
            Object retVal = jp.proceed();

            for (IncrementStat stat : stats.value()) {

				/*
				 * Go through all IncrementStat annotations and try to
				 * increment. That is except for one that have a throwing
				 * argument other than DummyStatsException.
				 */
                Class<? extends Throwable> expected = stat.throwing();
                if (DummyStatsException.class.equals(expected)) {
                    incrementStats(targetClass, stat.value(),
                            ((MethodSignature) jp.getSignature()).getMethod(),
                            jp.getArgs(), retVal, stat);
                }
            }

            return retVal;
        } catch (Exception e) {

            for (IncrementStat stat : stats.value()) {

                Class<? extends Throwable> expected = stat.throwing();
				/*
				 * If an exception is thrown we want to test all IncrementStat
				 * annotations, not just the "throwing" annotations as some
				 * still may pass as they may not depend on a result.
				 */

                if (DummyStatsException.class.equals(expected)
                        || e.getClass().equals(expected)) {
                    incrementStats(targetClass, stat.value(),
                            ((MethodSignature) jp.getSignature()).getMethod(),
                            jp.getArgs(), null, stat);
                }
            }
            throw e;
        }

    }

    /**
     * Increments the stats after the proceeding method call or an exception is
     * thrown.
     *
     * @param jp
     * @param bean
     * @param stat The IncrementStat annotation
     * @return
     * @throws Throwable
     */
    @Around(value = "execution(* *(..)) && target(bean) && @annotation(stat)", argNames = "jp, bean, stat")
    public Object incrementStatAroundMethod(ProceedingJoinPoint jp,
                                            Object bean, IncrementStat stat) throws Throwable {

        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass == null) {
            targetClass = bean.getClass();
        }

        try {
			/*
			 * Proceed with the target method call
			 */
            Object retVal = jp.proceed();

			/*
			 * Attempt to increment the stat only if the IncrementStat did not
			 * specify a throwing parameter.
			 */
            Class<? extends Throwable> expected = stat.throwing();
            if (DummyStatsException.class.equals(expected)) {
                incrementStats(targetClass, stat.value(),
                        ((MethodSignature) jp.getSignature()).getMethod(),
                        jp.getArgs(), retVal, stat);
            }
            return retVal;
        } catch (Exception e) {

            Class<? extends Throwable> expected = stat.throwing();

			/*
			 * If an exception is thrown we want to test the IncrementStat
			 * annotation as it could depend on a throwable or a result.
			 */

            if (DummyStatsException.class.equals(expected)
                    || e.getClass().equals(expected)) {
                incrementStats(targetClass, stat.value(),
                        ((MethodSignature) jp.getSignature()).getMethod(),
                        jp.getArgs(), null, stat);
            }

            throw e;
        }

    }

    public void setStatsSources(AnnotatedStatisticsSource... statsSources) {
        this.statsSources = Arrays.asList(statsSources);
    }

    public void setStatsSources(List<AnnotatedStatisticsSource> statsSources) {
        this.statsSources = statsSources;
    }

    public void addStatsSource(AnnotatedStatisticsSource statsSource) {
        this.statsSources.add(statsSource);
    }

}
