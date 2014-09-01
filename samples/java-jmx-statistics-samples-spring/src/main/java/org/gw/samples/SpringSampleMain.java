package org.gw.samples;

import org.gw.samples.model.MyObject;
import org.gw.samples.service.ExampleService;
import org.gw.samples.service.ServiceException;
import org.gw.stats.StatisticsService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * java jmx statistics example main method demonstrating the use of Spring Aop
 * proxying for loading the annotation driven statistics service. It first loads
 * the Spring config, then the StatisticsService, ExampleService all from
 * Springs ApplicationContext.
 *
 * @author Gman
 */
public class SpringSampleMain {

    public SpringSampleMain() {

    }

    public static void main(String[] args) throws InterruptedException {

        // Load Spring
        ApplicationContext ctx = new AnnotationConfigApplicationContext(
                AppConfig.class);


        // Load the StatisticsService so we can make standard calls
        StatisticsService statsService = ctx.getBean(StatisticsService.class);

        // Make example standard call to statsService
        statsService.incrementStats("Start", SpringSampleMain.class);

        // Load MyService spring bean
        ExampleService myService = ctx.getBean(ExampleService.class);

        // Now call a bunch of methods on the service to get some stats
        myService.AlwaysIncrement();
        try {
            myService.MultipleStats("Harry");
        } catch (ServiceException e) {
        }
        myService.AverageOverSecond();
        myService.DynamicName("Dynamic");
        myService.IncrementOnReturnTrue();
        myService.NameEqualsHarry("Harry");
        myService.NameEqualsHarryIfReturnTrueAlsoLog("Harry");
        try {
            myService.IncrementOnServiceException("Harry");
        } catch (ServiceException e) {
        }

        // Because we are using Spring to setup the stats, then the following
        // method calls does not produce any stats. In fact,MyObject is not in
        // the stats list in JConsole.
        MyObject pojo = new MyObject();
        try {
            pojo.DynamicName("Harry");
            pojo.DynamicLara("Lara");
        } catch (Exception e) {
        }


        System.out.println("Open up JConsole and have a look. Shutdown in Jconsole.");

        Thread.sleep(600000);

    }

}
