/**
 * TestObject.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.aop;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public class TestObject {

    private boolean test = false;
    private int result = 0;

    public TestObject() {

    }

    public TestObject(boolean test) {
        this.test = test;
    }

    public TestObject(int result) {
        this.result = result;
    }

    public TestObject(boolean test, int result) {
        this.test = test;
        this.result = result;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
