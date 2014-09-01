/**
 * TestSErvice.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.aop;

import org.gw.stats.AveragingStatistic.ROLLING_AVG_WINDOW;
import org.gw.stats.Statistic.LOG_LEVEL;
import org.gw.stats.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
@Service
@ManagedResource(value = "org.gw.test:type=TestService")
public class TestService {

    int standardAccessCount;
    int simpleCount;
    int conditionCount;
    int resultCount;
    int exCount;
    int exConditionCount;
    int exUnlessCount;
    int performanceDrag = 10;
    @Autowired
    private StatisticsService statsService;

    // int falseCount;
    // int trueCount;
    // int falseConditionCount;
    // int trueConditionCount;
    // int falseUnlessCount;
    // int trueUnlessCount;

    @IncrementStat("Init")
    public void init() {
        System.out.println("init");

        // initialise standard access stats
        statsService.initialiseStats("Standard Access 1", TestService.class);
    }

    public void incrementStandardAccess1() {
        statsService.incrementStats("Standard Access 1", TestService.class);
        standardAccessCount++;
    }

    @IncrementStat("Increment")
    public void increment() {
        try {
            Thread.sleep(performanceDrag);
        } catch (InterruptedException e) {
        }
        simpleCount++;
    }

    @IncrementStat(value = "Increment Rolling Sec", rollingAvgWindow = ROLLING_AVG_WINDOW.SECOND)
    public void incrementRolling() {
        simpleCount++;
    }

    @IncrementStat(value = "Increment Multimethod Same Name")
    public void stat1() {

    }

    @IncrementStat(value = "Increment Multimethod Same Name")
    public void stat2() {

    }

    @IncrementStat(value = "Increment Condition", condition = "#testObject.test == true")
    public void condition(TestObject testObject) {
        try {
            Thread.sleep(performanceDrag);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        conditionCount++;
    }

    @IncrementStat(value = "Increment Result", condition = "#testObject.test == true && #result == 15")
    public int result(TestObject testObject) {
        resultCount++;
        return testObject.getResult();
    }

    @IncrementStat(value = "Increment Result Void", condition = "#result == 15")
    public void resultVoid(TestObject testObject) {
        resultCount++;
    }

    // @IncrementStats("Increment")
    // public void incrementOnFalse() {
    // simpleCount++;
    // }

    @IncrementStat(value = "Increment Ex", throwing = TestException.class)
    public void incrementOnException(Exception e) throws Exception {
        if (e != null && e instanceof TestException) {
            exCount++;
        }

        throw e;
    }

    @IncrementStat(value = "Increment AnotherEx", throwing = AnotherTestException.class)
    public void incrementOnAnotherException(Exception e) throws Exception {
        if (e != null && e instanceof AnotherTestException) {
            exCount++;
        }
        throw e;
    }

    @IncrementStat(value = "Increment Ex Condition", condition = "#testObject.test == true", throwing = TestException.class)
    public void incrementOnExceptionCondition(TestObject testObject, Exception e)
            throws Exception {
        if (e != null && e instanceof TestException) {
            exConditionCount++;
            throw e;
        }
    }

    @IncrementStats({
            @IncrementStat(value = "All Before", condition = "#testObject.test == true", logLevel = LOG_LEVEL.DEBUG),
            @IncrementStat(value = "All Result", condition = "#testObject.test == true && #result == 16"),
            @IncrementStat(value = "All Ex", condition = "#testObject.test == true", throwing = TestException.class)})
    public int multiAnnotation(TestObject to, TestObject testObject, Exception e)
            throws Exception {

        if (e != null && e instanceof TestException) {
            throw e;
        }
        return testObject.getResult();
    }

    @IncrementStats({
            @IncrementStat("All Again"),
            @IncrementStat(value = "All More", condition = "#testObject.test == true && #result == 18")})
    public int multiAnnotation2(TestObject to, TestObject testObject,
                                TestException e) throws TestException, AnotherTestException {

        if (e != null && e instanceof TestException) {
            throw e;
        }
        return testObject.getResult();
    }

    @IncrementStats({
            @IncrementStat(value = "All Same", condition = "#testObject.test == true"),
            @IncrementStat(value = "All Same", condition = "#testObject.test == true && #result == 16"),
            @IncrementStat(value = "All Same", condition = "#testObject.test == true", throwing = TestException.class)})
    public int multiAnnotationSameName(TestObject testObject, Exception e)
            throws Exception {

        if (e != null && e instanceof TestException) {
            throw e;
        }
        return testObject.getResult();
    }

    @IncrementStat(value = "#name Statistic")
    public void dynamicName(String name) {

    }

    @IncrementStat(value = "#name #obj.result Statistic")
    public void dynamicName2(String name, TestObject obj) {

    }

    @IncrementStat(value = "#result Result Statistic")
    public String dynamicResultName(String name) {
        return name;
    }

    @IncrementStat(value = "#name #result Result Statistic")
    public String dynamicResultName2(String name) {
        return name;
    }

    @IncrementStat(value = "#name #obj.result #result Result Statistic")
    public String dynamicResultName2(String name, TestObject obj) {
        try {
            Thread.sleep(performanceDrag);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return name + obj.getResult();
    }

    public String performanceTest(String name, TestObject obj) {
        try {
            Thread.sleep(performanceDrag);
        } catch (InterruptedException e) {
        }
        return name + obj.getResult();
    }

    public int getStandardAccessCount() {
        return standardAccessCount;
    }

    public void setStandardAccessCount(int standardAccessCount) {
        this.standardAccessCount = standardAccessCount;
    }

    public int getSimpleCount() {
        return simpleCount;
    }

    public void setSimpleCount(int simpleCount) {
        this.simpleCount = simpleCount;
    }

    public int getConditionCount() {
        return conditionCount;
    }

    public void setConditionCount(int conditionCount) {
        this.conditionCount = conditionCount;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int unlessCount) {
        this.resultCount = unlessCount;
    }

    public StatisticsService getStatsService() {
        return statsService;
    }

    public void setStatsService(StatisticsService statsService) {
        this.statsService = statsService;
    }
}
