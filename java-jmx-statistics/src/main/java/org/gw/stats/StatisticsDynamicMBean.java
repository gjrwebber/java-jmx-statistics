package org.gw.stats;

import org.gw.stats.jmx.attributes.*;
import org.gw.stats.jmx.operations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of a {@link DynamicMBean} containing the actual statistics and
 * the MBeanInfo for the JMX MBean.
 *
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class StatisticsDynamicMBean implements DynamicMBean {

    private final static MBeanServer server = ManagementFactory
            .getPlatformMBeanServer();
    private ConcurrentHashMap<String, Statistic> stats = new ConcurrentHashMap<String, Statistic>();
    private Class<?> targetClass;
    private String objectName;
    private Logger logger;
    private Map<String, AllStatisticsOperation> allOperationCache = new HashMap<String, AllStatisticsOperation>();
    private AllStatisticsOperation[] allOperations = new AllStatisticsOperation[]{
            new ResetAllStatisticOperation(),
            new EnableAllStatisticsOperation(),
            new DisableAllStatisticsOperation(),
            new SetAllWindowStatisticsOperation(),
            new EnableAllLoggingStatisticsOperation(),
            new DisableAllLoggingStatisticsOperation()};
    @SuppressWarnings("rawtypes")
    private Map<String, StatisticOperation> statOperationCache = new HashMap<String, StatisticOperation>();
    @SuppressWarnings({"rawtypes"})
    private StatisticOperation[] statOperations = new StatisticOperation[]{
            new ResetStatisticOperation(), new EnableStatisticsOperation(),
            new DisableStatisticsOperation(),
            new EnableLoggingStatisticsOperation(),
            new DisableLoggingStatisticsOperation(),};
    @SuppressWarnings({"rawtypes"})
    private StatisticOperation[] rollingStatOperations = new StatisticOperation[]{
            new EnableRollingStatisticsOperation(),
            new DisableRollingStatisticsOperation(),
            new SetWindowStatisticsOperation()};
    @SuppressWarnings("rawtypes")
    private Map<String, StatisticsAttribute> attributeCache = new HashMap<String, StatisticsAttribute>();
    @SuppressWarnings("unchecked")
    private StatisticsAttribute<?, Statistic>[] attributes = new StatisticsAttribute[]{
            new StatisticsCountAttribute(), // new EnabledStatisticsAttribute(),
            new LoggingEnabledStatisticsAttribute(),
            new RecordedStatisticsAttribute(),};
    @SuppressWarnings("unchecked")
    private StatisticsAttribute<?, AveragingStatistic>[] rollingAttributes = new StatisticsAttribute[]{
            new RollingAvgStatisticsAttribute(),
            new RollingAvgWindowStatisticsAttribute(),
            // new RollingEnabledStatisticsAttribute()
    };
    private AtomicBoolean enabled = new AtomicBoolean(true);

    public StatisticsDynamicMBean(String objectName, Class<?> targetClass) {

        this.objectName = objectName;
        this.targetClass = targetClass;
        logger = LoggerFactory.getLogger(objectName);

		/*
         * Create a cache of operations for all
		 */
        for (AllStatisticsOperation op : allOperations) {
            allOperationCache.put(op.getName(), op);
        }
    }

    public int getStatisticsCount() {
        return stats.size();
    }

    @SuppressWarnings("unchecked")
    public void addStat(String key, Statistic statistic) {
        stats.putIfAbsent(key, statistic);

		/*
         * Create a cache of the attributes for this Statistic
		 */
        for (StatisticsAttribute<?, ? extends Statistic> attribute : attributes) {
            attributeCache.put(
                    statistic.getName() + attribute.getNameAddendum(statistic),
                    attribute);
        }

		/*
         * Create a cache of the attributes for this Statistic
		 */
        for (StatisticsAttribute<?, ? extends Statistic> attribute : rollingAttributes) {
            attributeCache.put(
                    statistic.getName() + attribute.getNameAddendum(statistic),
                    attribute);
        }

		/*
		 * Create a cache of the operations for this Statistic
		 */
        for (StatisticOperation<? extends Statistic> op : statOperations) {
            statOperationCache
                    .put(op.getNamePrefix() + statistic.getName(), op);
        }

		/*
		 * Create a cache of the operations for this Statistic
		 */
        for (StatisticOperation<? extends Statistic> op : rollingStatOperations) {
            statOperationCache
                    .put(op.getNamePrefix() + statistic.getName(), op);
        }
    }

    public void incrementStat(int increment, Object capturedObject,
                              String statName) {
        if (enabled.get()) {
            Statistic statistic = stats.get(statName);
            if (statistic != null) {
                statistic.increment(increment, capturedObject);
            }
        }
    }

    public void incrementStat(int increment, Object capturedObject,
                              Statistic statistic) {
        if (enabled.get()) {
            if (statistic != null) {
                statistic.increment(increment, capturedObject);
            }
        }
    }

    public void resetAll() {
        if (logger.isDebugEnabled()) {
            logger.debug("Resetting all stats for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            stat.reset();
        }
    }

    public void enableAllLogging() {
        if (logger.isDebugEnabled()) {
            logger.debug("Enabling logging for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            stat.enableLogging();
        }
    }

    public void disableAllLogging() {
        if (logger.isDebugEnabled()) {
            logger.debug("Disabling logging for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            stat.disableLogging();
        }
    }

    public void enableAll() {
        if (logger.isDebugEnabled()) {
            logger.debug("Enabling all stats for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            stat.enable();
        }
    }

    public void disableAll() {
        if (logger.isDebugEnabled()) {
            logger.debug("Disabling all stats for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            stat.disable();
        }
    }

    public void setTimeWindowForAll(int secs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting time window to " + secs
                    + "s for All stats for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            if (stat instanceof AveragingStatistic) {
                AveragingStatistic rollingStat = (AveragingStatistic) stat;
                rollingStat.setRollingTimeWindow(secs * 1000);
            }
        }

    }

    public void updateRollingForAll() {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating rolling avg for All stats for " + objectName);
        }
        for (Statistic stat : stats.values()) {
            if (stat instanceof AveragingStatistic) {
                AveragingStatistic rollingStat = (AveragingStatistic) stat;
                rollingStat.runCommands();
            }
        }

    }

    public void reset(String statName) {
        Statistic stat = stats.get(statName);
        if (stat != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Resetting " + statName + " for " + objectName);
            }

            stat.reset();
        } else {
            throw new IllegalStateException("Could not Reset " + statName
                    + ". Unable to find Statistic with name " + statName);
        }
    }

    public Statistic getStatistic(String name) {
        return stats.get(name);
    }

    public long getCount(String name) {
        try {
            return stats.get(name).getCount().get();
        } catch (Exception e) {
            return -1;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public String getObjectName() {
        return objectName;
    }

    void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * Register this {@link DynamicMBean} on the MBeanServer
     */
    public void registerStatsMBean() {
        try {
            ObjectName objName = new ObjectName(getObjectName());

            synchronized (server) {
                try {
                    if (server.isRegistered(objName)) {

                        if (logger.isDebugEnabled()) {
                            logger.debug(String
                                    .format("%s already registered on MBeanServer. Skipping registration.",
                                            objectName));
                        }

                    } else {
                        server.registerMBean(this, objName);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format(
                                    "%s registered on MBeanServer.", objectName));
                        }
                    }

                } catch (InstanceAlreadyExistsException e) {
                    logger.warn(" The ObjectName (" + this.getObjectName()
                            + ") specified on " + targetClass.getSimpleName()
                            + " is already being used.", e);
                } catch (MBeanRegistrationException e) {
                    logger.warn(" The ObjectName (" + this.getObjectName()
                            + ") specified on " + targetClass.getSimpleName()
                            + " could not be registered.", e);
                } catch (NotCompliantMBeanException e) {
                    logger.warn(" The ObjectName (" + this.getObjectName()
                            + ") specified on " + targetClass.getSimpleName()
                            + " is not compliant.", e);
                }
            }

        } catch (MalformedObjectNameException e) {
            logger.warn(
                    " The ObjectName @StatsManagedResource(" + getObjectName()
                            + ") specified on " + targetClass.getSimpleName()
                            + " is malformed. ", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object getAttribute(String attributeName)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        StatisticsAttribute attribute = attributeCache.get(attributeName);
        String statName = attribute.getStatisticName(attributeName);

        Statistic stat = stats.get(statName);
        if (stat == null) {
            throw new IllegalStateException(
                    "Could not find Statistic with name: " + statName);
        }
        return attribute.getValue(stat);

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public MBeanInfo getMBeanInfo() {

        List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
        List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();

        for (String statName : stats.keySet()) {

            Statistic stat = stats.get(statName);
            if (stat instanceof AveragingStatistic) {
                AveragingStatistic rollingStat = (AveragingStatistic) stat;

                // Add all attributes in the rolling statistics attributes array
                for (StatisticsAttribute<?, AveragingStatistic> att : this.rollingAttributes) {
                    if (att.show(rollingStat)) {
                        attributes.add(att.getMBeanAttributeInfo(rollingStat));
                    }
                }

                // Add all operations in the rolling statistics operations array
                for (StatisticOperation op : rollingStatOperations) {
                    if (op.show(rollingStat)) {
                        operations.add(op.getMBeanOperationinfo(statName));
                    }
                }

            }

            // Add all attributes in the statistics attributes array
            for (StatisticsAttribute<?, Statistic> att : this.attributes) {
                if (att.show(stat)) {
                    attributes.add(att.getMBeanAttributeInfo(stat));
                }
            }

            // Add all operations in the statistics operations array
            for (StatisticOperation op : statOperations) {
                if (op.show(stat)) {
                    operations.add(op.getMBeanOperationinfo(statName));
                }
            }

        }

        // Add all operations in the allOperations array
        for (AllStatisticsOperation op : allOperations) {
            operations.add(op.getMBeanOperationinfo());
        }

        MBeanInfo info = new MBeanInfo(this.getClass().getSimpleName(),
                "Statistics Dynamic MBean: " + objectName,
                attributes.toArray(new MBeanAttributeInfo[attributes.size()]),
                new MBeanConstructorInfo[0],
                operations.toArray(new MBeanOperationInfo[operations.size()]),
                new MBeanNotificationInfo[0]);

        return info;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.management.DynamicMBean#invoke(java.lang.String,
     * java.lang.Object[], java.lang.String[])
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {

        for (AllStatisticsOperation op : allOperations) {
            if (op.getName().equals(actionName)) {
                op.invoke(this, params);
                if (op.reRegisterAll()) {
                    registerStatsMBean();
                }
                return null;
            }
        }

        StatisticOperation op = statOperationCache.get(actionName);
        if (op == null) {
            throw new IllegalStateException("Could not " + actionName
                    + ". This is an unknown operation.");
        }
        String statName = op.getStatisticName(actionName);

        Statistic stat = stats.get(statName);
        if (stat == null) {
            throw new IllegalStateException(
                    "Could not find Statistic with name: " + statName);
        }
        op.invoke(stat, params);

        // If this operation requires re-registering after invocation, then do
        // it!
        if (op.reRegisterMBean(stat)) {
            registerStatsMBean();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.management.DynamicMBean#setAttribute(javax.management.Attribute )
     */
    @Override
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.management.DynamicMBean#setAttributes(javax.management.
     * AttributeList)
     */
    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }
}
