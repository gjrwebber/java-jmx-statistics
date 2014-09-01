package org.gw.samples;

import org.aspectj.lang.Aspects;
import org.gw.samples.config.SharedConfig;
import org.gw.samples.service.AspectJService;
import org.gw.samples.service.ExampleService;
import org.gw.stats.aop.AnnotationDrivenJMXStatisticsService;
import org.gw.stats.aop.StatsManagedResourceStatisticsSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;

@Configurable
//@EnableLoadTimeWeaving
@EnableMBeanExport
public class AppConfig extends SharedConfig {

    @Bean
    public ExampleService myService() {
        return new AspectJService();
    }

    @Bean
    public AnnotationDrivenJMXStatisticsService statsService() {

        // Load the stats Aspect
        AnnotationDrivenJMXStatisticsService statsService = Aspects
                .aspectOf(AnnotationDrivenJMXStatisticsService.class);

        // This will check org.gw.samples packages for classes annotated
        // with @StatsManagedResource
        StatsManagedResourceStatisticsSource source = new StatsManagedResourceStatisticsSource(
                "org.gw.samples");
        statsService.setStatsSources(source);
        statsService.init();

        return statsService;
    }
}
