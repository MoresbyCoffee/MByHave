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
import org.moresbycoffee.have.exceptions.MByHaveAssertionError;
import org.moresbycoffee.have.exceptions.MByHaveException;


/**
 * Tests the {@link MByHave#runScenario(String)} method.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class RunScenarioTest {

    private boolean first = false;
    private boolean second = false;
    private boolean third = false;

    private String parameter;
    private String parameter1;
    private String parameter2;

    @Given(definition = "first method")
    public void given() {
        first = true;
    }

    @When(definition = "second method $parameter")
    public void when(final String parameter) {
        second = true;
        this.parameter = parameter;
    }

    @Then(definition = "third $parameter1 $parameter2 method")
    public void then(final String parameter1, final String parameter2) {
        third = true;
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
    }

    @Test(expected = NullPointerException.class)
    public void runNull() {

        new MByHave(this).runScenario(null);
    }

    @Test
    public void runGiven() {
        new MByHave(this).runScenario("Given first method");
        assertTrue(first);
        assertFalse(second);
        assertFalse(third);
    }

    @Test(expected = AssertionError.class)
    public void runUnknownGiven() {
        try {
            new MByHave(this).runScenario("Given unknown given pattern");
        } catch (final AssertionError e) {
            assertTrue(e instanceof MByHaveAssertionError);
            throw e;
        }
        fail("It should have thrown a AssertionError.");
    }

    @Test
    public void runWithScenarioDescription() {
        new MByHave(this).runScenario("Scenario Scenario description \n" +
        		                       "Given first method");
        assertTrue(first);
        assertFalse(second);
        assertFalse(third);
    }

    @Test(expected = MByHaveException.class)
    public void runWithTwoScenarioDescription1() {
        new MByHave(this).runScenario("Scenario Scenario description \n" +
                                       "Scenario Second description \n" +
                                       "Given first method");
        assertTrue(first);
        assertFalse(second);
        assertFalse(third);
    }

    @Test(expected = MByHaveException.class)
    public void runWithTwoScenarioDescription2() {
        new MByHave(this).runScenario("Scenario Scenario description \n" +
                                       "Given first method\n" +
                                       "Scenario Second description");
        assertTrue(first);
        assertFalse(second);
        assertFalse(third);
    }

    @Test
    public void runWhen() {
        new MByHave(this).runScenario("When second method parameter123");
        assertFalse(first);
        assertTrue(second);
        assertFalse(third);
        assertEquals("parameter123", parameter);
    }


    @Test(expected = AssertionError.class)
    public void runUnknownWhen() {
        try {
            new MByHave(this).runScenario("When unknown given pattern");
        } catch (final AssertionError e) {
            assertTrue(e instanceof MByHaveAssertionError);
            throw e;
        }
        fail("It should have thrown a AssertionError.");
    }

    @Test
    public void runGivenWhen() {
        new MByHave(this).runScenario("Given first method\n" +
                                       "When second method parameter 123");
        assertTrue(first);
        assertTrue(second);
        assertFalse(third);
        assertEquals("parameter 123", parameter);
    }

    @Test
    public void runGivenWhenTwice() {
        new MByHave(this).runScenario("Given first method\n" +
                                       "When second method parameter 123\n" +
                                       "When second method second method");
        assertTrue(first);
        assertTrue(second);
        assertFalse(third);
        assertEquals("second method", parameter);
    }

    @Test
    public void runThen() {
        new MByHave(this).runScenario("Then third firstparam secondparam method");
        assertFalse(first);
        assertFalse(second);
        assertTrue(third);
        assertEquals("firstparam", parameter1);
        assertEquals("secondparam", parameter2);
    }

    @Test(expected = AssertionError.class)
    public void runUnknownThen() {
        try {
            new MByHave(this).runScenario("Then unknown given pattern");
        } catch (final AssertionError e) {
            assertTrue(e instanceof MByHaveAssertionError);
            throw e;
        }
        fail("It should have thrown a AssertionError.");
    }

    @Test
    public void runGivenThen() {
        new MByHave(this).runScenario("Given first method\n" +
                                       "Then third parameter 123 second method");
        assertTrue(first);
        assertFalse(second);
        assertTrue(third);

        assertEquals("parameter 123", parameter1);
        assertEquals("second", parameter2);
    }

    @Test
    public void runGivenWhenThen() {
        new MByHave(this).runScenario("Given first method\n" +
                                       "When second method when parameter\n" +
                                       "Then third parameter 123 second method");
        assertTrue(first);
        assertTrue(second);
        assertTrue(third);
        assertEquals("when parameter", parameter);
        assertEquals("parameter 123", parameter1);
        assertEquals("second", parameter2);
    }

    @Test
    public void runGivenWhenThenWithMethods() {
        new MByHave(this).given("first method").
                          when("second method when parameter").
                          then("third parameter 123 second method");
        assertTrue(first);
        assertTrue(second);
        assertTrue(third);
        assertEquals("when parameter", parameter);
        assertEquals("parameter 123", parameter1);
        assertEquals("second", parameter2);
    }


}
