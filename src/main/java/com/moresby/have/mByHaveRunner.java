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

import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import com.moresby.have.StepCandidate.MethodParameter;
import com.moresby.have.annotations.Given;
import com.moresby.have.annotations.Story;
import com.moresby.have.annotations.Then;
import com.moresby.have.annotations.When;
import com.moresby.have.domain.Scenario;
import com.moresby.have.exceptions.mByHaveAssertionError;

/**
 * TODO javadoc.
 *
 * TODO exception handling. Hide the inside behavior, concentrate on the real problem.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class mByHaveRunner extends Runner {

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

    private final List<com.moresby.have.domain.Story> stories;
    private final Runner parentRunner;
    private final Class<?> testClass;

//> CONSTRUCTORS

    public mByHaveRunner(final Class<?> testClass, final boolean parseFile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InitializationError {
        this.testClass = testClass;
        initStepCandidates(testClass);

        try {
            if (parseFile) {
                Runner runner;
                try {
                    runner = new JUnit4ClassRunner(testClass); //TODO find the runner
                } catch (final Exception e) {
                    runner = null;
                }
                this.parentRunner = runner;
                stories = parseStories(testClass);
                if (parentRunner == null && stories.isEmpty()) {
                    throw new InitializationError("No runnable test in this class.");
                }
            } else {
                stories = Collections.emptyList();
                parentRunner = null;
            }

        } catch (final IOException e) {
            throw new InitializationError(e); //TODO
        }

    }

    /**
     * @param testClass
     * @throws InitializationError
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public mByHaveRunner(final Class<?> testClass) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InitializationError {
        this(testClass, true);
    }

//> PUBLIC METHODS

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

    public void runScenario(final Object testObject, final InputStream scenarioIs) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Scenario scenario = parseScenario(scenarioIs);
        processScenario(testObject, scenario);
    }

//> PRIVATE METHODS

    private static List<com.moresby.have.domain.Story> parseStories(final Class<?> testClass) throws IllegalArgumentException, IOException, IllegalAccessException, InvocationTargetException {
        final List<com.moresby.have.domain.Story> mutableStories = new ArrayList<com.moresby.have.domain.Story>();
        if (testClass.isAnnotationPresent(Story.class)) {
            final Story story = testClass.getAnnotation(Story.class);
            final String[] storyFiles = story.files();

            for (final String storyFile : storyFiles) {
                System.out.println("Found storyFile: " + storyFile + " " + testClass.getPackage().getName());
                InputStream storyIs = testClass.getClassLoader().getResourceAsStream("com/moresby/have/" + storyFile);
                if (storyIs == null) {
                    storyIs = ClassLoader.getSystemResourceAsStream(storyFile);
                }
                if (storyIs == null) {
                    throw new NullPointerException();
                }
                final com.moresby.have.domain.Story storyObject = parseStory(storyIs);
                mutableStories.add(storyObject);
            }
        }
        return Collections.unmodifiableList(mutableStories);
    }

    private static com.moresby.have.domain.Story parseStory(final InputStream storyIs) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final InputStreamReader isReader = new InputStreamReader(storyIs);
        final BufferedReader    reader   = new BufferedReader(isReader);

        final List<Scenario> scenarios = new ArrayList<Scenario>();

        String line = null;
        StringBuilder storyBuilder = null;
        while((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("Scenario")) {
                if (storyBuilder != null) {
                    scenarios.add(parseScenario(storyBuilder.toString()));
                }
                storyBuilder = new StringBuilder();
            } else if (storyBuilder == null && (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then"))) {
                storyBuilder = new StringBuilder();
            }
            if (storyBuilder != null) {
                storyBuilder.append(line).append("\n");
            }
        }
        if (storyBuilder != null) {
            scenarios.add(parseScenario(storyBuilder.toString()));
        }
        return new com.moresby.have.domain.Story("Story name", scenarios);
    }

    private static Scenario parseScenario(final String scenario) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        try {
            final byte[] bytes = scenario.getBytes("UTF-8");
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try {
                return parseScenario(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (final IOException e) {
            //TODO should not happen.
            throw new RuntimeException(e);
        }

    }

    private static Scenario parseScenario(final InputStream scenario) throws IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final InputStreamReader isReader = new InputStreamReader(scenario);
        final BufferedReader    reader   = new BufferedReader(isReader);

        String scenarioDescription = null;
        final List<String> steps = new ArrayList<String>();

        String line = null;
        StringBuilder scenarioBuilder = null;
        boolean parseDescription = false;
        while((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("Scenario")) {
                if (scenarioBuilder == null ) {
                    scenarioBuilder = new StringBuilder(line);
                    parseDescription = true;
                } else {
                    throw new IllegalArgumentException("This scenario contains two scenario descriptions.");
                }
            } else if (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then")) {
                if (parseDescription) {
                    scenarioDescription = scenarioBuilder.toString();
                    scenarioBuilder = new StringBuilder(line);
                    parseDescription = false;
                } else if (scenarioBuilder != null) {
                    steps.add(scenarioBuilder.toString());
                    scenarioBuilder = new StringBuilder(line);
                } else {
                    scenarioDescription = "TODO";
                    scenarioBuilder = new StringBuilder(line);
                }
            } else if (scenarioBuilder != null) {
                scenarioBuilder.append(line + "\n");
            }
        }
        if (scenarioBuilder != null) {
            if (parseDescription) {
                throw new IllegalArgumentException("The scenario description does not contain any step description");
            }
            steps.add(scenarioBuilder.toString());
        }
        return new Scenario(scenarioDescription, steps);
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



    private static String[] getParameters(final Method method) {
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

    private static String createRegEx(final String stepValue, final Collection<String> params) {
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
            throw new mByHaveAssertionError("No maching step to the \"" + step + "\" step definition.");
        }

    }

    private void processScenario(final Object testObject, final Scenario scenario) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        System.out.println("Description: " + scenario.getDescription());
        for (final String step : scenario.getSteps()) {
            processStep(testObject, step);
        }
    }

    private void processStep(final Object testObject, final String step) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        System.out.println("Process step: " + step);
        //TODO get rid of the keyword!
        for (final StepKeyword keyword : keywords.values()) {
            if (step.startsWith(keyword.getKeyword())) {
                runStep(testObject, step, candidates.get(keyword.getAnnotation()));
                return;
            }
        }
        throw new IllegalArgumentException(); //TODO

    }

//> JUNIT RUNNER

    /** {@inheritDoc} */
    @Override
    public Description getDescription() {

        final Description mByHaveSuite = Description.createSuiteDescription("Story file tests");
        for (final com.moresby.have.domain.Story story : stories) {
            final Description storyDescription = Description.createTestDescription(testClass, story.getName());
            for (final Scenario scenario : story.getScenario()) {
                storyDescription.addChild(Description.createTestDescription(testClass, scenario.getDescription()));
            }
            mByHaveSuite.addChild(storyDescription);
        }

        if (parentRunner == null) {
            return mByHaveSuite;
        } else {
            final Description parentDescription = parentRunner.getDescription();
            parentDescription.addChild(mByHaveSuite);
            return parentDescription;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run(final RunNotifier notifier) {
        if (parentRunner != null) {
            parentRunner.run(notifier);
        }


    }

}
