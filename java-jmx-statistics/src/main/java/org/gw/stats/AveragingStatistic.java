package org.gw.stats;

import org.aspectj.bridge.ICommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Statistic} that retains history so that a rolling average can be
 * calculated.
 *
 * @author Gman
 */
public class AveragingStatistic extends Statistic {

    private static Logger logger = LoggerFactory
            .getLogger(AveragingStatistic.class);
    /**
     * The {@link ROLLING_AVG_WINDOW} for this statistic
     */
    private ROLLING_AVG_WINDOW rollingAvgWindow;
    /**
     * The {@link TreeMap} containing history of statistics keyed by the date in
     * millis since epoch and the value being the statistic.
     */
    private TreeMap<Long, Long> history = new TreeMap<Long, Long>();
    /**
     * Contains the current rolling average of the statistic.
     */
    private AtomicLong rollingAvg = new AtomicLong();
    /**
     * An optional {@link ExecutorService} to run the cleanCommand and
     * averageCommand if performance is an isssue.
     */
    private ExecutorService executor;
    /**
     * Flag which is used to determine if rolling is enabled.
     */
    private AtomicBoolean rollingEnabled = new AtomicBoolean(false);
    /**
     * The rolling time window for this statistic
     */
    private AtomicLong rollingTimeWindow = new AtomicLong();
    /**
     * The {@link ICommand} to clean the history
     */
    private Runnable cleanCommand = new Runnable() {
        @Override
        public void run() {
            cleanHistory();
        }
    };
    /**
     * The {@link ICommand} to calculate the average on the history
     */
    private Runnable averageCommand = new Runnable() {
        @Override
        public void run() {
            calculateAverage();
        }
    };
    private Date startTime = new Date();

    /**
     * @param name             The name of the statistic as a {@link String}
     * @param condition        The condition expression as an SPeL
     * @param throwing         The Throwable to increment on
     * @param rollingAvgWindow The rolling average
     * @param logLevel         The logging level
     * @param executor         The ExecutorService to use for running asynchronous commands
     */
    public AveragingStatistic(String name, String condition,
                              Class<? extends Throwable> throwing, LOG_LEVEL logLevel,
                              ROLLING_AVG_WINDOW rollingAvgWindow, ExecutorService executor) {
        this(name, condition, throwing, logLevel, rollingAvgWindow, executor,
                null, 0);
    }

    /**
     * @param name                The name of the statistic as a {@link String}
     * @param condition           The condition expression as an SPeL
     * @param throwing            The Throwable to increment on
     * @param rollingAvgWindow    The rolling average
     * @param logLevel            The logging level
     * @param executor            The ExecutorService to use for running asynchronous commands
     * @param recordingExpression The expression string for recording objects
     * @param maxRecording        The maximum number of recorded object to keep in memory
     */
    public AveragingStatistic(String name, String condition,
                              Class<? extends Throwable> throwing, LOG_LEVEL logLevel,
                              ROLLING_AVG_WINDOW rollingAvgWindow, ExecutorService executor,
                              String recordingExpression, int maxRecording) {
        super(name, condition, throwing, logLevel, recordingExpression,
                maxRecording);
        setRollingAvgWindow(rollingAvgWindow);
        this.executor = executor;
    }

    /**
     * Increments the count for this statistic and adds it to the history. If it
     * is keeping a rolling history, the enqueue the clean and average
     * {@link ICommand}s.
     */
    public long increment(int increment, Object capturedObject) {

        long result = 0;
        if (enabled.get()) {
            result = super.increment(increment, capturedObject);

            if (isRolling()) {

                long updateTime = System.currentTimeMillis();

                synchronized (history) {
                    history.put(updateTime, result);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Rolling on " + this);
                }

                runCommands();
            }
        }
        return result;
    }

    /**
     * Runs the <code>cleanCommand</code> and <code>averageCommand</code> either
     * in a separate {@link Thread} if an {@link ExecutorService} exists, or the
     * current {@link Thread}.
     */
    public void runCommands() {

		/*
         * If an ExecutorService is available run the Commands through it,
		 * otherwise, just run them in this Thread.
		 */
        if (executor != null) {
            executor.execute(cleanCommand);
            executor.execute(averageCommand);
        } else {
            cleanCommand.run();
            averageCommand.run();
        }
    }

    /**
     * @return the rollingAvgWindow
     */
    public ROLLING_AVG_WINDOW getRollingAvgWindow() {
        return rollingAvgWindow;
    }

    void setRollingAvgWindow(ROLLING_AVG_WINDOW rollingAvgWindow) {
        this.rollingAvgWindow = rollingAvgWindow;
        if (rollingAvgWindow != null
                && rollingAvgWindow != ROLLING_AVG_WINDOW.NONE) {
            rollingEnabled.set(true);
            this.rollingTimeWindow.set(rollingAvgWindow.millis);
        }
    }

    public long getRollingTimeWindow() {
        return rollingTimeWindow.get();
    }

    public void setRollingTimeWindow(long rollingTimeWindow) {
        this.rollingTimeWindow.set(rollingTimeWindow);
    }

    /**
     * Returns an unmodifiable {@link Collection} of {@link Long}'s representing
     * the history of statistics. This Collection is ordered from oldest to
     * newest.
     *
     * @return ordered unmodifiable {@link Collection} of {@link Long}'s
     */
    public List<Long> getHistoricValues() {
        synchronized (history) {
            return new ArrayList<Long>(history.values());
        }
    }

    /**
     * Calculates the average on the total count as it stands at this moment.
     */
    public void calculateAverage() {
        calculateAverage(new Date());
    }

    /**
     * Calculates the average on the total count as it stands at the given
     * {@link Date}
     */
    public void calculateAverage(Date date) {
        double windowPeriodsSinceStart = getWindowPeriodsSinceStart(date);

        // If its the first window, set to 1 so we get a running average.
        if (windowPeriodsSinceStart < 1) {
            windowPeriodsSinceStart = 1;
        }
        calculateAverage(windowPeriodsSinceStart);
    }

    /**
     * Returns the number of windows since the start to the given {@link Date}
     *
     * @param date The date to calculate the number of windows from the start
     *             {@link Date}
     * @return Returns the number of windows since the start to the given
     * {@link Date}
     */
    private double getWindowPeriodsSinceStart(Date date) {
        if (rollingTimeWindow.get() > 0) {
            long runningTime = date.getTime() - startTime.getTime();
            return Math.floor(runningTime / rollingTimeWindow.get());
        } else {
            return 0;
        }
    }

    /**
     * Calculates the average on the total count as it stands over the number of
     * windowPeriods since the start of time
     *
     * @param windowPeriods The number of periods to average over
     */
    public void calculateAverage(double windowPeriods) {

        if (logger.isDebugEnabled()) {
            logger.debug("Calculating average with " + count.get() + "/"
                    + windowPeriods);
        }
        if (windowPeriods <= 0) {
            windowPeriods = 1;
        }

        rollingAvg.set(Math.round(count.get() / windowPeriods));

    }

    /**
     * Cleans the history by removing the key-value pairs whose Date is older
     * that the current time - rollingAvg.
     */
    public void cleanHistory() {

        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning history on " + this);
        }
        /* Get newest Date */
        long newestTime = history.lastKey();

        long expiryInMillis = newestTime - rollingTimeWindow.get();

        cleanHistory(expiryInMillis);
    }

    /**
     * Cleans the history so that any stat older than expiry will be removed.
     *
     * @param expiryTimeInMillis The expiry time in milliseconds
     */
    public void cleanHistory(long expiryTimeInMillis) {

        int cleaned = 0;
        synchronized (history) {
            Iterator<Long> it = history.keySet().iterator();
            /*
             * Go through the history and remove any keyset older than the
			 * expiry. Once we hit a Date that has not expired we can break as
			 * they should be ordered from oldest to newest.
			 */
            while (it.hasNext()) {
                Long time = it.next();
                if (time < expiryTimeInMillis) {
                    it.remove();
                    cleaned++;
                } else {
                    break;
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Cleaned " + cleaned + " historic values on " + this);
        }
    }

    /**
     * @return
     */
    public AtomicLong getRollingAvg() {
        return rollingAvg;
    }

    /**
     * Returns true if this {@link AveragingStatistic} should be averaged
     *
     * @return true if this {@link AveragingStatistic} should be averaged
     */
    public boolean isRolling() {
        return rollingEnabled.get()
                && rollingAvgWindow != ROLLING_AVG_WINDOW.NONE
                && rollingTimeWindow.get() > 0;
    }

    public AtomicBoolean getRollingEnabled() {
        return rollingEnabled;
    }

    public void disableRolling() {
        if (rollingEnabled.get()) {
            this.rollingEnabled.set(false);
            resetRolling();
        }
    }

    public void enableRolling() {
        if (!rollingEnabled.get()) {
            this.rollingEnabled.set(true);
            resetRolling();
        }
    }

    public void resetRolling() {
        this.history.clear();
        this.rollingAvg.set(0);
        startTime = new Date();
    }

    /**
     * Resets the {@link AveragingStatistic}. Sets count and avg to 0 and clears
     * history.
     */
    public void reset() {
        super.reset();
        resetRolling();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AveragingStatistic [");
        builder.append("name=");
        builder.append(name);
        builder.append(", count=");
        builder.append(count);
        builder.append(", rollingAvg=");
        builder.append(rollingAvg);
        builder.append(", windowPeriodsSinceStart=");
        builder.append(getWindowPeriodsSinceStart(new Date()));
        builder.append(", rollingAvgWindow=");
        builder.append(rollingAvgWindow);
        builder.append(", rollingTimeWindow=");
        builder.append(rollingTimeWindow);
        builder.append(", enabled=");
        builder.append(enabled);
        builder.append(", rollingEnabled=");
        builder.append(rollingEnabled);
        builder.append(", startTime=");
        builder.append(startTime);
        builder.append(", condition=");
        builder.append(condition);
        builder.append(", throwing=");
        builder.append(throwing);
        builder.append(", logLevel=");
        builder.append(logLevel);
        builder.append("]");
        return builder.toString();
    }

    /**
     * The window of time for calculating the average.
     */
    public enum ROLLING_AVG_WINDOW {
        NONE(0, 0), SECOND(1000, 1000), MINUTE(60000, 10000), HOUR(3600000,
                60000), DAY(86400000, 60000);

        long millis;
        long coolingOffInMillis;

        private ROLLING_AVG_WINDOW(long millis, long coolingOffInMillis) {
            this.millis = millis;
            this.coolingOffInMillis = coolingOffInMillis;
        }
    }

}
