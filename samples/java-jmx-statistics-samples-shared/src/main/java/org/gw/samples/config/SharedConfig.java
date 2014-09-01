package org.gw.samples.config;

import org.gw.samples.service.SamplesMgmtService;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;

/**
 * Created by gman on 26/08/2014.
 */
@Configurable
@EnableMBeanExport
public class SharedConfig {

    @Bean
    public SamplesMgmtService mgmtService() {
        return new SamplesMgmtService();
    }
}
