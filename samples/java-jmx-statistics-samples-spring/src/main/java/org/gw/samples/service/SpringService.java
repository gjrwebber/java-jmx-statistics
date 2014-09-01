package org.gw.samples.service;

import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "org.gw.samples.springservice:name=SpringService")
public class SpringService extends ExampleServiceImpl {

}
