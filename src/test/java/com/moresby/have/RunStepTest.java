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
package com.moresby.have;

import static org.junit.Assert.*;

import org.junit.Test;

import com.moresby.have.annotations.Given;
import com.moresby.have.exceptions.mByHaveAssertionError;

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

    @Given(" a no param given method")
    public void noParamGiven() {
        this.noParamGiven = true;
    }

    @Given("one parameter $parameter")
    public void oneParamGiven(final String parameter) {
        this.oneParamGiven = true;
        this.oneParamParam = parameter;
    }

    @Given("test fail")
    public void testFail() {
        fail("This is a test. It have had to happen.");
    }

    @Given("(.*) pattern")
    public void regExPatternTest() {
        this.regExpGiven = true;
    }

    @Test
    public void runGiven() {
        new mByHave(this).given(" a no param given method");
        assertTrue(noParamGiven);
    }

    @Test
    public void runGivenOneParameter() {
        new mByHave(this).given("one parameter test parameter text");
        assertTrue(oneParamGiven);
        assertEquals("test parameter text", oneParamParam);
    }

    @Test
    public void runGivenOneParameterLineBreak() {
        new mByHave(this).given("one parameter test parameter\n text");
        assertTrue(oneParamGiven);
        assertEquals("test parameter\n text", oneParamParam);
    }

    @Test
    public void testFailTest() {
        boolean assertionError = false;
        try {
            new mByHave(this).given("test fail");
        } catch (final mByHaveAssertionError e) {
            throw e;
        } catch (final AssertionError e) {
            assertionError = true;
        }
        assertTrue(assertionError);
    }

    @Test(expected = mByHaveAssertionError.class)
    public void testReqEx() {
        new mByHave(this).given("dgagfdsagsa pattern");
    }

}