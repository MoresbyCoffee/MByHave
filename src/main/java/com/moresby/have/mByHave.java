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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

import org.junit.runner.RunWith;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import com.moresby.have.StepCandidate.MethodParameter;
import com.moresby.have.annotations.Given;

/**
 * TODO javadoc.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
@RunWith(mByHaveRunner.class)
public class mByHave {

    private final Collection<StepCandidate> givenCandidates = new ArrayList<StepCandidate>();
    private final Collection<StepCandidate> whenCandidates = new ArrayList<StepCandidate>();
    private final Collection<StepCandidate> thenCandidates = new ArrayList<StepCandidate>();



    public String[] getParameters(final Method method) {
        System.out.println("Paranamer");
        final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
        final String[] params = paranamer.lookupParameterNames(method, false);
        return params;
    }

    public Map<Integer, MethodParameter> findParameterPositions(final String[] params, final String stepValue) {

        final Map<Integer, MethodParameter> result = new TreeMap<Integer, MethodParameter>();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                final String param = params[i];
                System.out.println("param: " + param);
                final String paramPlaceHolder = "{" + param + "}";
                final int position = stepValue.indexOf(paramPlaceHolder);
                System.out.println("Position: " + position);
                if (position < 0) {
//                    continue;
                    throw new IllegalArgumentException(); //TODO other exception
                }
                if (stepValue.indexOf(paramPlaceHolder, position + param.length()) >= 0) {
                    throw new IllegalArgumentException(); //TODO Too many parameters.
                }
                result.put(Integer.valueOf(position), new MethodParameter(param, i));
            }
        }
        return result;
    }

    public String createRegEx(final String stepValue, final Collection<String> params) {
        String regEx = stepValue;
        for (final String param : params) {
            final String paramPlaceHolder = "{" + param + "}";
            regEx = regEx.replace(paramPlaceHolder, "(.*)");
        }
        return regEx;
    }

    private final Object testObject;

    public <T> mByHave(final T testObject) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        this.testObject = testObject;

        final Class<?> testClass = testObject.getClass();

        for (final Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Given.class)) {
                final Given given = method.getAnnotation(Given.class);
                System.out.println("Given: " + given.value());

                final String[] params = getParameters(method);
                final Map<Integer, MethodParameter> parameterPositions = findParameterPositions(params, given.value());


                for(final Map.Entry<Integer, MethodParameter> paramPos : parameterPositions.entrySet()) {
                    System.out.println("Position: " + paramPos.getKey() + " Param: " + paramPos.getValue().getParamName());
                }
                final String regEx = createRegEx(given.value(), Arrays.asList(params));

                System.out.println("RegEx: " + regEx);

                givenCandidates.add(new StepCandidate(given.value(), given.priority(), method, parameterPositions, regEx));

            }
        }
    }

    private void runCandidate(final StepCandidate candidate, final Matcher matcher) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Map<Integer, MethodParameter> positions = candidate.getParameterPositions();
        int i = 1;

        final SortedMap<Integer, String> methodParameters = new TreeMap<Integer, String>();
        for (final MethodParameter param : positions.values()) {
            final String paramValue = matcher.group(i++);
            methodParameters.put(param.getParamPos(), paramValue);
            System.out.println("Parameter name: " + param.getParamName() + " Value: " + paramValue);
        }

        System.out.println("Params: " + candidate.getMethod().getParameterTypes().length);

        candidate.getMethod().invoke(testObject, methodParameters.values().toArray());

    }

    private void runStep(final String step, final Collection<StepCandidate> stepCandidates) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean found = false;
        for (final StepCandidate candidate : stepCandidates) {
            final Matcher matcher = candidate.getPattern().matcher(step);
            if (matcher.find()) {
                found = true;
                System.out.println("FOUND! " + candidate.getValue());
                runCandidate(candidate, matcher);
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("No maching step");
        }

    }

    public mByHave given(final String given) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        runStep(given, givenCandidates);
        return this;
    }

    public mByHave when(final String when) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        runStep(when, whenCandidates);
        return this;
    }

    public mByHave then(final String then) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        runStep(then, thenCandidates);
        return this;
    }

    public void run() {

    }
}
