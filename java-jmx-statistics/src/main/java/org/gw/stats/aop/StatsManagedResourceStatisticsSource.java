package org.gw.stats.aop;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.*;

/**
 * A {@link AnnotatedStatisticsSource} to collect all types annotated with
 * StatsManagedResource
 *
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@Component
public class StatsManagedResourceStatisticsSource
        implements
        AnnotatedStatisticsSource {

    private static Logger logger = LoggerFactory
            .getLogger(StatsManagedResourceStatisticsSource.class);

    @Autowired
    private ApplicationContext ctx;

    private Map<String, Class<?>> beans;

    private String[] basePackages = System.getProperty(
            "statistics.base.packages", "").split(",");

    /**
     * A library to provide realtime details of IEvent Objects.
     */
    private Reflections reflections;

    public StatsManagedResourceStatisticsSource() {

    }

    public StatsManagedResourceStatisticsSource(String... basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * Initialises a {@link org.gw.stats.StatisticsDynamicMBean} object to track the
     * statistics of all Spring beans annotated with
     * {@link StatsManagedResource}
     */
    @PostConstruct
    public void init() {

        if (logger.isDebugEnabled()) {
            logger.debug("Initialising StatsManagedResourceStatisticsSource...");
            logger.debug(String.format(
                    "Scanning base packages for @StatsManagedResource: %s",
                    Arrays.toString(basePackages)));
        }

        beans = new HashMap<String, Class<?>>();

        // Setup the Reflections API
        // First create a filter for Reflections
        FilterBuilder basePackageFilter = new FilterBuilder();
        Set<URL> urls = new HashSet<URL>();
        for (String basePackage : basePackages) {
            basePackageFilter.includePackage(basePackage);
            urls.addAll(ClasspathHelper.forPackage(basePackage));
        }
        reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(basePackageFilter).setUrls(urls)
                .setScanners(new TypeAnnotationsScanner()));

        Set<Class<?>> allStatsManagedResources = reflections
                .getTypesAnnotatedWith(StatsManagedResource.class);

        for (Class<?> targetClass : allStatsManagedResources) {
            /*
             * Get the JMX ObjectName from the @StatsManagedResource
			 */
            StatsManagedResource managedResource = targetClass
                    .getAnnotation(StatsManagedResource.class);

            String objectName = managedResource.value();
            if (objectName == null || objectName.length() == 0) {
                objectName = targetClass.getPackage().getName()
                        + ".stats:type=" + targetClass.getSimpleName();
            }

            beans.put(objectName, targetClass);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String
                    .format("StatsManagedResourceStatisticsSource initialised. Found %d @StatsManagedResources : %s",
                            beans.size(), beans));
        }
    }

    /**
     * @see AnnotatedStatisticsSource#getStatsBeans()
     */
    @Override
    public Map<String, Class<?>> getStatsBeans() {

        if (beans == null) {
            init();
        }
        return beans;
    }

    /**
     * @return the basePackages
     */
    public String[] getBasePackages() {
        return basePackages;
    }

    /**
     * @param basePackages the basePackages to set
     */
    public void setBasePackages(String... basePackages) {
        this.basePackages = basePackages;
    }
}
