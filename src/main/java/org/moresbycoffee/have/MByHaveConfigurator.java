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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.moresbycoffee.have.StepCandidate.MethodParameter;
import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.annotations.Then;
import org.moresbycoffee.have.annotations.When;
import org.moresbycoffee.have.exceptions.MByHaveException;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * Utility class to create {@link MByHaveConfiguration configuration} for {@link MByHaveRunner}.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public final class MByHaveConfigurator {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(MByHaveRunner.class.getName());

    private static final Map<Class<? extends Annotation>, StepKeyword>         KEYWORDS;

    static {
        final Map<Class<? extends Annotation>, StepKeyword> mutableKeywords = new HashMap<Class<? extends Annotation>, StepKeyword>();
        mutableKeywords.put(Given.class, new StepKeyword(Given.class, "Given"));
        mutableKeywords.put(When.class,  new StepKeyword(When.class,  "When" ));
        mutableKeywords.put(Then.class,  new StepKeyword(Then.class,  "Then" ));
        KEYWORDS = Collections.unmodifiableMap(mutableKeywords);

    }

    /**
     * Creates a {@link MByHaveConfiguration configuration} by the given testClass. This methos scans the
     * testClass for keyword annotated methods and if the parseJUnitAnnotations is true it will scan for the
     * JUnit {@link BeforeClass}, {@link Before}, {@link After} and {@link AfterClass} annotations.
     *
     * @param testClass The testclass to scan
     * @param parseJUnitAnnotations If it is <tt>true</tt> than JUnit annotated methods also will be picked up.
     * @return The configuration.
     */
    public static MByHaveConfiguration configure(final Class<?> testClass, final boolean parseJUnitAnnotations) {

        final Map<Class<? extends Annotation>, List<StepCandidate>> candidates;

        candidates = initStepCandidates(testClass);

        final List<Method> beforeClassMethods;
        final List<Method> beforeMethods;
        final List<Method> afterMethods;
        final List<Method> afterClassMethods;
        if (parseJUnitAnnotations) {
            beforeClassMethods = getAnnotatedMethods(testClass, BeforeClass.class, true );
            beforeMethods      = getAnnotatedMethods(testClass, Before.class,      false);
            afterMethods       = getAnnotatedMethods(testClass, After.class,       false);
            afterClassMethods  = getAnnotatedMethods(testClass, AfterClass.class,  true );
        } else {
            beforeClassMethods = Collections.emptyList();
            beforeMethods      = Collections.emptyList();
            afterMethods       = Collections.emptyList();
            afterClassMethods  = Collections.emptyList();
        }


        return new MByHaveConfiguration(KEYWORDS, candidates, testClass, beforeClassMethods, beforeMethods, afterMethods, afterClassMethods);
    }

    /**
     * Scans for the step candidates.
     *
     * @param testClass The test class.
     * @return The found stepcandidates in a map of keyword, stepcandidate list pairs.
     */
    private static Map<Class<? extends Annotation>, List<StepCandidate>> initStepCandidates(final Class<?> testClass) {
        final Map<Class<? extends Annotation>, List<StepCandidate>> mutableCandidates = new HashMap<Class<? extends Annotation>, List<StepCandidate>>();
        for (final Class<? extends Annotation> annotation : KEYWORDS.keySet()) {
            mutableCandidates.put(annotation, initStepCandidates(annotation, testClass));
        }
        return Collections.unmodifiableMap(mutableCandidates);
    }


    /**
     * Finds and initializes the step candidates.
     * This method looks for the <tt>annotated</tt> methods in the <tt>testClass</tt> and adds the found
     * methods as {@link StepCandidate}s to the <tt>stepCandidatesList</tt>
     *
     * @param <T> The type of the annotations
     * @param annotation The annotation the method is looking for.
     * @param testClass The class the method is scanning for annotated methods.
     * @return The list to which the annotated methods will be added as {@link StepCandidate}s.
     */
    private static <T extends Annotation> List<StepCandidate> initStepCandidates(final Class<T> annotation, final Class<?> testClass) {
        final List<StepCandidate> stepCandidatesList = new ArrayList<StepCandidate>();
        LOG.info("Init candidates");
        for (final Method method : testClass.getDeclaredMethods()) {
            final String definitionValue = getAnnotationValue(annotation, method);
            if (definitionValue != null) {
                LOG.finer("Step definition: " + definitionValue);

                /* Retrieves the method parameters. */
                final String[] params = getParameters(method);
                /* Finds the parameters in the step definition string. */
                final Map<Integer, MethodParameter> parameterPositions = findParameterPositions(params, definitionValue);

                /* Logs the method parameters. */
                if (LOG.isLoggable(Level.FINER)) {
                    for (final Map.Entry<Integer, MethodParameter> paramPos : parameterPositions.entrySet()) {
                        LOG.finer("Position: " + paramPos.getKey() + " Param: " + paramPos.getValue().getParamName());
                    }
                }
                final String regEx = createRegEx(definitionValue, Arrays.asList(params));

                LOG.finer("RegEx: " + regEx);

                stepCandidatesList.add(new StepCandidate(definitionValue, method, parameterPositions, regEx));

            }
        }
        return stepCandidatesList;
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

    private static String[] getParameters(final Method method) {
        final Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
        final String[] params = paranamer.lookupParameterNames(method, false);
        return params;
    }

    private static String createRegEx(final String stepValue, final Collection<String> paramNames) {
        String regEx = Pattern.quote(stepValue);

        for (final String paramName : paramNames) {
            final String paramPlaceHolder = getPlaceholderPattern(paramName);
            regEx = regEx.replace(paramPlaceHolder, "\\E(.*)\\Q");
        }
        return '^' + regEx + '$';
    }

    private static String getPlaceholderPattern(final String paramName) {
        return "$" + paramName;
    }

    public static Map<Integer, MethodParameter> findParameterPositions(final String[] paramNames, final String stepValue) {

        final Map<Integer, MethodParameter> result = new TreeMap<Integer, MethodParameter>();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {

                final String paramName        = paramNames[i];
                final String paramPlaceHolder = getPlaceholderPattern(paramName);
                final int    posInStepPattern = stepValue.indexOf(paramPlaceHolder);

                LOG.fine("Parameter: " + paramName);
                LOG.fine("Position:  " + posInStepPattern);

                /* Check there is only one appearance in the stepPattern. */
                if (posInStepPattern < 0) {
                    throw new MByHaveException("The pattern does not contain placeholder for the " + paramName + " parameter.");
                }
                if (stepValue.indexOf(paramPlaceHolder, posInStepPattern + paramName.length()) >= 0) {
                    throw new MByHaveException("The pattern does contain more than one placeholder for the " + paramName + " parameter");
                }

                /* Add to the map. */
                result.put(Integer.valueOf(posInStepPattern), new MethodParameter(paramName, i));
            }
        }

        return result;
    }

    private static List<Method> getAnnotatedMethods(final Class<?> testClass, final Class<? extends Annotation> annotation, final boolean isStatic) {
        final List<Method> methods = new ArrayList<Method>();
        for (final Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (isStatic != Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException("The " + annotation + " should be applied on " + (isStatic ? "static" : "non static") + " method.");
                }
                methods.add(method);
            }
        }
        return methods;
    }

    /** Hidden constructor of utiltity class. */
    private MByHaveConfigurator() {
        /* NOP */
    }

}