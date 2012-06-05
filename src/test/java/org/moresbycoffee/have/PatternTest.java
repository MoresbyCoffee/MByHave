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
import org.moresbycoffee.have.exceptions.MByHaveAssertionError;


/**
 * Tests the pattern matchings. These tests are using only given methods, but
 * everything should work the same way with When and Then methods.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class PatternTest {

	private boolean somethingMethod = false;

	@Given(definition = "something test")
	public void somethingMethod() {
		this.somethingMethod = true;
	}

	@Test(expected = MByHaveAssertionError.class)
	public void prefixTest() {
		new MByHave(this).given("something");
	}

	@Test(expected = MByHaveAssertionError.class)
	public void prefixTest2() {
		new MByHave(this).given("blah blah something test");
	}

	@Test(expected = MByHaveAssertionError.class)
	public void postTest() {
		new MByHave(this).given("test");
	}

	@Test(expected = MByHaveAssertionError.class)
	public void postTest2() {
		new MByHave(this).given("something test blah blah");
	}

	@Test
	public void normalCase() {
		new MByHave(this).given("something test");
		assertTrue(somethingMethod);
	}

}
