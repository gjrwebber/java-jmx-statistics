package org.gw.samples.service;

import org.gw.stats.aop.StatsManagedResource;
import org.springframework.jmx.export.annotation.ManagedResource;

//Using AspectJ
@ManagedResource
@StatsManagedResource("example.service.stats:type=AspectjService")
public class AspectJService extends ExampleServiceImpl {

}
