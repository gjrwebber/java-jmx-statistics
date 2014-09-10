package org.gw.samples.service;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

@ManagedResource(objectName = "org.gw.samples.springservice:name=SpringService")
@Service
public class SpringService extends ExampleServiceImpl {

}
