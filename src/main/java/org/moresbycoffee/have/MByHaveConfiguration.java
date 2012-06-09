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
 * TODO javadoc.
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
     * @param keywords
     * @param candidates
     * @param stories
     * @param testClass
     * @param beforeClassMethods
     * @param beforeMethods
     * @param afterMethods
     * @param afterClassMethods
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
     * @return the keywords
     */
    public Map<Class<? extends Annotation>, StepKeyword> getKeywords() {
        return keywords;
    }
    /**
     * @return the candidates
     */
    public Map<Class<? extends Annotation>, List<StepCandidate>> getCandidates() {
        return candidates;
    }
    /**
     * @return the testClass
     */
    public Class<?> getTestClass() {
        return testClass;
    }
    /**
     * @return the beforeClassMethods
     */
    public List<Method> getBeforeClassMethods() {
        return beforeClassMethods;
    }
    /**
     * @return the beforeMethods
     */
    public List<Method> getBeforeMethods() {
        return beforeMethods;
    }
    /**
     * @return the afterMethods
     */
    public List<Method> getAfterMethods() {
        return afterMethods;
    }
    /**
     * @return the afterClassMethods
     */
    public List<Method> getAfterClassMethods() {
        return afterClassMethods;
    }


}
