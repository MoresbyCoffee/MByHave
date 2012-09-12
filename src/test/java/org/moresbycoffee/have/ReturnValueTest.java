/*
 * Masabi Ltd.
 * 56 Ayres Street, London, SE1 1EU
 * http://www.masabi.com/
 *
 * Copyright (c) 2012 Masabi Ltd. All rights reserved.
 */
package org.moresbycoffee.have;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.annotations.Story;
import org.moresbycoffee.have.annotations.Then;

/**
 * 
 *
 * @author bsudy
 * @since 2012
 */
@RunWith(MByHaveRunner.class)
@Story(files = { "returnValue.story "})
public class ReturnValueTest {

    private static final String TEST_STRING = "TestString";

    @Given("a return value")
    public String returnValue() {
        return TEST_STRING;
    }
    
    @Given("an Integer return value")
    public Integer intReturnValue() {
        return Integer.MAX_VALUE;
    }
    
    @Then("it is available in the next method")
    public void assertReturnValue(ReturnValue<String> returnValue) {
        assertEquals(TEST_STRING, returnValue.getValue());
    }
    
}
