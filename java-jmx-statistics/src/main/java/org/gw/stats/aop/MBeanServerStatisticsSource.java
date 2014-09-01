/**
 *
 */
package org.gw.stats.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * StatsSource which returns all MBeans from the platforms MBean Server. A
 * domain prefix filter can be used if you wish to narrow the mbean search. This
 * must be set before calling <code>init()</code>
 *
 * @author Gman
 */
@Component
public class MBeanServerStatisticsSource implements AnnotatedStatisticsSource {

    private static Logger logger = LoggerFactory
            .getLogger(MBeanServerStatisticsSource.class);

    private Map<String, Class<?>> mbeans;

    /**
     * The mbean domain prefix filter to use when looking up mbeans on the MBean
     * Server.
     */
    private String[] domainPrefixFilters = System.getProperty(
            "statistics.mbean.domain.prefix.filters", "").split(",");

    /**
     * The addendum to the domain in the {@link ObjectName} of the stats for
     * this source
     */
    private String domainAddendum = System.getProperty(
            "statistics.mbean.domain.addon", ".stats");

    private QueryExp query;

    /**
     * Empty Constructor
     */
    public MBeanServerStatisticsSource() {
        setDomainPrefixFilters(this.domainPrefixFilters);
    }

    /**
     * Constructor which takes a domain addendum and domain prefix filter
     *
     * @param domainAddendum      The addendum to the domain for this stats source
     * @param domainPrefixFilters The domain filters for querying the mbean server
     */
    public MBeanServerStatisticsSource(
            String domainAddendum, String... domainPrefixFilters) {
        setDomainAddendum(domainAddendum);
        setDomainPrefixFilters(domainPrefixFilters);
    }

    /**
     * Constructor which takes a domain prefix filter
     *
     * @param domainPrefixFilters The domain filters for querying the mbean server
     */
    public MBeanServerStatisticsSource(String... domainPrefixFilters) {
        this(null, domainPrefixFilters);
    }

    public void init() {

        if (logger.isDebugEnabled()) {
            logger.debug("Initialising MBeanServerStatisticsSource...");
        }

        mbeans = new HashMap<String, Class<?>>();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        for (ObjectInstance obj : ManagementFactory.getPlatformMBeanServer()
                .queryMBeans(null, query)) {
            try {
                Class<?> clazz = Class.forName(obj.getClassName());

				/*
                 * Add .stats to the domain of the ObjectName
				 */
                String statsObjectName = obj.getObjectName().getDomain()
                        + domainAddendum + ":"
                        + obj.getObjectName().getKeyPropertyListString();

                mbeans.put(statsObjectName, clazz);
            } catch (ClassNotFoundException e) {
                // Don't care
                logger.info("Could not load class to check for MBean for stats: "
                        + obj.getClassName());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String
                    .format("MBeanServerStatisticsSource initialised. Found %d MBeans. To be assessed for ",
                            mbeans.size()));
        }
    }

    /**
     * @see AnnotatedStatisticsSource#getStatsBeans()
     */
    @Override
    public Map<String, Class<?>> getStatsBeans() {
        if (mbeans == null) {
            init();
        }
        return mbeans;
    }

    public String[] getDomainPrefixFilters() {
        return domainPrefixFilters;
    }

    /**
     * Sets the domain prefix filter and creates a {@link QueryExp} ready for
     * <code>init()</code> to be called.
     *
     * @param domainPrefixFilters
     */
    @SuppressWarnings("serial")
    public void setDomainPrefixFilters(final String... domainPrefixFilters) {
        this.domainPrefixFilters = domainPrefixFilters;
        query = new QueryExp() {

            @Override
            public void setMBeanServer(MBeanServer arg0) {

            }

            @Override
            public boolean apply(ObjectName name)
                    throws BadStringOperationException,
                    BadBinaryOpValueExpException,
                    BadAttributeValueExpException, InvalidApplicationException {
                if (domainPrefixFilters != null) {
                    for (int i = 0; i < domainPrefixFilters.length; i++) {
                        String filter = domainPrefixFilters[i];
                        if (name.getDomain().startsWith(filter)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return true;
                }
            }
        };
    }

    public String getDomainAddendum() {
        return domainAddendum;
    }

    public void setDomainAddendum(String domainAddendum) {
        this.domainAddendum = domainAddendum;
    }

}
