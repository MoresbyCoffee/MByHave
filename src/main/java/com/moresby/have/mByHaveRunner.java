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
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import com.moresby.have.StepCandidate.MethodParameter;
import com.moresby.have.annotations.Given;
import com.moresby.have.annotations.Story;
import com.moresby.have.annotations.Then;
import com.moresby.have.annotations.When;
import com.moresby.have.domain.Scenario;
import com.moresby.have.exceptions.mByHaveAssertionError;
import com.moresby.have.exceptions.mByHaveException;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * TODO javadoc.
 *
 * TODO line breaks
 * TODO replace {param} with $param. It's more similar to Jbehave.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class mByHaveRunner extends Runner/* ParentRunner<Scenario> */ {

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
    private final Class<?> testClass;

//> CONSTRUCTORS

    public mByHaveRunner(final Class<?> testClass) throws mByHaveException, InitializationError {
        this(testClass, true);
    }

    public mByHaveRunner(final Class<?> testClass, final boolean parseStoryFiles) throws mByHaveException, InitializationError {
//        super(testClass);

        this.testClass = testClass;
        initStepCandidates(testClass);

        if (parseStoryFiles) {
            stories = parseStories(testClass);
            if (stories.isEmpty()) {
                throw new InitializationError("No runnable test in this class.");
            }
        } else {
            stories = Collections.emptyList();
        }

    }

//> PUBLIC METHODS

    public void given(final Object testObject, final String given) throws mByHaveException {
        runStep(testObject, given, candidates.get(Given.class));
    }

    public void when(final Object testObject, final String when) throws mByHaveException {
        runStep(testObject, when, candidates.get(When.class));
    }

    public void then(final Object testObject, final String then) throws mByHaveException {
        runStep(testObject, then, candidates.get(Then.class));
    }

    public void runScenario(final Object testObject, final String scenario) throws mByHaveException {

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

    public void runScenario(final Object testObject, final InputStream scenarioIs) throws IOException, mByHaveException {
        final Scenario scenario = parseScenario(scenarioIs);
        processScenario(testObject, scenario);
    }

//> PRIVATE METHODS

    /**
     * Loads a resource from the jar. The resource can be in the <tt>root</tt> or in the package of the testClass.
     *
     * @param storyFile The name of the story file.
     * @param testClass The testClass.
     * @return The InputStream of the story file.
     * @throws mByHaveException If the file is not found.
     */
    private static InputStream loadResource(final String storyFile, final Class<?> testClass) throws mByHaveException {

        {
            final InputStream storyIs = testClass.getClassLoader().getResourceAsStream(storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }
        {
            final InputStream storyIs = mByHaveRunner.class.getClassLoader().getResourceAsStream(storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }
        {
            final InputStream storyIs = ClassLoader.getSystemResourceAsStream(storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }
        final String packageName = testClass.getPackage().getName().replace('.', '/');
        {
            final InputStream storyIs = testClass.getClassLoader().getResourceAsStream(packageName + "/" + storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }
        {
            final InputStream storyIs = mByHaveRunner.class.getClassLoader().getResourceAsStream(packageName + "/" + storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }
        {
            final InputStream storyIs = ClassLoader.getSystemResourceAsStream(packageName + "/" + storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }

        throw new mByHaveException("The story file is not found. " + storyFile);

    }

    private static List<com.moresby.have.domain.Story> parseStories(final Class<?> testClass) throws mByHaveException {
        final List<com.moresby.have.domain.Story> mutableStories = new ArrayList<com.moresby.have.domain.Story>();
        if (testClass.isAnnotationPresent(Story.class)) {
            final Story story = testClass.getAnnotation(Story.class);
            final String[] storyFiles = story.files();

            for (final String storyFile : storyFiles) {

                final InputStream storyIs = loadResource(storyFile, testClass);
                com.moresby.have.domain.Story storyObject;
                try {
                    storyObject = parseStory(storyFile, storyIs);
                } catch (final IOException e) {
                    throw new mByHaveException("The story file is not readable. " + storyFile, e);
                } finally {
                    try {
                        storyIs.close();
                    } catch (final IOException e) {
                        //TODO something
                    }
                }
                mutableStories.add(storyObject);
            }
        }
        return Collections.unmodifiableList(mutableStories);
    }

    private static com.moresby.have.domain.Story parseStory(final String storyName, final InputStream storyIs) throws mByHaveException, IOException {
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
        return new com.moresby.have.domain.Story(storyName, scenarios);
    }

    private static Scenario parseScenario(final String scenario) throws mByHaveException {

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

    private static Scenario parseScenario(final InputStream scenario) throws mByHaveException, IOException {
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
                    scenarioDescription = "TODO"; //TODO
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

    public static Map<Integer, MethodParameter> findParameterPositions(final String[] paramNames, final String stepValue) {

        final Map<Integer, MethodParameter> result = new TreeMap<Integer, MethodParameter>();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {

                final String paramName        = paramNames[i];
                final String paramPlaceHolder = getPlaceholderPattern(paramName);
                final int    posInStepPattern = stepValue.indexOf(paramPlaceHolder);

                System.out.println("Parameter: " + paramName);
                System.out.println("Position:  " + posInStepPattern);

                /* Check there is only one appearance in the stepPattern. */
                if (posInStepPattern < 0) {
                    throw new mByHaveException("The pattern does not contain placeholder for the " + paramName + " parameter.");
                }
                if (stepValue.indexOf(paramPlaceHolder, posInStepPattern + paramName.length()) >= 0) {
                    throw new mByHaveException("The pattern does contain more than one placeholder for the " + paramName + " parameter");
                }

                /* Add to the map. */
                result.put(Integer.valueOf(posInStepPattern), new MethodParameter(paramName, i));
            }
        }

        return result;
    }

    private static String getPlaceholderPattern(final String paramName) {
        return "$" + paramName;
    }

    private static String createRegEx(final String stepValue, final Collection<String> paramNames) {
        String regEx = stepValue;
        for (final String paramName : paramNames) {
            final String paramPlaceHolder = getPlaceholderPattern(paramName);
            regEx = regEx.replace(paramPlaceHolder, "(.*)");
        }
        return regEx;
    }

    private void runCandidate(final Object testObject, final StepCandidate candidate, final Matcher matcher, final String step) throws mByHaveException {
        final Map<Integer, MethodParameter> positions = candidate.getParameterPositions();
        int i = 1;

        final SortedMap<Integer, String> methodParameters = new TreeMap<Integer, String>();
        for (final MethodParameter param : positions.values()) {
            final int starts = matcher.start(i);
            final int ends   = matcher.end(i);
//            final String paramValue = matcher.group(i++);
            final String paramValue = step.substring(starts, ends);
            System.out.println("Param Value: " + paramValue + " Group: " + matcher.group(i));
            i++;

            methodParameters.put(Integer.valueOf(param.getParamPos()), paramValue);
            System.out.println("Parameter name: " + param.getParamName() + " Value: " + paramValue);
        }

        System.out.println("Params: " + candidate.getMethod().getParameterTypes().length);

        try {
            candidate.getMethod().invoke(testObject, methodParameters.values().toArray());
        } catch (final IllegalArgumentException e) {
            throw new mByHaveException("The parameters could not be matched.", e);
        } catch (final IllegalAccessException e) {
            throw new mByHaveException("The annotatated method should be public.", e);
        } catch (final InvocationTargetException e) {
            if (e.getTargetException() instanceof AssertionError) {
                throw (AssertionError) e.getTargetException();
            }
            throw new mByHaveException(e);
        }

    }

//    private Integer[] lineBreakPositions(final String string) {
//        final ArrayList<Integer> breakPositions = new ArrayList<Integer>();
//        for (int index = string.indexOf('\n'); index >= 0; index = string.indexOf('\n', index + 1)) {
//            breakPositions.add(Integer.valueOf(index));
//        }
//        return breakPositions.toArray(new Integer[] {});
//    }

    private void runStep(final Object testObject, final String step, final Collection<StepCandidate> stepCandidates) throws mByHaveException {
        boolean found = false;
        for (final StepCandidate candidate : stepCandidates) {

//            final Integer[] breakPositions = lineBreakPositions(step);
            final Matcher matcher = candidate.getPattern().matcher(step.replace('\n', ' '));
            if (matcher.find()) {
                found = true;
                System.out.println("FOUND! " + candidate.getValue());
                runCandidate(testObject, candidate, matcher, step);
                break;
            }
        }
        if (!found) {
            throw new mByHaveAssertionError("No maching step to the \"" + step + "\" step definition.");
        }

    }

    private void processScenario(final Object testObject, final Scenario scenario) throws mByHaveException {
        System.out.println("Description: " + scenario.getDescription());
        for (final String step : scenario.getSteps()) {
            processStep(testObject, step);
        }
    }

    private void processStep(final Object testObject, final String step) throws mByHaveException {
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

    public static class StoryDescription {

        private final String name;
        private final Description description;
        private final List<ScenarioDescription> scenarios;

        /**
         * @param name
         * @param description
         * @param scenarios
         */
        public StoryDescription(String name, Description description, List<ScenarioDescription> scenarios) {
            super();
            this.name = name;
            this.description = description;
            this.scenarios = scenarios;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the description
         */
        public Description getDescription() {
            return description;
        }

        /**
         * @return the scenarios
         */
        public List<ScenarioDescription> getScenarios() {
            return scenarios;
        }

    }


    public static class ScenarioDescription {

        private final String name;
        private final Description description;
        private final List<StepDescription> steps;
        /**
         * @param name
         * @param description
         * @param scenarios
         */
        public ScenarioDescription(String name, Description description, List<StepDescription> steps) {
            super();
            this.name = name;
            this.description = description;
            this.steps = steps;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the description
         */
        public Description getDescription() {
            return description;
        }

        /**
         * @return the steps
         */
        public List<StepDescription> getSteps() {
            return steps;
        }


    }


    public static class StepDescription {

        private final String step;
        private final Description description;

        /**
         * @param step
         * @param description
         */
        public StepDescription(String step, Description description) {
            super();
            this.step = step;
            this.description = description;
        }

        /**
         * @return the step
         */
        public String getStep() {
            return step;
        }

        /**
         * @return the description
         */
        public Description getDescription() {
            return description;
        }



    }

    private Description            mainDescription;
    private List<StoryDescription> storyDescriptions = Collections.emptyList();
    /** {@inheritDoc} */
    @Override
    public Description getDescription() {
        if (mainDescription != null) {
            return mainDescription;
        }

        int storyIndex = 0;
        storyDescriptions = new ArrayList<StoryDescription>();
        mainDescription = Description.createSuiteDescription("Story file tests");
        for (final com.moresby.have.domain.Story story : stories) {

            final Description storyDescription = Description.createSuiteDescription(++storyIndex + ". " + story.getName().replace("\n", " "));
            final List<ScenarioDescription> scenarioDescriptions = new ArrayList<ScenarioDescription>();

            int scenarioIndex = 0;
            for (final Scenario scenario : story.getScenario()) {
                final Description scenarioDescription = Description.createSuiteDescription(storyIndex + "." + (++scenarioIndex) + ". " + scenario.getDescription().replace("\n", " "));
                final List<StepDescription> stepDescriptions = new ArrayList<StepDescription>();
                int stepIndex = 0;
                for (final String step : scenario.getSteps()) {
                    final Description stepDescription = Description.createTestDescription(testClass, storyIndex + "." + scenarioIndex + "." + (++stepIndex) + ". " + step.replace("\n", " "));
                    stepDescriptions.add(new StepDescription(step, stepDescription));
                    scenarioDescription.addChild(stepDescription);
                    System.out.println("Add step: " + stepDescription);
                }

                scenarioDescriptions.add(new ScenarioDescription(scenario.getDescription(), scenarioDescription, stepDescriptions));
                storyDescription.addChild(scenarioDescription);
            }

            storyDescriptions.add(new StoryDescription(story.getName(), storyDescription, scenarioDescriptions));
            mainDescription.addChild(storyDescription);
        }
        return mainDescription;
    }

    /** {@inheritDoc} */
    @Override
    public void run(final RunNotifier notifier) {
        notifier.fireTestStarted(mainDescription);
        try {
            for (final StoryDescription storyDescription : storyDescriptions) {
                notifier.fireTestStarted(storyDescription.getDescription());
                for (final ScenarioDescription scenarioDescription : storyDescription.getScenarios()) {
                    notifier.fireTestStarted(scenarioDescription.getDescription());

                    final Object testObject = testClass.newInstance();

                    for (final StepDescription stepDescription : scenarioDescription.getSteps()) {
                        notifier.fireTestStarted(stepDescription.getDescription());

                        try {
                            System.out.println("Process step: " + " scenarion : " + scenarioDescription.getName() + " step: " + stepDescription.getStep());
                            processStep(testObject, stepDescription.getStep());
                        } catch (final Throwable t) {
                            notifier.fireTestFailure(new Failure(stepDescription.getDescription(), t));
                            break;
                        }

                        notifier.fireTestFinished(stepDescription.getDescription());
                    }
                    notifier.fireTestFinished(scenarioDescription.getDescription());
                }
                notifier.fireTestFinished(storyDescription.getDescription());
            }
        } catch (final Throwable t) {
            notifier.fireTestFailure(new Failure(mainDescription, t));
        }
        notifier.fireTestFinished(mainDescription);
    }




}
