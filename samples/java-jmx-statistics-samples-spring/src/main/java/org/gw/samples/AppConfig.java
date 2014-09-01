package org.gw.samples;

import org.gw.samples.config.SharedConfig;
import org.gw.samples.service.ExampleService;
import org.gw.samples.service.SpringService;
import org.gw.stats.aop.AnnotationDrivenJMXStatisticsService;
import org.gw.stats.aop.MBeanServerStatisticsSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableMBeanExport;

@Configurable
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableLoadTimeWeaving
@EnableMBeanExport
public class AppConfig extends SharedConfig {

    @Bean
    public ExampleService exampleService() {
        return new SpringService();
    }

    @Bean
    public AnnotationDrivenJMXStatisticsService statsService() {

        // Instantiate the stats Aspect
        AnnotationDrivenJMXStatisticsService statsService = new AnnotationDrivenJMXStatisticsService();

		/*
         * This will check the mbean server for ObjectNames starting with
		 * org.gw.samples The MBeans it finds will have been setup using
		 * Spring, either in XML or with @ManagedResource
		 */
        MBeanServerStatisticsSource source = new MBeanServerStatisticsSource();
        source.setDomainPrefixFilters("org.gw.samples");

        statsService.setStatsSources(source);

        return statsService;
    }
}
