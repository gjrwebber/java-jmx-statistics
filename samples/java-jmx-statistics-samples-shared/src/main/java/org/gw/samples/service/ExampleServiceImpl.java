package org.gw.samples.service;

import org.gw.stats.AveragingStatistic;
import org.gw.stats.Statistic;
import org.gw.stats.aop.IncrementStat;
import org.gw.stats.aop.IncrementStats;

import java.util.Random;


public abstract class ExampleServiceImpl implements ExampleService {

    private Random random = new Random();

    public ExampleServiceImpl() {

    }

    /**
     * Creates a new Statisitc called AlwaysIncrement and associates it with
     * ExampleServiceImpl.class. This will register the statistics with the
     * ObjectName: example.service.stats:type=MyAnnotatedService This statistic
     * will increment on every execution of this method. If using AspectJ for
     * setup PrivateInternalIncrement will equal this statistic.
     */
    @IncrementStat("AlwaysIncrement")
    public void AlwaysIncrement() {
        // .. do something
        PrivateInternalIncrement();
    }


    /**
     * Same as AlwaysIncrement, though it will only increment when using AspectJ for setup
     */
    @IncrementStat("PrivateInternalIncrement")
    private void PrivateInternalIncrement() {
        // .. do something
    }

    /**
     * Same as AlwaysIncrement, though it will only increment if the method returns true
     */
    @IncrementStat(value = "IncrementOnReturnTrue", condition = "#result == true")
    public boolean IncrementOnReturnTrue() {
        // .. do something
        return random.nextBoolean();
    }

    /**
     * Similar to AlwaysIncrement, however for every value of the parameter 'name' will
     * result in an individual statistic named 'Dynamic #name' where #name is
     * the value of the parameter at runtime
     */
//	@IncrementStat("Dynamic #name")
    public boolean DynamicName(String name) {
        // .. do something
        return true;
    }

    /**
     * Same as IncrementOnReturnTrue, though it will only increment if the provided name equals
     * 'Harry' and the method returns true
     */
    @IncrementStat(value = "NameEqualsHarry", condition = "#name == 'Harry'")
    public boolean NameEqualsHarry(String name) {
        // .. do something
        return true;
    }

    /**
     * Same as NameEqualsHarry, but it will also log the statistic if logging is enabled
     * for debug
     */
    @IncrementStat(value = "NameEqualsHarry", condition = "#name == 'Harry' && #result == true", logLevel = Statistic.LOG_LEVEL.DEBUG)
    public boolean NameEqualsHarryIfReturnTrueAlsoLog(String name) {
        // .. do something
        return random.nextBoolean();
    }

    /**
     * Same as AlwaysIncrement, though it will only increment if the given
     * ServiceException is thrown
     */
    @IncrementStat(value = "IncrementOnServiceException", throwing = ServiceException.class)
    public void IncrementOnServiceException(String name)
            throws ServiceException {
        if (random.nextBoolean()) throw new ServiceException();
    }

    /**
     * Multiple annotations on one method
     */
    @IncrementStats({
            @IncrementStat(value = "IncrementOnServiceException", throwing = ServiceException.class),
            @IncrementStat(value = "NameEqualsHarry", condition = "#name == 'Harry'"),
            @IncrementStat(value = "IncrementOnReturnTrue", condition = "#result == true")})
    public boolean MultipleStats(String name) throws ServiceException {
        if (random.nextBoolean()) throw new ServiceException();
        else return true;
    }

    /**
     * Same as AlwaysIncrement, though it will also provide an average count of the last 1
     * second window
     */
    @IncrementStat(value = "AverageOverSecond", rollingAvgWindow = AveragingStatistic.ROLLING_AVG_WINDOW.SECOND)
    public void AverageOverSecond() {
        // .. do something
    }

}
