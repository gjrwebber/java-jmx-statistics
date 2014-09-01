package org.gw.samples.service;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Created by gman on 26/08/2014.
 */
@ManagedResource
public class SamplesMgmtService {

    @ManagedOperation
    public void shutdown() {
        System.out.println("Goodbye.");
        System.exit(0);
    }
}
