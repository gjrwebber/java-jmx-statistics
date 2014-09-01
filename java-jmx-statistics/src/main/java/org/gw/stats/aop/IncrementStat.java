package org.gw.stats.aop;

import org.gw.stats.AveragingStatistic;
import org.gw.stats.Statistic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be added to methods you want a set of stats to be kept and
 * incremented when various conditions are met.
 *
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface IncrementStat {

    /**
     * The name of the statistic. This is used as the display name of this
     * statistic in the JMX MBeanInfo.
     */
    String value() default "";

    /**
     * The {@link Throwable} that will result in the stats being incremented.
     * <p/>
     * By default DummyStatsException.class is returned, meaning the stats will
     * not be incremented unless a {@link Throwable} is explicitly set.
     */
    Class<? extends Throwable> throwing() default DummyStatsException.class;

    /**
     * Spring Expression Language (SpEL) attribute used for conditional
     * incrementing. All method parameters can be access using the #paramName.
     * <p/>
     * The resulting Object of the method call can also be using the expression
     * using the reserved keyword parameter #result. If one of the method
     * parameter names is result, the results of this conditional are not
     * guaranteed.
     * <p/>
     * Eg. The method public String doSomething(String name) can be used in the
     * expression as so:
     * <p/>
     * <code>@IncrementStats(value="Name Test", condition = "#name == \"Barry\"" &&
     * #result == \"Bill\"")</code>
     * <p/>
     * This will result in the stats being incremented if the method is invoked
     * with <code>doSomething("Barry")</code> and the result String equals
     * "Bill".
     * <p/>
     * <strong>Note:</strong> If the method return nothing, aka, void, then the
     * #result conditional will always return true.
     * <p/>
     * Default is "", the stats are always incremented.
     */
    String condition() default "";

    /**
     * Display a rolling average of the statistic over the time returned by this
     * parameter.
     * <p/>
     * <strong>Note:</strong> The avg is calculated each time the statistic is
     * incremented, not when are looking at the result. So, the avg is only
     * accurate up to the last time it was incremented.
     * <p/>
     * By default NONE is returned and means no rolling avg required
     */
    AveragingStatistic.ROLLING_AVG_WINDOW rollingAvgWindow() default AveragingStatistic.ROLLING_AVG_WINDOW.NONE;

    /**
     * The logging level. Defaults to NONE.
     */
    Statistic.LOG_LEVEL logLevel() default Statistic.LOG_LEVEL.NONE;

    /**
     * Capture the value returned by this expression and display the history
     * over the last maxRecord
     *
     * @return
     */
    String recordExp() default "";

    /**
     * @return
     */
    int maxRecording() default 100;
}
