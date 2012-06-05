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

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.moresbycoffee.have.MByHaveRunner;
import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.annotations.Story;
import org.moresbycoffee.have.annotations.Then;
import org.moresbycoffee.have.annotations.When;


/**
 * TODO javadoc.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
@RunWith(MByHaveRunner.class)
@Story(files = "storytest1.story")
public class Story1Test {
	
	/** Logger. */
	private static final Logger LOG = Logger.getLogger(Story1Test.class.getName());

    @Test
    public void test() {
        fail("This test should not run.");
    }

    @Given(definition = "first method")
    public void firstMethod() {
        LOG.info("First out");
    }

    @When(definition = "second method $param")
    public void whenTestMethod(final String param) {
    	LOG.info("When test method: " + param);
    }

    @Then(definition = "third $param1 $param2 method")
    public void thenTwoParamMethod(final String param1, final String param2) {
    	LOG.info("Then test param1: " + param1 + " param2: " + param2);
    }
}