/**
 * IncrementStatsAspectTest.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.aop;

import org.gw.stats.AveragingStatistic;
import org.gw.stats.Statistic;
import org.gw.stats.StatisticsDynamicMBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/stats-test.xml")
@DirtiesContext
public class IncrementStatsAspectTest {

    @Autowired
    private TestService testService;

    @Autowired
    private AnnotationDrivenJMXStatisticsService statsService;

    private StatisticsDynamicMBean tsStats;

    @Before
    public void testSetup() {
        Assert.assertNotNull("No statsService", statsService);
        Assert.assertNotNull("No stats", statsService.getStatsMBeansMap());

        // check that the statsService injected into testService is the same as
        // the one in this test class
        Assert.assertNotNull(testService.getStatsService());
        Assert.assertEquals("Injected stats service is different",
                statsService, testService.getStatsService());

        // Call init. This will also ensure the statsService has initialised
        // (lazily)
        testService.init();

        statsService.init();

        Map<Class<?>, StatisticsDynamicMBean> stats = statsService
                .getStatsMBeansMap();
        Assert.assertEquals(1, stats.size());
        Assert.assertNotNull("No TestService stat",
                stats.get(TestService.class));
        tsStats = stats.get(TestService.class);

        tsStats.resetAll();
        // Assert.assertEquals(16,
        // tsStats.getMBeanInfo().getAttributes().length);
    }

    @Test
    public final void testResetAll() throws Exception {
        AtomicLong incrementCount = (AtomicLong) tsStats
                .getAttribute("Increment");
        AtomicLong multiCount = (AtomicLong) tsStats
                .getAttribute("Increment Multimethod Same Name");


        testService.increment();
        testService.increment();
        testService.increment();

        Assert.assertEquals(3, incrementCount.get());

        testService.stat1();
        testService.stat2();
        testService.stat2();
        Assert.assertEquals(3, multiCount.get());

        tsStats.resetAll();

        Assert.assertEquals(0, incrementCount.get());
        Assert.assertEquals(0, multiCount.get());

    }

    @Test
    public final void testReset() throws Exception {
        AtomicLong incrementCount = (AtomicLong) tsStats
                .getAttribute("Increment");
        AtomicLong multiCount = (AtomicLong) tsStats
                .getAttribute("Increment Multimethod Same Name");

        // Sanity check
        testService.increment();
        testService.increment();
        testService.increment();
        Assert.assertEquals(3, incrementCount.get());
        testService.stat1();
        testService.stat2();
        testService.stat2();
        Assert.assertEquals(3, multiCount.get());

        tsStats.reset("Increment");

        Assert.assertEquals(0, incrementCount.get());
        Assert.assertEquals(3, multiCount.get());

    }

    @Test
    public final void testDualModeStats() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        // check that we can access standard stat
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Standard Access 1");
        Assert.assertEquals(0, count.get());

        // check that we can access annotated stat
        AtomicLong count2 = (AtomicLong) tsStats.getAttribute("Increment");
        Assert.assertEquals(0, count2.get());

        // increment standard stat
        testService.incrementStandardAccess1();

        // check that it was incremented
        Statistic stat = tsStats.getStatistic("Standard Access 1");
        long lcount = stat.getCount().get();
        Assert.assertEquals(1, lcount);

        // clean up
        testService.setStandardAccessCount(0);
    }

    @Test
    public final void testSimpleIncrement() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        AtomicLong count = (AtomicLong) tsStats.getAttribute("Increment");

        // Sanity check
        Assert.assertEquals(0, count.get());
        testService.increment();

        Statistic stat = tsStats.getStatistic("Increment");
        long counts = stat.getCount().get();

        Assert.assertEquals(1, count.get());
        testService.increment();
        testService.increment();
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testMultiMethodSameNameIncrement()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Increment Multimethod Same Name");

        // Sanity check
        Assert.assertEquals(0, count.get());
        testService.stat1();
        testService.stat2();
        testService.stat2();
        Assert.assertEquals(3, count.get());
        testService.stat1();
        Assert.assertEquals(4, count.get());

    }

    @Test
    public final void testConditionalIncrement()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Increment Condition");

        // Sanity check
        Assert.assertEquals(0, count.get());

        TestObject test = new TestObject();

        testService.condition(test);

        // test will be false, so no increment
        Assert.assertEquals(0, count.get());

        test.setTest(true);

        testService.condition(test);

        // test will be true, so increment
        Assert.assertEquals(1, count.get());
        testService.condition(test);
        testService.condition(test);
        Assert.assertEquals(3, count.get());

        test.setTest(false);

        testService.condition(test);
        testService.condition(test);
        // test will be false, so no increment
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testResultIncrement() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Increment Result");

        // Sanity check
        Assert.assertEquals(0, count.get());

        TestObject test = new TestObject();

        testService.result(test);
        // result will be 0, test will be false, so no increment
        Assert.assertEquals(0, count.get());

        test.setResult(15);

        testService.result(test);

        // result will be 15, but test will be false, so no increment
        Assert.assertEquals(0, count.get());

        test.setTest(true);
        testService.result(test);

        // result will be 15, test will be true, so increment
        Assert.assertEquals(1, count.get());

        testService.result(test);
        testService.result(test);

        Assert.assertEquals(3, count.get());

        test.setResult(10);

        testService.result(test);

        // result will be 10, test will be true, so no increment
        Assert.assertEquals(3, count.get());

        test.setResult(15);

        testService.result(test);

        // result will be 15, test will be true, so increment
        Assert.assertEquals(4, count.get());
    }

    @Test
    public final void testVoidResultIncrement()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Increment Result Void");

        // Sanity check
        Assert.assertEquals(0, count.get());

        TestObject test = new TestObject();

        testService.resultVoid(test);
        testService.resultVoid(test);
        // result will be void, so no increment
        Assert.assertEquals(0, count.get());
    }

    @Test
    public final void testExIncrement() throws AttributeNotFoundException,
            MBeanException, ReflectionException {
        AtomicLong count = (AtomicLong) tsStats.getAttribute("Increment Ex");
        TestException e = new TestException();
        TestException ae = new AnotherTestException();
        // Sanity check
        Assert.assertEquals(0, count.get());
        try {
            testService.incrementOnException(e);
        } catch (Exception e1) {
        }

        Assert.assertEquals(1, count.get());
        try {
            testService.incrementOnException(e);
        } catch (Exception e1) {
        }
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnException(ae);
        } catch (Exception e1) {
        }
        // Wrong exception type thrown, so no increment
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnException(null);
        } catch (Exception e1) {
        }
        // No exception thrown, so no increment
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnException(new Exception());
        } catch (Exception e1) {
        }
        // Wrong exception type thrown, so no increment
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnException(e);
        } catch (Exception e1) {
        }
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testAnotherExIncrement()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Increment AnotherEx");
        TestException e = new AnotherTestException();
        TestException ae = new TestException();
        // Sanity check
        Assert.assertEquals(0, count.get());
        try {
            testService.incrementOnAnotherException(e);
        } catch (Exception e1) {
        }

        Assert.assertEquals(1, count.get());
        try {
            testService.incrementOnAnotherException(e);
        } catch (Exception e1) {
        }
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnAnotherException(ae);
        } catch (Exception e1) {
        }
        // Wrong exception type thrown, so no increment
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnAnotherException(null);
        } catch (Exception e1) {
        }
        // No exception thrown, so no increment
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnAnotherException(new Exception());
        } catch (Exception e1) {
        }
        // Wrong exception type thrown, so no increment
        Assert.assertEquals(2, count.get());

        try {
            testService.incrementOnAnotherException(e);
        } catch (Exception e1) {
        }
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testExConditionalIncrement()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        AtomicLong count = (AtomicLong) tsStats
                .getAttribute("Increment Ex Condition");
        TestException e = new TestException();

        // Sanity check
        Assert.assertEquals(0, count.get());

        TestObject test = new TestObject();
        try {
            testService.incrementOnExceptionCondition(test, e);
        } catch (Exception e1) {
        }

        // test will be false, so no increment
        Assert.assertEquals(0, count.get());

        test.setTest(true);

        try {
            testService.incrementOnExceptionCondition(test, e);
        } catch (Exception e1) {
        }

        // test will be true, so increment
        Assert.assertEquals(1, count.get());
        try {
            testService.incrementOnExceptionCondition(test, e);
        } catch (Exception e1) {
        }
        try {
            testService.incrementOnExceptionCondition(test, e);
        } catch (Exception e1) {
        }
        Assert.assertEquals(3, count.get());

        test.setTest(false);

        try {
            testService.incrementOnExceptionCondition(test, e);
        } catch (Exception e1) {
        }
        // test will be false, so no increment
        Assert.assertEquals(3, count.get());

        // Set to true, so the remaining tests should increment if the correct
        // TestException is thrown
        test.setTest(true);

        try {
            testService.incrementOnExceptionCondition(test, null);
        } catch (Exception e1) {
        }
        // No exception thrown, so no increment
        Assert.assertEquals(3, count.get());

        try {
            testService.incrementOnExceptionCondition(test, new Exception());
        } catch (Exception e1) {
        }
        // Wrong exception type thrown, so no increment
        Assert.assertEquals(3, count.get());

        test.setTest(true);

        try {
            testService.incrementOnExceptionCondition(test, e);
        } catch (Exception e1) {
        }
        Assert.assertEquals(4, count.get());
    }

    @Test
    public final void testMultiAnnotationIncrement() throws Exception {
        AtomicLong beforeCount = (AtomicLong) tsStats
                .getAttribute("All Before");
        AtomicLong resultCount = (AtomicLong) tsStats
                .getAttribute("All Result");
        AtomicLong exCount = (AtomicLong) tsStats.getAttribute("All Ex");

        TestException e = new TestException();
        TestObject test = new TestObject();

        // Sanity check
        Assert.assertEquals(0, beforeCount.get());
        Assert.assertEquals(0, resultCount.get());
        Assert.assertEquals(0, exCount.get());

        testService.multiAnnotation(null, test, null);

        Assert.assertEquals(0, beforeCount.get());
        Assert.assertEquals(0, resultCount.get());
        Assert.assertEquals(0, exCount.get());

        test.setTest(true);

        testService.multiAnnotation(null, test, null);

        Assert.assertEquals(1, beforeCount.get());
        Assert.assertEquals(0, resultCount.get());
        Assert.assertEquals(0, exCount.get());

        test.setResult(16);

        testService.multiAnnotation(null, test, null);

        Assert.assertEquals(2, beforeCount.get());
        Assert.assertEquals(1, resultCount.get());
        Assert.assertEquals(0, exCount.get());

        test.setResult(0);

        try {
            testService.multiAnnotation(null, test, e);
        } catch (Exception e1) {
        }

        Assert.assertEquals(3, beforeCount.get());
        Assert.assertEquals(1, resultCount.get());
        Assert.assertEquals(1, exCount.get());

    }

    @Test
    public final void testMultiAnnotation2Increment() throws Exception {
        AtomicLong beforeCount = (AtomicLong) tsStats.getAttribute("All Again");
        AtomicLong resultCount = (AtomicLong) tsStats.getAttribute("All More");

        TestException e = new TestException();
        TestObject test = new TestObject();

        // Sanity check
        Assert.assertEquals(0, beforeCount.get());
        Assert.assertEquals(0, resultCount.get());

        testService.multiAnnotation2(null, test, null);

        Assert.assertEquals(1, beforeCount.get());
        Assert.assertEquals(0, resultCount.get());

        test.setTest(true);

        testService.multiAnnotation2(null, test, null);

        Assert.assertEquals(2, beforeCount.get());
        Assert.assertEquals(0, resultCount.get());

        test.setResult(18);

        testService.multiAnnotation2(null, test, null);

        Assert.assertEquals(3, beforeCount.get());
        Assert.assertEquals(1, resultCount.get());

        test.setResult(0);

        try {
            testService.multiAnnotation2(null, test, e);
        } catch (Exception e1) {
        }

        Assert.assertEquals(4, beforeCount.get());
        Assert.assertEquals(1, resultCount.get());

    }

    @Test
    public final void testSimpleIncrementRollingSec()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        final AtomicLong avg = (AtomicLong) tsStats
                .getAttribute("Increment Rolling Sec: Rolling Avg");

        AveragingStatistic stat = (AveragingStatistic) tsStats
                .getStatistic("Increment Rolling Sec");

        int[] incrementsPerPeriod = new int[]{4, 12, 24, 6, 2, 8, 10, 2, 11,
                30, 12, 5, 3, 10, 12};
        long period = 200;
        int total = 0;
        for (int periodIndex = 0; periodIndex < incrementsPerPeriod.length; periodIndex++) {
            int incPerPeriod = incrementsPerPeriod[periodIndex];

            long incStart = System.currentTimeMillis();
            for (int increment = 0; increment < incPerPeriod; increment++) {

                stat.increment();
                total++;

                try {
                    /*
                     * Sleep so that each iteration of the seconds for loop is 1
					 * second
					 */
                    Thread.sleep(period / incPerPeriod);
                } catch (InterruptedException e) {
                }
            }

            try {
                /*
				 * Sleep so that each iteration of the seconds for loop is 1
				 * second
				 */
                long sleep = period - (System.currentTimeMillis() - incStart);
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
            }

        }

        Assert.assertEquals(total, stat.getCount().get());

		/*
		 * Call calculateAverage to calculate the average after incrementing has
		 * finished
		 */
        stat.calculateAverage();

        double periods = Math.floor(stat.getRollingTimeWindow() / period);
        System.out.println("periods :: " + stat.getRollingTimeWindow() + "/"
                + period + " = " + periods);
        double windows = Math.floor(incrementsPerPeriod.length / periods);
        System.out.println("windows :: " + incrementsPerPeriod.length + "/"
                + periods + " = " + windows);
        long myAvg = Math.round(total / windows);
        System.out.println("rolling avg :: " + total + "/" + windows + " = "
                + myAvg);

        Assert.assertEquals(myAvg, avg.get());
    }

    @Test
    public final void testDynamicName() throws AttributeNotFoundException,
            MBeanException, ReflectionException {

        // #name Statistic
        String name = "Harry";
        String expectedStatName = name + " Statistic";

        try {
            tsStats.getAttribute(expectedStatName);
            Assert.fail("Should've thrown an NPE");
        } catch (Exception e) {

        }

        testService.dynamicName(name);

        AtomicLong count = (AtomicLong) tsStats.getAttribute(expectedStatName);

        Assert.assertEquals(1, count.get());
        testService.dynamicName(name);
        testService.dynamicName(name);
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testDynamicName2() throws AttributeNotFoundException,
            MBeanException, ReflectionException {

        // #name #obj.result Statistic
        String name = "Bobo";
        TestObject obj = new TestObject(12);
        String expectedStatName = name + " " + obj.getResult() + " Statistic";

        try {
            tsStats.getAttribute(expectedStatName);
            Assert.fail("Should've thrown an NPE");
        } catch (Exception e) {

        }

        testService.dynamicName2(name, obj);

        AtomicLong count = (AtomicLong) tsStats.getAttribute(expectedStatName);

        Assert.assertEquals(1, count.get());
        testService.dynamicName2(name, obj);
        testService.dynamicName2(name, obj);
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testDynamicResultName()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        // #result Result Statistic
        String name = "Bob";
        String expectedStatName = name + " Result Statistic";

        try {
            tsStats.getAttribute(expectedStatName);
            Assert.fail("Should've thrown an NPE");
        } catch (Exception e) {

        }

        testService.dynamicResultName(name);

        AtomicLong count = (AtomicLong) tsStats.getAttribute(expectedStatName);

        Assert.assertEquals(1, count.get());
        testService.dynamicResultName(name);
        testService.dynamicResultName(name);
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testDynamicResultName2()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        // "#name #result Result Statistic"
        String name = "Pedro";
        String expectedStatName = name + " " + name + " Result Statistic";

        try {
            tsStats.getAttribute(expectedStatName);
            Assert.fail("Should've thrown an NPE");
        } catch (Exception e) {

        }

        testService.dynamicResultName2(name);

        AtomicLong count = (AtomicLong) tsStats.getAttribute(expectedStatName);

        Assert.assertEquals(1, count.get());
        testService.dynamicResultName2(name);
        testService.dynamicResultName2(name);
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void testDynamicResultName3()
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {

        // "#name #obj.result #result Result Statistic"
        String name = "Pedro";
        TestObject obj = new TestObject(12);
        String expectedStatName = name + " " + "12 " + name
                + "12 Result Statistic";

        try {
            tsStats.getAttribute(expectedStatName);
            Assert.fail("Should've thrown an NPE");
        } catch (Exception e) {

        }

        testService.dynamicResultName2(name, obj);

        AtomicLong count = (AtomicLong) tsStats.getAttribute(expectedStatName);

        Assert.assertEquals(1, count.get());
        testService.dynamicResultName2(name, obj);
        testService.dynamicResultName2(name, obj);
        Assert.assertEquals(3, count.get());

    }

    @Test
    public final void performanceTest() {

        double runs = 10;
        double runsForAverage = 50;

        String name = "Pedro";
        TestObject obj = new TestObject(12);

        System.out.println("Running No Stats...");

        StopWatch stopWatch = new StopWatch("Simple Performance Test");
        double statsTimeTotal = 0;
        for (int j = 0; j < runsForAverage; j++) {
            stopWatch.start("No Stats: " + j);

            for (int i = 0; i < runs; i++) {
                testService.performanceTest(name, obj);
            }
            stopWatch.stop();
            statsTimeTotal += stopWatch.getLastTaskTimeMillis();
        }
        double noStatsAvg = statsTimeTotal / runsForAverage;

        System.out.println("No Stats avg: " + noStatsAvg + "ms");
        System.out.println("Running Simple Stats...");

        double timeTotal = 0;
        for (int j = 0; j < runsForAverage; j++) {
            stopWatch.start("With Stats: " + j);
            for (int i = 0; i < runs; i++) {
                testService.increment();
            }
            stopWatch.stop();
            timeTotal += stopWatch.getLastTaskTimeMillis();
        }

        double statsAvg = timeTotal / runsForAverage;

        double diff = statsAvg - noStatsAvg;
        double perc = (diff / noStatsAvg * 100);

        System.out.println("Simple Stats avg: " + statsAvg
                + "ms, performance hit: %" + perc);
        Assert.assertTrue(
                "Simple performance hit is greater than %5. Actual: %" + perc,
                perc <= 5);

        System.out.println("Running Conditional Stats...");
        timeTotal = 0;
        for (int j = 0; j < runsForAverage; j++) {
            stopWatch.start("With Conditional Stats: " + j);
            for (int i = 0; i < runs; i++) {
                testService.condition(obj);
            }
            stopWatch.stop();
            timeTotal += stopWatch.getLastTaskTimeMillis();
        }

        statsAvg = timeTotal / runsForAverage;

        diff = statsAvg - noStatsAvg;
        perc = (diff / noStatsAvg * 100);

        System.out.println("Conditional Stats avg: " + statsAvg
                + "ms, performance hit: %" + perc);

        Assert.assertTrue(
                "Conditional performance hit is greater than %5. Actual: %"
                        + perc, perc <= 5);

        System.out.println("Running Dynamic Stats...");

        timeTotal = 0;
        for (int j = 0; j < runsForAverage; j++) {
            stopWatch.start("With Dynamic Stats: " + j);
            for (int i = 0; i < runs; i++) {
                testService.dynamicResultName2(name, obj);
            }
            stopWatch.stop();
            timeTotal += stopWatch.getLastTaskTimeMillis();
        }

        statsAvg = timeTotal / runsForAverage;

        diff = statsAvg - noStatsAvg;
        perc = (diff / noStatsAvg * 100);

        System.out.println("Dynamic Stats avg: " + statsAvg
                + "ms, performance hit: %" + perc);

        System.out.println("\n\n" + stopWatch.prettyPrint());

        Assert.assertTrue(
                "Dynamic performance hit is greater than %5. Actual: %" + perc,
                perc <= 5);
    }

}
