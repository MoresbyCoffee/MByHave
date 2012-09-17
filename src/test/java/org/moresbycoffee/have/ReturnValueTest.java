/*
 * Masabi Ltd.
 * 56 Ayres Street, London, SE1 1EU
 * http://www.masabi.com/
 *
 * Copyright (c) 2012 Masabi Ltd. All rights reserved.
 */
package org.moresbycoffee.have;

import static org.junit.Assert.*;

import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.annotations.Then;

/**
 * Step definition for {@link ReturnValue} tests used by {@link ReturnValueTestEmbedded} and {@link ReturnValueTestStoryFile} test cases.
 *
 * @author Barnabas Sudy<barnabas.sudy@gmail.com>
 * @since 2012
 */
public abstract class ReturnValueTest {

    private static final String TEST_STRING = "TestString";

    @Given("a string return value")
    public String returnValue() {
        return TEST_STRING;
    }
    
    @Given("a null Integer return value")
    public Integer intReturnValue() {
        return null;
    }
    
    @Then("the string return value is available in this method")
    public void assertReturnValue(ReturnValue<String> returnValue) {
        assertEquals(TEST_STRING, returnValue.getValue());
    }
    
    @Then("the null Integer is here")
    public void assertNullReturnValue(ReturnValue<Integer> returnValue) {
        assertNull(returnValue.getValue());
    }
    
    @Then("no Boolean return value existing")
    public void assertNoReturnValue(ReturnValue<Boolean> returnValue) {
        assertNull(returnValue);
    }
    
    @Then("the first scenarios return value shouldn't be here")
    public void checkScenarioClear(ReturnValue<String> returnValue) {
        assertNull(returnValue);
    }
}
