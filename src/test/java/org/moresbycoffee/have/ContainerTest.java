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

import org.junit.runner.RunWith;
import org.moresbycoffee.have.annotations.Story;
import org.moresbycoffee.have.annotations.Then;
import org.moresbycoffee.have.annotations.When;

/**
 * Test the {@link Container} handling in the MByHave implementation.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
@RunWith(MByHaveRunner.class)
@Story(files = "containerTest.story")
public class ContainerTest {

    @When("a $text is added to a container called: $container")
    public void addToContainer(final String text, final Container<String> container) {
        container.setValue(text);
    }

    @Then("the $container container should contain the $value")
    public void checkContainer(final Container<String> container, final String value) {
        assertEquals(value, container.getValue());
    }

    final Integer testInteger = Integer.valueOf(4324);

    @When("an integer added to a container called $container")
    public void addInteger(final Container<Integer> container) {
        container.setValue(testInteger);
    }

    @Then("the $container (containing the integer) can be retrieved as number container")
    public void getNumber(final Container<Number> container){
        System.out.println("container: " + container.getValue());
        assertTrue(testInteger == container.getValue());
    }

}
