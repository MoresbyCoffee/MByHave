/*
 * Moresby Coffee Bean
 *
 * Copyright (c) 2012, Barnabas Sudy (barnabas.sudy@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.moresbycoffee.have;

import static org.junit.Assert.*;

import org.junit.Test;
import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.annotations.Then;
import org.moresbycoffee.have.annotations.When;

/**
 * Tests the priority of the steps. The higher priority value means the method will be matched first.
 *
 * @author bsudy
 * @since 2012
 */
public class StepPriorityTest {

    private boolean firstTest = false; 
    
    @Given(value = "a method with a $parameter", priority = 1)
    public void givenTestMethodA(final String parameter) {
        fail();
    }
    
    @Given(value = "a method with a $parameter plus something", priority = 2)
    public void givenTestMethodB(final String parameter) {
        firstTest = true;
    }
    
    /**
     * Tests priority in one direction. Methods in <i>false, right</i> order.
     */
    @Test 
    public void testGivenPriority() {
        new MByHave(this).given("a method with a parameter plus something");
        assertTrue(firstTest);
    }
    
    private boolean secondTest = false;
    
    @Then(value = "a then method with $parameter", priority = 2)
    public void thenTestMethodA(final String parameter) {
        secondTest = true;
    }
    
    @Then(value = "a then method with a $parameter plus something", priority = 1)
    public void thenTestMethodB(final String parameter) {
        fail();
    }
    
    /**
     * Tests priority in other direction. Methods in <i>right, false</i> order.
     */
    @Test
    public void testThenPriority() {
        new MByHave(this).then("a then method with a parameter plus something");
        assertTrue(secondTest);
    }

    private boolean thirdTest = false;
    
    @When(value = "a then method with $parameter", priority = 2)
    public void whenTestMethodA(final String parameter) {
        thirdTest = true;
    }
    
    @When(value = "a then method with a $parameter", priority = 1)
    public void whenTestMethodB(final String parameter) {
        fail();
    }
    
    /**
     * Tests two equivalent methods with different priority. 
     */
    @Test
    public void testWhenPriority() {
        new MByHave(this).when("a then method with a parameter");
        assertTrue(thirdTest);
    }
    
    
}
