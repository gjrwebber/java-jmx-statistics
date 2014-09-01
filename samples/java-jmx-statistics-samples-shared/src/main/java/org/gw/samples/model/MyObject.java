package org.gw.samples.model;

import org.gw.stats.aop.IncrementStat;
import org.gw.stats.aop.StatsManagedResource;

@StatsManagedResource("example.model.stats:type=MyObject")
public class MyObject {

    /**
     * Creates a new Statisitc called Dynamic {name} where {name} is the
     * parameter passed into the method is the and associates it with
     * MyObject.class. This will register the statistics with the
     * ObjectName: org.gw.samples.model:type=MyObject.
     * <p>This statistic will increment on every execution of this method.
     * </p>
     */
    @IncrementStat(value = "Dynamic #name")
    public void DynamicName(String name) throws Exception {
        // .. do something
        if (name == null) {
            throw new Exception();
        }
    }

    /**
     * Creates a new Statisitc called Dynamic {name} where {name} is the
     * parameter passed into the method is the and associates it with
     * MyObject.class. This will register the statistics with the
     * ObjectName: org.gw.samples.model:type=MyObject.
     * <p>This statistic will increment on every execution of this method
     * only if the name is "Lara".
     * </p>
     */
    @IncrementStat(value = "Dynamic #name", condition = "#name == 'Lara'")
    public void DynamicLara(String name) throws Exception {
        // .. do something
        if (name == null) {
            throw new Exception();
        }
    }

}
