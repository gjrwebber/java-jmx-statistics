package org.gw.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class to represent a statistic. A statistic is a named entity that contains a
 * count and optionally a {@link Collection} of recorded objects.
 * <p/>
 * To increment this statistic call <code>increment()</code>. To record an
 * object while incrementing call <code>increment(Object)</code>.
 *
 * @author Gman
 */
public class Statistic {

    private static Logger logger = LoggerFactory.getLogger(Statistic.class);
    /**
     * The name of the statistic
     */
    protected String name;
    /**
     * The condition on which to increment the statistic
     */
    protected String condition;
    /**
     * Increment when throwing this {@link Throwable}
     */
    protected Class<? extends Throwable> throwing;
    /**
     * Log level when logging the statistic
     */
    protected LOG_LEVEL logLevel = LOG_LEVEL.NONE;
    /**
     * The statistics will not be incremented or recorded if this is false
     */
    protected AtomicBoolean enabled = new AtomicBoolean(true);
    /**
     * Contains the statistic count
     */
    protected AtomicLong count = new AtomicLong();
    /**
     * The expression for recording objects
     */
    private String recordingExpression;
    /**
     * The default number of recorded objects
     */
    private int maxRecordings = 100;
    /**
     * Holds the recorded objects in a circular array
     */
    private Object[] recorded;
    /**
     * The index used in the circular array above
     */
    private int index;

    /**
     * @param name                The name of the {@link Statistic}
     * @param condition           The condition expression string for this statistic
     * @param throwing            THe {@link Throwable} for this statistic
     * @param logLevel            The {@link LOG_LEVEL} for this statistic
     * @param recordingExpression The expression string for recording objects
     */
    public Statistic(String name, String condition,
                     Class<? extends Throwable> throwing, LOG_LEVEL logLevel,
                     String recordingExpression) {
        this(name, condition, throwing, logLevel, recordingExpression, 100);
    }

    /**
     * @param name                The name of the {@link Statistic}
     * @param condition           The condition expression string for this statistic
     * @param throwing            THe {@link Throwable} for this statistic
     * @param logLevel            The {@link LOG_LEVEL} for this statistic
     * @param recordingExpression The expression string for recording objects
     * @param maxRecordings       The maximum number of recorded object to keep in memory
     */
    public Statistic(String name, String condition,
                     Class<? extends Throwable> throwing, LOG_LEVEL logLevel,
                     String recordingExpression, int maxRecordings) {

        this.name = name;
        this.condition = condition;
        this.throwing = throwing;
        this.logLevel = logLevel;
        this.maxRecordings = maxRecordings;
        if (recordingExpression != null && recordingExpression.length() > 0) {
            this.recordingExpression = recordingExpression;
            recorded = new Object[this.maxRecordings];
        }
    }

    /**
     * Increment the count for this {@link Statistic} by 1.
     */
    public long increment() {
        return increment(null);
    }

    /**
     * Increment the count for this {@link Statistic} by 1 and record the given
     * Object.
     *
     * @param recordedObject The Object to record. If the maxRecordings has been reached,
     *                       this Object replaces the oldest in the array.
     */
    public long increment(Object recordedObject) {
        return increment(1, recordedObject);
    }

    /**
     * Increments the count for this statistic by <code>increment</code> and
     * records the given Object.
     *
     * @param increment      The number of increments for this statistic
     * @param recordedObject The Object to record. If the maxRecordings has been reached,
     *                       this Object replaces the oldest in the array.
     */
    public long increment(int increment, Object recordedObject) {
        long result = 0;
        if (enabled.get()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Incrementing count on " + this);
            }
            result = count.addAndGet(increment);
            record(recordedObject);
            if (logger.isDebugEnabled()) {
                logger.debug("Count now " + count + " on " + this);
            }
        }
        return result;
    }

    /**
     * Records the recordedObject if it is not null and this statistic is recording objects.
     *
     * @param recordedObject
     */
    public void record(Object recordedObject) {
        if (isRecording() && recordedObject != null) {
            /*
             * Store the recordedObject in the array. Either at the current
			 * index or the start (overwriting oldest) if index == length.
			 */
            synchronized (recorded) {
                if (index == recorded.length) {
                    index = 0;
                }
                recorded[index++] = recordedObject;
            }
        }
    }

    /**
     * Returns a {@link Collection} of {@link Object}'s representing the
     * recorded statistic history. This {@link Collection} is ordered from
     * oldest to newest.
     *
     * @return ordered oldest to newest {@link Collection} of {@link Long}'s
     */
    public List<Object> getRecordedHistory() {
        synchronized (recorded) {
            List<Object> result = new ArrayList<Object>(recorded.length);
            for (int i = index; i < recorded.length; i++) {
                Object obj = recorded[i];
                if (obj != null) {
                    result.add(obj);
                }
            }
            for (int i = 0; i < index; i++) {
                Object obj = recorded[i];
                if (obj != null) {
                    result.add(obj);
                }
            }
            return result;
        }
    }

    /**
     * Return true if the statistic should be recording objects
     *
     * @return
     */
    public boolean isRecording() {
        return recordingExpression != null && recorded != null;
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public Class<? extends Throwable> getThrowing() {
        return throwing;
    }

    /**
     * @return the logLevel
     */
    public LOG_LEVEL getLogLevel() {
        return logLevel;
    }

    public AtomicLong getCount() {
        return count;
    }

    public AtomicBoolean getEnabled() {
        return enabled;
    }

    public String getRecordingExpression() {
        return recordingExpression;
    }

    public void setRecordingExpression(String recordingExpression) {
        this.recordingExpression = recordingExpression;
    }

    /**
     * Disable this statistic. Stops it incrementing and recording and resets it.
     */
    public void disable() {
        if (enabled.get()) {
            this.enabled.set(false);
            reset();
        }
    }

    /**
     * Enables this statistic. Resets it and starts incrementing and recording.
     */
    public void enable() {
        if (!enabled.get()) {
            this.enabled.set(true);
            reset();
        }
    }

    public void enableLogging() {
        this.logLevel = LOG_LEVEL.DEBUG;
    }

    public void disableLogging() {
        this.logLevel = LOG_LEVEL.NONE;
    }

    /**
     * Resets the {@link Statistic}. Sets count to 0 and clears
     * recorded.
     */
    public void reset() {
        if (logger.isDebugEnabled()) {
            logger.debug("Resetting " + this);
        }
        this.count.set(0);
        if (recorded != null) {
            for (int i = 0; i < recorded.length; i++) {
                recorded[i] = null;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Statistic [name=");
        builder.append(name);
        builder.append(", count=");
        builder.append(count);
        builder.append(", condition=");
        builder.append(condition);
        builder.append(", throwing=");
        builder.append(throwing);
        builder.append(", logLevel=");
        builder.append(logLevel);
        builder.append(", enabled=");
        builder.append(enabled);
        builder.append("]");
        return builder.toString();
    }

    public enum LOG_LEVEL {
        NONE, DEBUG, INFO;
    }

}
