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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import com.moresby.have.StepCandidate.MethodParameter;
import com.moresby.have.annotations.Given;
import com.moresby.have.annotations.Then;
import com.moresby.have.annotations.When;

/**
 * TODO javadoc.
 *
 * TODO exception handling. Hide the inside behavior, concentrate on the real problem.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class mByHaveRunner extends BlockJUnit4ClassRunner {

    private final Map<Class<? extends Annotation>, StepKeyword>         keywords;
    private final Map<Class<? extends Annotation>, List<StepCandidate>> candidates;
    {
        final Map<Class<? extends Annotation>, StepKeyword> mutableKeywords = new HashMap<Class<? extends Annotation>, StepKeyword>();
        mutableKeywords.put(Given.class, new StepKeyword(Given.class, "Given"));
        mutableKeywords.put(When.class,  new StepKeyword(When.class,  "When" ));
        mutableKeywords.put(Then.class,  new StepKeyword(Then.class,  "Then" ));
        keywords = Collections.unmodifiableMap(mutableKeywords);

        final Map<Class<? extends Annotation>, List<StepCandidate>> mutableCandidates = new HashMap<Class<? extends Annotation>, List<StepCandidate>>();
        for (final Class<? extends Annotation> annotation : keywords.keySet()) {
            mutableCandidates.put(annotation, new ArrayList<StepCandidate>());
        }

        candidates = Collections.unmodifiableMap(mutableCandidates);
    }

    /**
     * @param testClass
     * @throws InitializationError
     */
    public mByHaveRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
        initStepCandidates(testClass);
    }

    private void initStepCandidates(final Class<?> testClass) {
        for (final Map.Entry<Class<? extends Annotation>, List<StepCandidate>> candidate : candidates.entrySet()) {
            initStepCandidates(candidate.getKey(), testClass, candidate.getValue());
        }
    }

    private static <T extends Annotation> String getAnnotationValue(final Class<T> annotation, final Method method) {
        if (annotation == Given.class) {
            if (method.isAnnotationPresent(Given.class)) {
                final Given given = method.getAnnotation(Given.class);
                return given.value();
            }
        } else if (annotation == When.class) {
            if (method.isAnnotationPresent(When.class)) {
                final When when = method.getAnnotation(When.class);
                return when.value();
            }
        } else if (annotation == Then.class) {
            if (method.isAnnotationPresent(Then.class)) {
                final Then then = method.getAnnotation(Then.class);
                return then.value();
            }
        }
        return null;
    }


    private static <T extends Annotation> void initStepCandidates(final Class<T> annotation, final Class<?> testClass, final List<StepCandidate> stepCandidatesList) {
        for (final Method method : testClass.getDeclaredMethods()) {
            final String definitionValue = getAnnotationValue(annotation, method);
            if (definitionValue != null) {
                System.out.println("Given: " + definitionValue);

                final String[] params = getParameters(method);
                final Map<Integer, MethodParameter> parameterPositions = findParameterPositions(params, definitionValue);


                for(final Map.Entry<Integer, MethodParameter> paramPos : parameterPositions.entrySet()) {
                    System.out.println("Position: " + paramPos.getKey() + " Param: " + paramPos.getValue().getParamName());
                }
                final String regEx = createRegEx(definitionValue, Arrays.asList(params));

                System.out.println("RegEx: " + regEx);

                stepCandidatesList.add(new StepCandidate(definitionValue, method, parameterPositions, regEx));

            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Description getDescription() {
        final Description description = super.getDescription();
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);
    }



    private static String[] getParameters(final Method method) {
        System.out.println("Paranamer");
        final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
        final String[] params = paranamer.lookupParameterNames(method, false);
        return params;
    }

    public static Map<Integer, MethodParameter> findParameterPositions(final String[] params, final String stepValue) {

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

    public static String createRegEx(final String stepValue, final Collection<String> params) {
        String regEx = stepValue;
        for (final String param : params) {
            final String paramPlaceHolder = "{" + param + "}";
            regEx = regEx.replace(paramPlaceHolder, "(.*)");
        }
        return regEx;
    }

    private void runCandidate(final Object testObject, final StepCandidate candidate, final Matcher matcher) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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

    private void runStep(final Object testObject, final String step, final Collection<StepCandidate> stepCandidates) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        boolean found = false;
        for (final StepCandidate candidate : stepCandidates) {
            final Matcher matcher = candidate.getPattern().matcher(step);
            if (matcher.find()) {
                found = true;
                System.out.println("FOUND! " + candidate.getValue());
                runCandidate(testObject, candidate, matcher);
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("No maching step");
        }

    }

    public void given(final Object testObject, final String given) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        runStep(testObject, given, candidates.get(Given.class));
    }

    public void when(final Object testObject, final String when) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        runStep(testObject, when, candidates.get(When.class));
    }

    public void then(final Object testObject, final String then) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        runStep(testObject, then, candidates.get(Then.class));
    }

    public void runScenario(final Object testObject, final String scenario) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        try {
            final byte[] bytes = scenario.getBytes("UTF-8");
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try {
                runScenario(testObject, inputStream);
            } finally {
                inputStream.close();
            }
        } catch (final IOException e) {
            //TODO should not happen.
            throw new RuntimeException(e);
        }

    }

    public void runScenario(final Object testObject, final InputStream scenario) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final InputStreamReader isReader = new InputStreamReader(scenario);
        final BufferedReader    reader   = new BufferedReader(isReader);

        String line = null;
        StringBuilder stepBuilder = null;
        while((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then")) {
                if (stepBuilder != null) {
                    processStep(testObject, stepBuilder.toString());
                }
                stepBuilder = new StringBuilder(line);
            } else {
                stepBuilder.append(line);
            }
        }
        if (stepBuilder != null) {
            processStep(testObject, stepBuilder.toString());
        }
    }

    public void processStep(final Object testObject, final String step) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        System.out.println("Process step: " + step);
        //TODO get rid of the keyword!
        for (final StepKeyword keyword : keywords.values()) {
            if (step.startsWith(keyword.getKeyword())) {
                runStep(testObject, step, candidates.get(Given.class));
                return;
            }
        }
        throw new IllegalArgumentException(); //TODO

    }

}
