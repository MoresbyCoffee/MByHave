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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import com.moresby.have.annotations.Given;

/**
 * TODO javadoc.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class mByHave {

    public static class StepCandidate {

        private final String value;
        private final int    priority;
        private final Method method;


        /**
         * @param value
         * @param priority
         */
        public StepCandidate(final String value, final int priority, final Method method) {
            this.value    = value;
            this.priority = priority;
            this.method   = method;
        }

        public String getValue() {
            return value;
        }

        public int getPriority() {
            return priority;
        }

        public Method getMethod() {
            return method;
        }





    }

    private final Collection<StepCandidate> givenCandidates = new ArrayList<StepCandidate>();


    public void createPattern(final Method method, final String stepValue) {
        System.out.println("Paranamer");
        final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
        final String[] params = paranamer.lookupParameterNames(method, false);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                final String param = params[i];
                System.out.println("param: " + param);

            }
        }



    }

    public <T> mByHave(final Object testObject) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        final Class<?> testClass = testObject.getClass();

        for (final Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Given.class)) {
                final Given given = method.getAnnotation(Given.class);
                System.out.println("Given: " + given.value());
                givenCandidates.add(new StepCandidate(given.value(), given.priority(), method));
                createPattern(method, given.value());
            }
        }

//        for (final StepCandidate stepCandidate : givenCandidates) {
//            stepCandidate.getMethod().invoke(testObject);
//        }
    }

    public mByHave given(final String given) {


        return this;
    }

    public mByHave when(final String when) {
        return this;
    }

    public mByHave then(final String then) {
        return this;
    }

    public void run() {

    }
}
