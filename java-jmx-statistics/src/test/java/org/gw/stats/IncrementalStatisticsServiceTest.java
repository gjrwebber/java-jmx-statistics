/**
 * IncrementalStatisticsServiceTest.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats;

import org.gw.stats.Statistic.LOG_LEVEL;
import org.junit.Assert;
import org.junit.Test;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class IncrementalStatisticsServiceTest {

    /**
     * Test method for
     * {@link org.gw.IncrementalJMXStatisticsService#incrementStats(java.lang.String, java.lang.Object)}
     * .
     *
     * @throws ReflectionException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     */
    @Test
    public final void testIncrementStatsStringObject()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        JMXStatisticsService service = new JMXStatisticsService();
        service.initialiseStats("Test1", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test2", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test3", RollingAvgStatisticTest.class);

        service.incrementStats("Test1", IncrementalStatisticsServiceTest.class);
        StatisticsDynamicMBean stats = service.getStatsMBeansMap().get(
                IncrementalStatisticsServiceTest.class);
        Assert.assertNotNull(stats);
        Statistic stat = stats.getStatistic("Test1");
        Assert.assertNotNull(stat);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test1")).get());

        service.incrementStats("Test2", IncrementalStatisticsServiceTest.class);
        Statistic stat2 = stats.getStatistic("Test2");
        Assert.assertNotNull(stat2);
        Assert.assertEquals(1, stat2.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test2")).get());

        service.incrementStats("Test3", RollingAvgStatisticTest.class);
        StatisticsDynamicMBean stats3 = service.getStatsMBeansMap().get(
                RollingAvgStatisticTest.class);
        Assert.assertNotNull(stats3);
        Statistic stat3 = stats3.getStatistic("Test3");
        Assert.assertNotNull(stat3);
        Assert.assertEquals(1, stat3.getCount().get());
        Assert.assertEquals(1,
                ((AtomicLong) stats3.getAttribute("Test3")).get());

    }

    /**
     * Test method for
     * {@link org.gw.IncrementalJMXStatisticsService#incrementStatsAndLog(java.lang.String, java.lang.Object, boolean)}
     * .
     *
     * @throws ReflectionException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     */
    @Test
    public final void testIncrementStatsAndLog()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        JMXStatisticsService service = new JMXStatisticsService();
        service.initialiseStats("Test1",
                IncrementalStatisticsServiceTest.class, LOG_LEVEL.DEBUG);
        service.incrementStats("Test1", IncrementalStatisticsServiceTest.class);
        StatisticsDynamicMBean stats = service.getStatsMBeansMap().get(
                IncrementalStatisticsServiceTest.class);
        Assert.assertNotNull(stats);
        Statistic stat = stats.getStatistic("Test1");
        Assert.assertNotNull(stat);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test1")).get());
    }

    /**
     * Test method for
     * {@link org.gw.IncrementalJMXStatisticsService#reset(java.lang.String, java.lang.Object)}
     * .
     *
     * @throws ReflectionException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     */
    @Test
    public final void testReset() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        JMXStatisticsService service = new JMXStatisticsService();
        service.initialiseStats("Test1", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test2", RollingAvgStatisticTest.class);

        service.incrementStats("Test1", IncrementalStatisticsServiceTest.class);
        StatisticsDynamicMBean stats = service.getStatsMBeansMap().get(
                IncrementalStatisticsServiceTest.class);
        Assert.assertNotNull(stats);
        Statistic stat = stats.getStatistic("Test1");
        Assert.assertNotNull(stat);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test1")).get());

        service.incrementStats("Test2", RollingAvgStatisticTest.class);
        StatisticsDynamicMBean stats2 = service.getStatsMBeansMap().get(
                RollingAvgStatisticTest.class);
        Assert.assertNotNull(stats2);
        Statistic stat2 = stats2.getStatistic("Test2");
        Assert.assertNotNull(stat2);
        Assert.assertEquals(1, stat2.getCount().get());
        Assert.assertEquals(1,
                ((AtomicLong) stats2.getAttribute("Test2")).get());

        service.reset("Test1", IncrementalStatisticsServiceTest.class);
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, ((AtomicLong) stats.getAttribute("Test1")).get());

        Assert.assertEquals(1, stat2.getCount().get());
        Assert.assertEquals(1,
                ((AtomicLong) stats2.getAttribute("Test2")).get());
    }

    /**
     * Test method for
     * {@link org.gw.IncrementalJMXStatisticsService#reset(java.lang.String, java.lang.Object)}
     * .
     *
     * @throws ReflectionException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     */
    @Test
    public final void testResetAllForClass() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        JMXStatisticsService service = new JMXStatisticsService();
        service.initialiseStats("Test1", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test2", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test3", RollingAvgStatisticTest.class);

        service.incrementStats("Test1", IncrementalStatisticsServiceTest.class);
        StatisticsDynamicMBean stats = service.getStatsMBeansMap().get(
                IncrementalStatisticsServiceTest.class);
        Assert.assertNotNull(stats);
        Statistic stat = stats.getStatistic("Test1");
        Assert.assertNotNull(stat);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test1")).get());

        service.incrementStats("Test2", IncrementalStatisticsServiceTest.class);
        Statistic stat2 = stats.getStatistic("Test2");
        Assert.assertNotNull(stat2);
        Assert.assertEquals(1, stat2.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test2")).get());

        service.incrementStats("Test3", RollingAvgStatisticTest.class);
        StatisticsDynamicMBean stats3 = service.getStatsMBeansMap().get(
                RollingAvgStatisticTest.class);
        Assert.assertNotNull(stats3);
        Statistic stat3 = stats3.getStatistic("Test3");
        Assert.assertNotNull(stat3);
        Assert.assertEquals(1, stat3.getCount().get());
        Assert.assertEquals(1,
                ((AtomicLong) stats3.getAttribute("Test3")).get());

        service.resetAll(IncrementalStatisticsServiceTest.class);
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, ((AtomicLong) stats.getAttribute("Test1")).get());
        Assert.assertEquals(0, stat2.getCount().get());
        Assert.assertEquals(0, ((AtomicLong) stats.getAttribute("Test2")).get());

        Assert.assertEquals(1, stat3.getCount().get());
        Assert.assertEquals(1,
                ((AtomicLong) stats3.getAttribute("Test3")).get());
    }

    /**
     * Test method for
     * {@link org.gw.IncrementalJMXStatisticsService#reset(java.lang.String, java.lang.Object)}
     * .
     *
     * @throws ReflectionException
     * @throws MBeanException
     * @throws AttributeNotFoundException
     */
    @Test
    public final void testResetAll() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        JMXStatisticsService service = new JMXStatisticsService();
        service.initialiseStats("Test1", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test2", IncrementalStatisticsServiceTest.class);
        service.initialiseStats("Test3", RollingAvgStatisticTest.class);

        service.incrementStats("Test1", IncrementalStatisticsServiceTest.class);
        StatisticsDynamicMBean stats = service.getStatsMBeansMap().get(
                IncrementalStatisticsServiceTest.class);
        Assert.assertNotNull(stats);
        Statistic stat = stats.getStatistic("Test1");
        Assert.assertNotNull(stat);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test1")).get());

        service.incrementStats("Test2", IncrementalStatisticsServiceTest.class);
        Statistic stat2 = stats.getStatistic("Test2");
        Assert.assertNotNull(stat2);
        Assert.assertEquals(1, stat2.getCount().get());
        Assert.assertEquals(1, ((AtomicLong) stats.getAttribute("Test2")).get());

        service.incrementStats("Test3", RollingAvgStatisticTest.class);
        StatisticsDynamicMBean stats3 = service.getStatsMBeansMap().get(
                RollingAvgStatisticTest.class);
        Assert.assertNotNull(stats3);
        Statistic stat3 = stats3.getStatistic("Test3");
        Assert.assertNotNull(stat3);
        Assert.assertEquals(1, stat3.getCount().get());
        Assert.assertEquals(1,
                ((AtomicLong) stats3.getAttribute("Test3")).get());

        service.resetAll();
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, ((AtomicLong) stats.getAttribute("Test1")).get());
        Assert.assertEquals(0, stat2.getCount().get());
        Assert.assertEquals(0, ((AtomicLong) stats.getAttribute("Test2")).get());

        Assert.assertEquals(0, stat3.getCount().get());
        Assert.assertEquals(0,
                ((AtomicLong) stats3.getAttribute("Test3")).get());
    }

}
