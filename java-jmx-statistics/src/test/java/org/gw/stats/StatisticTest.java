/**
 * StatisticTest.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats;

import org.gw.stats.Statistic.LOG_LEVEL;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class StatisticTest {

    /**
     * Test method for
     * {@link Statistic#increment()}.
     */
    @Test
    public final void testIncrement() {
        Statistic stat = new Statistic("", "", null, LOG_LEVEL.NONE, null);
        Assert.assertEquals(0, stat.getCount().get());
        stat.increment();
        Assert.assertEquals(1, stat.getCount().get());
        stat.increment();
        Assert.assertEquals(2, stat.getCount().get());
    }

    /**
     * Test method for
     * {@link Statistic#disable()}.
     */
    @Test
    public final void testDisable() {
        Statistic stat = new Statistic("", "", null, LOG_LEVEL.NONE, null);
        Assert.assertEquals(0, stat.getCount().get());
        stat.increment();
        Assert.assertEquals(1, stat.getCount().get());
        stat.disable();
        stat.increment();
        Assert.assertEquals(0, stat.getCount().get());
    }

    /**
     * Test method for
     * {@link Statistic#enable()}.
     */
    @Test
    public final void testEnable() {
        Statistic stat = new Statistic("", "", null, LOG_LEVEL.NONE, null);
        Assert.assertEquals(0, stat.getCount().get());
        stat.increment();
        Assert.assertEquals(1, stat.getCount().get());
        stat.disable();
        stat.increment();
        Assert.assertEquals(0, stat.getCount().get());
        stat.enable();
        stat.increment();
        Assert.assertEquals(1, stat.getCount().get());
    }

    /**
     * Test method for
     * {@link Statistic#reset()}.
     */
    @Test
    public final void testReset() {
        Statistic stat = new Statistic("", "", null, LOG_LEVEL.NONE, null);
        Assert.assertEquals(0, stat.getCount().get());
        stat.increment();
        Assert.assertEquals(1, stat.getCount().get());
        stat.increment();
        Assert.assertEquals(2, stat.getCount().get());
        stat.reset();
        Assert.assertEquals(0, stat.getCount().get());
    }

}
