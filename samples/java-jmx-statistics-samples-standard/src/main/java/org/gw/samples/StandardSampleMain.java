package org.gw.samples;

import org.gw.stats.AveragingStatistic;
import org.gw.stats.JMXStatisticsService;
import org.gw.stats.StatisticsService;

/**
 * java jmx statistics example main method demonstrating the use without
 * annotations, spring or aspectj (no frills).
 *
 * @author Gman
 */
public class StandardSampleMain {

    public StandardSampleMain() {

    }

    public static void main(String[] args) throws Exception {

        StatisticsService statsService = new JMXStatisticsService();

        statsService.initialiseStats("Start", StandardSampleMain.class, AveragingStatistic.ROLLING_AVG_WINDOW.HOUR);

        statsService.initialiseStats("End", StandardSampleMain.class);

        // Make example standard call to statsService
        statsService.incrementStats(5, "Start", StandardSampleMain.class);

        // Make example standard call to statsService
        statsService.incrementStats(3, "End", StandardSampleMain.class);


        System.out.println("Open up JConsole and have a look. Shutdown in Jconsole.");

        Thread.sleep(600000);

    }

}
