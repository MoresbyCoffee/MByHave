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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import com.moresby.have.StepCandidate.MethodParameter;
import com.moresby.have.annotations.Given;
import com.moresby.have.annotations.Story;
import com.moresby.have.annotations.Then;
import com.moresby.have.annotations.When;
import com.moresby.have.domain.Scenario;
import com.moresby.have.exceptions.MByHaveAssertionError;
import com.moresby.have.exceptions.MByHaveException;

/**
 * <p>A JUnit {@link Runner} implementation designed to run the mBy.Have story files.</p>
 * <p>The mBy.Have <strong>story</strong> files consist of <strong>scenarios</strong>.
 * Each scenario describes a test case and for each of them a new test object will be
 * instantiated. A scenario can contain one or more <p>steps</p>. The steps contains the
 * real logic of the test and they have three form: <strong>given</strong>,
 * <strong>when</strong> and <strong>then</strong>. These step types ought to be used
 * in this order (given, when, then) but there is not strict restriction. The behavior of
 * them also the same only the Given, When, Then keywords are different.</p>
 * <p>The steps in a scenario will be parsed and tired to be matched to an annotated
 * method from the test class. There is one-one annotation for each step type:</p>
 * <ul>
 * <li>{@link Given} for the given steps,</li>
 * <li>{@link When} for the when steps and</li>
 * <li>{@link Then} for the then steps.</li>
 * </ul>
 * <p>Each annotation takes a value, which is a pattern by which the steps will be tried
 * being matched.  ... parameter placeholder .....</p>
 * ... relation between annotation value and step - parameter matching - greedy ....
 * ... how to use ...
 * ... example ...
 *
 *
 * TODO auto parameter type conversion.
 * TODO more javadoc
 * TODO more tests.
 * TODO code cleanup.
 * TODO replace system.out.println to Log.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class MByHaveRunner extends Runner {
	
	/** Logger. */
	private static Logger LOG = Logger.getLogger(MByHaveRunner.class.getName());

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

    public MByHaveRunner(final Class<?> testClass) throws MByHaveException, InitializationError {
        this(testClass, true);
    }

    MByHaveRunner(final Class<?> testClass, final boolean parseStoryFiles) throws MByHaveException, InitializationError {

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

//> PACKAGE PRIVATE METHODS

    void given(final Object testObject, final String given) throws MByHaveException {
        runStep(testObject, given, candidates.get(Given.class));
    }

    void when(final Object testObject, final String when) throws MByHaveException {
        runStep(testObject, when, candidates.get(When.class));
    }

    void then(final Object testObject, final String then) throws MByHaveException {
        runStep(testObject, then, candidates.get(Then.class));
    }

    void runScenario(final Object testObject, final String scenario) throws MByHaveException {

        try {
            final byte[] bytes = scenario.getBytes("UTF-8");
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try {
                runScenario(testObject, inputStream);
            } finally {
                inputStream.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    void runScenario(final Object testObject, final InputStream scenarioIs) throws IOException, MByHaveException {
        final Scenario scenario = parseScenario(scenarioIs);
        processScenario(testObject, scenario);
    }

//> PRIVATE METHODS

    /**
     * Loads a resource from the jar. The resource can be in the <tt>root</tt> or
     * in the package of the testClass. It tries to load by the testClass's
     * ClassLoader, this runner's ClassLoader and the System testLoader as well.
     *
     * @param storyFile The name of the story file.
     * @param testClass The testClass.
     * @return The InputStream of the story file.
     * @throws MByHaveException If the file is not found.
     */
    private static InputStream loadResource(final String storyFile, final Class<?> testClass) throws MByHaveException {

        {
            final InputStream storyIs = testClass.getClassLoader().getResourceAsStream(storyFile);
            if (storyIs != null) {
                return storyIs;
            }
        }
        {
            final InputStream storyIs = MByHaveRunner.class.getClassLoader().getResourceAsStream(storyFile);
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
            final InputStream storyIs = MByHaveRunner.class.getClassLoader().getResourceAsStream(packageName + "/" + storyFile);
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

        throw new MByHaveException("The story file is not found. " + storyFile);

    }

    private static List<com.moresby.have.domain.Story> parseStories(final Class<?> testClass) throws MByHaveException {
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
                    throw new MByHaveException("The story file is not readable. " + storyFile, e);
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

    private static com.moresby.have.domain.Story parseStory(final String storyName, final InputStream storyIs) throws MByHaveException, IOException {
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

    private static Scenario parseScenario(final String scenario) throws MByHaveException {

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

    private static Scenario parseScenario(final InputStream scenario) throws MByHaveException, IOException {
        final InputStreamReader isReader = new InputStreamReader(scenario);
        final BufferedReader    reader   = new BufferedReader(isReader);

        String scenarioDescription = null;
        final List<String> steps = new ArrayList<String>();

        String line = null;
        StringBuilder scenarioBuilder = null;
        boolean parseDescription = false;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("Scenario")) {
                if (scenarioBuilder == null) {
                    scenarioBuilder = new StringBuilder(line);
                    parseDescription = true;
                } else {
                    throw new MByHaveException("This scenario contains two scenario descriptions.");
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
                    scenarioDescription = "Scenario"; //Default scenario description
                    scenarioBuilder = new StringBuilder(line);
                }
            } else if (scenarioBuilder != null) {
                scenarioBuilder.append(line + "\n");
            }
        }
        if (scenarioBuilder != null) {
            if (parseDescription) {
                throw new MByHaveException("The scenario description does not contain any step description");
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
                return given.definition();
            }
        } else if (annotation == When.class) {
            if (method.isAnnotationPresent(When.class)) {
                final When when = method.getAnnotation(When.class);
                return when.definition();
            }
        } else if (annotation == Then.class) {
            if (method.isAnnotationPresent(Then.class)) {
                final Then then = method.getAnnotation(Then.class);
                return then.definition();
            }
        }
        return null;
    }


    /**
     * Finds and initializes the step candidates.
     * This method looks for the <tt>annotated</tt> methods in the <tt>testClass</tt> and adds the found
     * methods as {@link StepCandidate}s to the <tt>stepCandidatesList</tt>
     *  
     * @param annotation The annotation the method is looking for.
     * @param testClass The class the method is scanning for annotated methods.
     * @param stepCandidatesList The list to which the annotated methods will be added as {@link StepCandidate}s. 
     */
    private static <T extends Annotation> void initStepCandidates(final Class<T> annotation, final Class<?> testClass, final List<StepCandidate> stepCandidatesList) {
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

    private static String getPlaceholderPattern(final String paramName) {
        return "$" + paramName;
    }

    private static String createRegEx(final String stepValue, final Collection<String> paramNames) {
        String regEx = Pattern.quote(stepValue);

        for (final String paramName : paramNames) {
            final String paramPlaceHolder = getPlaceholderPattern(paramName);
            regEx = regEx.replace(paramPlaceHolder, "\\E(.*)\\Q");
        }
        return '^' + regEx + '$';
    }

    private void runCandidate(final Object testObject, final StepCandidate candidate, final Matcher matcher, final String step) throws MByHaveException {
        
    	LOG.fine("Run stepCandiate: " + candidate.getStepDefinition());
    	final Map<Integer, MethodParameter> positions = candidate.getParameterPositions();
        int i = 1;

        final SortedMap<Integer, String> methodParameters = new TreeMap<Integer, String>();
        for (final MethodParameter param : positions.values()) {
            final int starts = matcher.start(i);
            final int ends   = matcher.end(i);

            final String paramValue = step.substring(starts, ends);

            LOG.finer("Param Value: " + paramValue + " Group: " + matcher.group(i));
            i++;

            methodParameters.put(Integer.valueOf(param.getParamPos()), paramValue);

            LOG.finer("Parameter name: " + param.getParamName() + " Value: " + paramValue);
        }

        LOG.finer("Num of params: " + candidate.getMethod().getParameterTypes().length);

        try {
            candidate.getMethod().invoke(testObject, methodParameters.values().toArray());
        } catch (final IllegalArgumentException e) {
            throw new MByHaveException("The parameters could not be matched.", e);
        } catch (final IllegalAccessException e) {
            throw new MByHaveException("The annotatated method should be public.", e);
        } catch (final InvocationTargetException e) {
            if (e.getTargetException() instanceof AssertionError) {
                throw (AssertionError) e.getTargetException();
            }
            throw new MByHaveException(e);
        }

    }


    private void runStep(final Object testObject, final String step, final Collection<StepCandidate> stepCandidates) throws MByHaveException {
        boolean found = false;
        for (final StepCandidate candidate : stepCandidates) {

            final Matcher matcher = candidate.getPattern().matcher(step.replace('\n', ' '));
            if (matcher.find()) {
                found = true;
                runCandidate(testObject, candidate, matcher, step);
                break;
            }
        }
        if (!found) {
            throw new MByHaveAssertionError("No maching step to the \"" + step + "\" step definition.");
        }

    }

    private void processScenario(final Object testObject, final Scenario scenario) throws MByHaveException {
        LOG.info("Process Scenario: " + scenario.getDescription());
        for (final String step : scenario.getSteps()) {
            processStep(testObject, step);
        }
    }

    private void processStep(final Object testObject, final String step) throws MByHaveException {
        LOG.info("Process step: " + step);
        
        for (final StepKeyword keyword : keywords.values()) {
            if (step.startsWith(keyword.getKeyword())) {
            	
            	/* Gets rid of the keyword and the leading and trailing whitespace. */
            	final String trimmedStep = step.substring(keyword.getKeyword().length()).trim();
            	
                runStep(testObject, trimmedStep, candidates.get(keyword.getAnnotation()));
                return;
            }
        }
        throw new IllegalArgumentException(); //TODO

    }

//> JUNIT RUNNER

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
        mainDescription   = Description.createSuiteDescription(testClass.getName());

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

    private static class StoryDescription {

        private final String                    name;
        private final Description               description;
        private final List<ScenarioDescription> scenarios;

        /**
         * @param name The name of the story. (the name of the file containing the story.)
         * @param description The JUnit description.
         * @param scenarios The JUnit descriptions of the scenarios.
         */
        private StoryDescription(final String name, final Description description, final List<ScenarioDescription> scenarios) {
            super();
            this.name        = name;
            this.description = description;
            this.scenarios   = scenarios;
        }

        /**
         * @return the name The name of the story. (the name of the file containing the story.)
         */
        private String getName() {
            return name;
        }

        /**
         * @return the description The JUnit description.
         */
        private Description getDescription() {
            return description;
        }

        /**
         * @return the scenarios The JUnit descriptions of the scenarios.
         */
        private List<ScenarioDescription> getScenarios() {
            return scenarios;
        }

    }


    private static class ScenarioDescription {

        private final String                name;
        private final Description           description;
        private final List<StepDescription> steps;

        /**
         * @param name The name of the scenario - parsed from the file/scenario definition.
         * @param description The JUnit description.
         * @param steps The description wrapper objects of the steps of the scenario.
         */
        private ScenarioDescription(final String name, final Description description, final List<StepDescription> steps) {
            super();
            this.name        = name;
            this.description = description;
            this.steps       = steps;
        }

        /**
         * @return The name of the scenario - parsed from the file/scenario definition.
         */
        private String getName() {
            return name;
        }

        /**
         * @return The JUnit description.
         */
        private Description getDescription() {
            return description;
        }

        /**
         * @return The description wrapper objects of the steps of the scenario.
         */
        private List<StepDescription> getSteps() {
            return steps;
        }


    }


    private static class StepDescription {

        private final String      step;
        private final Description description;

        /**
         * @param step The <i>step</i>
         * @param description The JUnit description of the step.
         */
        public StepDescription(final String step, final Description description) {
            super();
            this.step        = step;
            this.description = description;
        }

        /**
         * @return The <i>step</i>
         */
        public String getStep() {
            return step;
        }

        /**
         * @return The JUnit description of the step.
         */
        public Description getDescription() {
            return description;
        }
    }




}
