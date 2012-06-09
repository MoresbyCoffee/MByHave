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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Representes a MByHave test configuration. A configuration consists of the
 * the {@link StepCandidate candidate}s (methods annotated with keywords),
 * the {@link StepKeyword keyword}s, the test class and if it is necessary it also
 * contains the list of the {@link org.junit.Before}, {@link org.junit.BeforeClass},
 * {@link org.junit.After} and {@link org.junit.AfterClass} annotated methods.
 * The JUnit annotated methods has to be stored when the MByHave is used as a
 * JUnitRunner.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class MByHaveConfiguration {

    private final Map<Class<? extends Annotation>, StepKeyword>         keywords;
    private final Map<Class<? extends Annotation>, List<StepCandidate>> candidates;

    private final Class<?> testClass;

    private final List<Method> beforeClassMethods;
    private final List<Method> beforeMethods;
    private final List<Method> afterMethods;
    private final List<Method> afterClassMethods;
    /**
     * @param keywords The keywords used as step definitions and descriptions. (NonNull)
     * @param candidates The step candidates to be matched to the step descriptios. (NonNull)
     * @param testClass The test class. (NonNull)
     * @param beforeClassMethods The list of the {@link org.junit.BeforeClass} annotated methods in the test class. (NonNull)
     * @param beforeMethods The list of the {@link org.junit.Before} annotated methods in the test class. (NonNull)
     * @param afterMethods The list of the {@link org.junit.After} annotated methods in the test class. (NonNull)
     * @param afterClassMethods The list of the {@link org.junit.AfterClass} annotated methods in the test class. (NonNull)
     */
    public MByHaveConfiguration(final Map<Class<? extends Annotation>, StepKeyword> keywords,
                                final Map<Class<? extends Annotation>, List<StepCandidate>> candidates,
                                final Class<?> testClass,
                                final List<Method> beforeClassMethods,
                                final List<Method> beforeMethods,
                                final List<Method> afterMethods,
                                final List<Method> afterClassMethods) {
        this.keywords           = keywords;
        this.candidates         = candidates;
        this.testClass          = testClass;
        this.beforeClassMethods = beforeClassMethods;
        this.beforeMethods      = beforeMethods;
        this.afterMethods       = afterMethods;
        this.afterClassMethods  = afterClassMethods;
    }

    /**
     * @return The keywords used as step definitions and descriptions. (NonNull)
     */
    public Map<Class<? extends Annotation>, StepKeyword> getKeywords() {
        return keywords;
    }

    /**
     * @return The step candidates to be matched to the step descriptios. (NonNull)
     */
    public Map<Class<? extends Annotation>, List<StepCandidate>> getCandidates() {
        return candidates;
    }

    /**
     * @return The test class. (NonNull)
     */
    public Class<?> getTestClass() {
        return testClass;
    }
    /**
     * @return The list of the {@link org.junit.BeforeClass} annotated methods in the test class. (NonNull)
     */
    public List<Method> getBeforeClassMethods() {
        return beforeClassMethods;
    }

    /**
     * @return The list of the {@link org.junit.Before} annotated methods in the test class. (NonNull)
     */
    public List<Method> getBeforeMethods() {
        return beforeMethods;
    }

    /**
     * @return The list of the {@link org.junit.After} annotated methods in the test class. (NonNull)
     */
    public List<Method> getAfterMethods() {
        return afterMethods;
    }

    /**
     * @return The list of the {@link org.junit.AfterClass} annotated methods in the test class. (NonNull)
     */
    public List<Method> getAfterClassMethods() {
        return afterClassMethods;
    }

}
