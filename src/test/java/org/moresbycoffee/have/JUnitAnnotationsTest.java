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

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.moresbycoffee.have.annotations.Story;
import org.moresbycoffee.have.annotations.Then;

/**
 * Tests the JUnit annotations. ({@link org.junit.Before &#64;Before},
 * {@link org.junit.After &#64;After},
 * {@link org.junit.BeforeClass &#64;BeforeClass} and
 * {@link org.junit.AfterClass &#64;AfterClass})
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
@RunWith(MByHaveRunner.class)
@Story(files = "junitAnnotation.story")
public class JUnitAnnotationsTest {

    private static int beforeClass = 0;
    private static int before      = 0;
    private static int after       = 0;


    @BeforeClass
    public static void beforeClass() {
        beforeClass++;
    }

    @Before
    public void before() {
        before++;
    }

    @After
    public void after() {
        after++;
    }

    @AfterClass
    public static void afterClass() {
        //To test the afterClass enable the failure and the test will fail.
//        Assert.fail("AfterClass test");
    }

    @Then("before should be $num")
    public void beforeThen(final String num) {
        Assert.assertEquals(Integer.parseInt(num), before);
    }

    @Then("beforeClass should be $num")
    public void beforeClassThen(final String num) {
        Assert.assertEquals(Integer.parseInt(num), beforeClass);
    }

    @Then("after should be $num")
    public void afterThen(final String num) {
        Assert.assertEquals(Integer.parseInt(num), after);
    }




}
