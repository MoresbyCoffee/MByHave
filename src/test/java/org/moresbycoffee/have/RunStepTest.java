/*
 * Moresby Coffee Bean
 *
 * Copyright (c) 2012, Barnabas Sudy (barnabas.sudy@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package org.moresbycoffee.have;

import static org.junit.Assert.*;

import org.junit.Test;
import org.moresbycoffee.have.MByHave;
import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.exceptions.MByHaveAssertionError;


/**
 * TODO javadoc.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class RunStepTest {

    private boolean noParamGiven  = false;
    private boolean oneParamGiven = false;
    private String  oneParamParam = null;
    private boolean regExpGiven   = false;

    @Given(definition = " a no param given method")
    public void noParamGiven() {
        this.noParamGiven = true;
    }

    @Given(definition = "one parameter $parameter")
    public void oneParamGiven(final String parameter) {
        this.oneParamGiven = true;
        this.oneParamParam = parameter;
    }

    @Given(definition = "test fail")
    public void testFail() {
        fail("This is a test. It have had to happen.");
    }

    @Given(definition = "(.*) pattern")
    public void regExPatternTest() {
        this.regExpGiven = true;
    }

    @Test
    public void runGiven() {
        new MByHave(this).given(" a no param given method");
        assertTrue(noParamGiven);
    }

    @Test
    public void runGivenOneParameter() {
        new MByHave(this).given("one parameter test parameter text");
        assertTrue(oneParamGiven);
        assertEquals("test parameter text", oneParamParam);
    }

    @Test
    public void runGivenOneParameterLineBreak() {
        new MByHave(this).given("one parameter test parameter\n text");
        assertTrue(oneParamGiven);
        assertEquals("test parameter\n text", oneParamParam);
    }

    @Test
    public void testFailTest() {
        boolean assertionError = false;
        try {
            new MByHave(this).given("test fail");
        } catch (final MByHaveAssertionError e) {
            throw e;
        } catch (final AssertionError e) {
            assertionError = true;
        }
        assertTrue(assertionError);
    }

    @Test(expected = MByHaveAssertionError.class)
    public void testReqEx() {
        new MByHave(this).given("dgagfdsagsa pattern");
    }

    @Test
    public void testReqEx2() {
        new MByHave(this).given("(.*) pattern");
        assertTrue(regExpGiven);
    }

    private boolean twoParamGivenMethod = false;
    private String  twoParamGivenMethodParam1 = null;
    private String  twoParamGivenMethodParam2 = null;

    private boolean secondTwoParamGivenMethod = false;
    private String  secondTwoParamGivenMethodParam1 = null;
    private String  secondTwoParamGivenMethodParam2 = null;

    @Given(definition = "given text $firstParam $secondParam blah blah")
    public void twoParamGivenMethod(final String firstParam, final String secondParam) {
        this.twoParamGivenMethodParam1 = firstParam;
        this.twoParamGivenMethodParam2 = secondParam;

        this.twoParamGivenMethod = true;

    }

    @Given(definition = "second text $firstParam $secondParam")
    public void secondTwoParamGivenMethod(final String firstParam, final String secondParam) {
        this.secondTwoParamGivenMethodParam1 = firstParam;
        this.secondTwoParamGivenMethodParam2 = secondParam;

        this.secondTwoParamGivenMethod = true;
    }

    @Test
    public void testTwoTwoParameterGiven() {
        new MByHave(this).given("second text blah \nblah  test").given("given text blah blah blah blah");

        assertTrue(secondTwoParamGivenMethod);
        assertEquals("blah \nblah ", secondTwoParamGivenMethodParam1);
        assertEquals("test", secondTwoParamGivenMethodParam2);

        assertTrue(twoParamGivenMethod);
        assertEquals("blah", twoParamGivenMethodParam1);
        assertEquals("blah", twoParamGivenMethodParam2);

    }

    @Test
    public void testRunScenario() {
        new MByHave(this).runScenario("Given second text blah blah\n" +
                                      "Given given text blah blah1 \ndsfas blah blah");

        assertTrue(secondTwoParamGivenMethod);
        assertEquals("blah", secondTwoParamGivenMethodParam1);
        assertEquals("blah", secondTwoParamGivenMethodParam2);

        assertTrue(twoParamGivenMethod);
        assertEquals("blah blah1", twoParamGivenMethodParam1);
        assertEquals("dsfas", twoParamGivenMethodParam2);
    }


}
