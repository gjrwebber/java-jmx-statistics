/**
 * RollingAvgStatisticTest.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats;

import org.gw.stats.AveragingStatistic.ROLLING_AVG_WINDOW;
import org.gw.stats.Statistic.LOG_LEVEL;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class RollingAvgStatisticTest {

    /**
     * Test method for
     * {@link AveragingStatistic#increment()}
     * .
     *
     * @throws InterruptedException
     */
    @Test
    public final void testIncrement() throws InterruptedException {
        AveragingStatistic stat = new AveragingStatistic("", "", null,
                LOG_LEVEL.NONE, ROLLING_AVG_WINDOW.HOUR, null);
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        Assert.assertEquals(0, stat.getRollingAvg().get());
        stat.increment();
        Thread.sleep(100);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, stat.getHistoricValues().size());
        stat.increment();
        Assert.assertEquals(2, stat.getCount().get());
        Assert.assertEquals(2, stat.getHistoricValues().size());

    }

    /**
     * Test method for
     * {@link AveragingStatistic#reset()}
     * .
     *
     * @throws InterruptedException
     */
    @Test
    public final void testReset() throws InterruptedException {
        AveragingStatistic stat = new AveragingStatistic("", "", null,
                LOG_LEVEL.NONE, ROLLING_AVG_WINDOW.HOUR, null);
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        Assert.assertEquals(0, stat.getRollingAvg().get());
        stat.increment();
        Thread.sleep(100);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, stat.getHistoricValues().size());
        stat.increment();
        Thread.sleep(100);
        Assert.assertEquals(2, stat.getCount().get());
        Assert.assertEquals(2, stat.getHistoricValues().size());
        stat.reset();
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());

    }

    /**
     * Test method for
     * {@link AveragingStatistic#disableRolling()}
     * .
     *
     * @throws InterruptedException
     */
    @Test
    public final void testDisableRolling() throws InterruptedException {
        AveragingStatistic stat = new AveragingStatistic("", "", null,
                LOG_LEVEL.NONE, ROLLING_AVG_WINDOW.HOUR, null);
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        stat.increment();
        Thread.sleep(100);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, stat.getHistoricValues().size());
        stat.disableRolling();
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        stat.increment();
        Assert.assertEquals(2, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());

    }

    /**
     * Test method for
     * {@link AveragingStatistic#enableRolling()}
     * .
     *
     * @throws InterruptedException
     */
    @Test
    public final void testEnableRolling() throws InterruptedException {
        AveragingStatistic stat = new AveragingStatistic("", "", null,
                LOG_LEVEL.NONE, ROLLING_AVG_WINDOW.HOUR, null);
        Assert.assertEquals(0, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        stat.increment();
        Thread.sleep(100);
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(1, stat.getHistoricValues().size());
        stat.disableRolling();
        Assert.assertEquals(1, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        stat.increment();
        Thread.sleep(100);
        Assert.assertEquals(2, stat.getCount().get());
        Assert.assertEquals(0, stat.getHistoricValues().size());
        stat.enableRolling();
        stat.increment();
        Assert.assertEquals(3, stat.getCount().get());
        Assert.assertEquals(1, stat.getHistoricValues().size());
    }

    @Test
    public final void testCleanHistory() throws InterruptedException {

        AveragingStatistic stat = new AveragingStatistic("", "", null,
                LOG_LEVEL.NONE, ROLLING_AVG_WINDOW.DAY, null);

        stat.increment();
        Thread.sleep(100);

        Date expire = new Date();
        stat.increment();
        Thread.sleep(100);
        Date expire2 = new Date();
        stat.increment();
        Thread.sleep(100);

        Assert.assertEquals(3, stat.getCount().get());
        Assert.assertEquals(3, stat.getHistoricValues().size());
        Assert.assertEquals(1, stat.getHistoricValues().get(0).longValue());
        Assert.assertEquals(2, stat.getHistoricValues().get(1).longValue());
        Assert.assertEquals(3, stat.getHistoricValues().get(2).longValue());

        stat.cleanHistory(expire.getTime());
        Assert.assertEquals(2, stat.getHistoricValues().size());
        Assert.assertEquals(2, stat.getHistoricValues().get(0).longValue());
        Assert.assertEquals(3, stat.getHistoricValues().get(1).longValue());
        stat.cleanHistory(expire2.getTime());
        Assert.assertEquals(1, stat.getHistoricValues().size());
        Assert.assertEquals(3, stat.getHistoricValues().get(0).longValue());
        stat.cleanHistory(System.currentTimeMillis());
        Assert.assertEquals(0, stat.getHistoricValues().size());
    }

    @Test
    public final void testRolling() {

        AveragingStatistic stat = new AveragingStatistic("", "", null,
                LOG_LEVEL.NONE, ROLLING_AVG_WINDOW.SECOND, null);

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
        long avg = Math.round(total / windows);
        System.out.println("rolling avg :: " + total + "/" + windows + " = "
                + avg);

        Assert.assertEquals(avg, stat.getRollingAvg().get());

    }

}
