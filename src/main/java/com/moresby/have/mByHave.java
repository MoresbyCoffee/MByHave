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

import org.junit.runners.model.InitializationError;

import com.moresby.have.exceptions.mByHaveException;

/**
 * <p>The main class of moresBy.Have for inline usage.</p>
 * <p>This class can be instantiated in a test class and the
 * {@link com.moresby.have.annotations.Given}, {@link com.moresby.have.annotations.When}
 * and {@link com.moresby.have.annotations.Then} annotated methods. The annotations have
 * a mandatory field where the <i>pattern</i> string has to be defined. The annotated
 * methods can take <strong>parameters</strong> what has to appear in the pattern string
 * prefixed a <code>$</code>. During the behavior procession these patterns will be
 * matched to the behavior <strong>steps</strong> and the matching method will be invoked with the
 * parameters parsed from the steps.</p>
 * <p>The steps are assembled into <strong>Scenarios</strong>. A scenario
 * can be defined with the {@link #given(String)}, {@link #when(String)} and
 * {@link #then(String)} methods or as a long string combining the Given, When
 * and Then steps and passed to the {@link #runScenario(String)}.</p>
 * <p>Definition example:</p>
 * <pre>{@code
 *   @Given("first method")
 *   public void given() {
 *       ...
 *   }
 *
 *   @When("second method $parameter")
 *   public void when(final String parameter) {
 *       ...
 *   }
 *
 *   @Then("third method with $parameter1 and $parameter2 ")
 *   public void then(final String parameter1, final String parameter2) {
 *       ...
 *   }
 *
 *
 * }</pre>
 * <p>Example using {@link #given(String)}, {@link #when(String)} and
 * {@link #then(String)} methods:</p>
 * <pre>{@code
 *
 *    \@Test
 *    public void runGivenWhenThenWithMethods() {
 *        new mByHave(this).given("first method").
 *                          when("second method when parameter").
 *                          then("third parameter method first parameter and second parameter");
 *        ... assertions ...
 *
 *    }
 * }</pre>
 *
 * <p>Example using {@link #runScenario(String)}:</p>
 *
 * <pre>{@code
 *
 *    \@Test
 *    public void runGivenWhenThenWithMethods() {
 *        new mByHave(this).runScenario("Given first method\n" +
 *                                      "When second method when parameter\n" +
 *                                      "Then third parameter method first parameter and second parameter");
 *        ... assertions ...
 *
 *    }
 * }</pre>
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class mByHave {

    private final Object        testObject;
    private final mByHaveRunner runner;

    public mByHave(final Object testObject) throws mByHaveException {
        this(testObject, testObject.getClass());
    }

    private mByHave(final Object testObject, final Class<?> stepClass) throws mByHaveException {
        this.testObject = testObject;
        try {
            this.runner     = new mByHaveRunner(stepClass, false);
        } catch (final InitializationError e) {
            throw new mByHaveException("The mByHave initailization did not succeed", e);
        }
    }


    /**
     * <p>Tries to match a {@link com.moresby.have.annotations.Given} annotated
     * method by the <tt>given</tt> parameter value.</p>
     * <p>If it finds one it will invoke with the <i>parsed parameter values</i>.</p>
     * <p>If there is no matching {@link com.moresby.have.annotations.Given}
     * annotated method, an {@link com.moresby.have.exceptions.mByHaveAssertionError}
     * will occur. It will mark the test <i>failed</i>.</p>
     *
     * @param given The string which will be tried to be matched with a
     *      {@link com.moresby.have.annotations.Given} annotated method.
     * @return the invoked instance to be chainable.
     * @throws mByHaveException If any error occurs during the process.
     */
    public mByHave given(final String given) throws mByHaveException {
        runner.given(testObject, given);
        return this;
    }

    /**
     * <p>Tries to match a {@link com.moresby.have.annotations.When} annotated method
     * by the <tt>when</tt> parameter value.</p>
     * <p>If it finds one it will invoke with the <i>parsed parameter values</i>.</p>
     * <p>If there is no matching {@link com.moresby.have.annotations.When} annotated
     * method, an {@link com.moresby.have.exceptions.mByHaveAssertionError}
     * will occur. It will mark the test <i>failed</i>.</p>
     *
     * @param when The string which will be tried to be matched with a
     *      {@link com.moresby.have.annotations.When} annotated method.
     * @return the invoked instance to be chainable.
     * @throws mByHaveException If any error occurs during the process.
     */
    public mByHave when(final String when) throws mByHaveException {
        runner.when(testObject, when);
        return this;
    }

    /**
     * <p>Tries to match a {@link com.moresby.have.annotations.Then} annotated
     * method by the <tt>then</tt> parameter value.</p>
     * <p>If it finds one it will invoke with the <i>parsed parameter values</i>.</p>
     * <p>If there is no matching {@link com.moresby.have.annotations.Then}
     * annotated method, an {@link com.moresby.have.exceptions.mByHaveAssertionError}
     * will occur. It will mark the test <i>failed</i>.</p>
     *
     * @param then The string which will be tried to be matched with a
     *      {@link com.moresby.have.annotations.Then} annotated method.
     * @return the invoked instance to be chainable.
     * @throws mByHaveException If any error occurs during the process.
     */
    public mByHave then(final String then) throws mByHaveException {
        runner.then(testObject, then);
        return this;
    }

    /**
     * <p>Parses the <tt>scenario</tt> string and splits to <i>given</i>, <i>when</i> and
     * <i>then</i> steps and tries to match them to annotated method. If is matches
     * the annotated method will be invoked with the parsed parameters.</p>
     * <p>If there is no matching annotated method, an
     * {@link com.moresby.have.exceptions.mByHaveAssertionError}
     * will occur. It will mark the test <i>failed</i>.</p>
     *
     * @param scenario The scenario script
     * @throws mByHaveException If any error occurs during the process.
     */
    public void runScenario(final String scenario) throws mByHaveException {
        runner.runScenario(testObject, scenario);
    }

}
