package org.gw.samples;

import org.gw.samples.config.SharedConfig;
import org.gw.stats.aop.MBeanServerStatisticsSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.*;

@Configurable
@ComponentScan(basePackages = {"org.gw"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableLoadTimeWeaving
@EnableMBeanExport
public class AppConfig extends SharedConfig {

    @Bean
    public MBeanServerStatisticsSource mBeanServerStatisticsSource() {

		/*
         * This will check the mbean server for ObjectNames starting with
		 * org.gw.samples The MBeans it finds will have been setup using
		 * Spring, either in XML or with @ManagedResource
		 */
        MBeanServerStatisticsSource source = new MBeanServerStatisticsSource();
        source.setDomainPrefixFilters("org.gw.samples");
        return source;
    }
}
