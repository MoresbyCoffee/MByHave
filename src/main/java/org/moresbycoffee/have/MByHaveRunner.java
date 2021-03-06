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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.moresbycoffee.have.StepCandidate.MethodParameter;
import org.moresbycoffee.have.annotations.Given;
import org.moresbycoffee.have.annotations.Story;
import org.moresbycoffee.have.annotations.Then;
import org.moresbycoffee.have.annotations.When;
import org.moresbycoffee.have.domain.Scenario;
import org.moresbycoffee.have.exceptions.MByHaveAssertionError;
import org.moresbycoffee.have.exceptions.MByHaveException;

import com.google.common.reflect.TypeToken;


/**
 * <p>A JUnit {@link Runner} implementation designed to run the MBy.Have story files.</p>
 * <p>The MBy.Have <strong>story</strong> files consist of <strong>scenarios</strong>.
 * Each scenario describes a test case and for each of them a new test object will be
 * instantiated. A scenario can contain one or more <p>steps</p>. The steps contains the
 * real logic of the test and they have three form: <strong>given</strong>,
 * <strong>when</strong> and <strong>then</strong>. These step types ought to be used
 * in this order (given, when, then) but there is not strict restriction. The behavior of
 * them also the same only the Given, When, Then configuration.getKeywords() are different.</p>
 * <p>The steps in a scenario will be parsed and tired to be matched to an annotated
 * method from the test class. There is one-one annotation for each step type:</p>
 * <ul>
 * <li>{@link Given} for the given steps,</li>
 * <li>{@link When} for the when steps and</li>
 * <li>{@link Then} for the then steps.</li>
 * </ul>
 * <p>Each annotation takes a value, which is a pattern by which the steps will be tried
 * being matched. The patterns has to contain the placeholders of the method parameters.
 * The placeholder syntax is: <strong><tt>$paramname</tt></strong>.</p>
 * <p>The pattern matching will match the step description and the method (defined by the
 * step definition) with the parameters. To pick up the parameter values, a reqEx pattern
 * will be used with <tt>(.*)</tt> pattern at the parameter placeholders. The matcher
 * will work in - called - greedy mode so it will pick up the longest possible string
 * from the step description.</p>
 * <p>To learn more about pattern matching visit the official Java site:
 * <a href="http://docs.oracle.com/javase/tutorial/essential/regex/">http://docs.oracle.com/javase/tutorial/essential/regex/</a></p>
 * <h4>How to use</h4>
 * <p>To use the MByHaveRunner the test class has to contain one or more annotated step
 * definition methods. The step definitions has to contain placeholder for each method
 * parameter. The method parameter has to be {@link java.lang.String}. The test class has
 * to be annotated with {@link org.junit.runner.RunWith} - added this class as parameter -
 * and with {@link Story} - added the story files as parameter.
 * </p>
 * <h4>Example</h4>
 * <h5>Test class</h5>
 * <pre>
 *    &#064;RunWith(MByHaveRunner.class)
 *    &#064;Story(files = "storytest1.story")
 *    public class Story1Test {
 *
 *
 *        &#064;Given(definition = "first method")
 *        public void firstMethod() {
 *            ...
 *        }
 *
 *        &#064;When(definition = "second method $param")
 *        public void whenTestMethod(final String param) {
 *    	      ...
 *        }
 *
 *        &#064;Then(definition = "third $param1 $param2 method")
 *        public void thenTwoParamMethod(final String param1, final String param2) {
 *    	     ...
 *        }
 *    }
 * </pre>
 * <h5>storytest1.story story file</h5>
 * <pre>
 * #This is a comment
 *
 * Scenario show how this works
 * Given first method
 * When second method <strong>param1</strong>
 * Then third <strong>param1</strong> <strong>param2</strong> method
 *
 * Scenario second scenario
 * Given first method
 * When second method <strong>trick</strong>
 * Then third <strong>blah blah</strong> <strong>ehe</strong> method
 * </pre>
 * <h4>Roadmap</h4>
 * <h5>v1.0</h5>
 * <ul>
 * <li><s>JUnit annotation support.</s></li>
 * <li>more javadoc</li>
 * <li><s>code cleanup - split up MByHaveRunner.</s></li>
 * <li>centeral maven repository deployment</li>
 * </ul>
 *
 * <h5>v1.1</h5>
 * <ul>
 * <li>auto parameter type conversion</li>
 * <li>NetBeans test result fix</li>
 * </ul>
 * <h5>v1.2</h5>
 * <ul>
 * <li><tt>And</tt> keyword</li>
 * </ul>
 * <h5>v2.0</h5>
 * <ul>
 * <li>android integration</li>
 * </ul>
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class MByHaveRunner extends Runner {

	/** Logger. */
	private static Logger LOG = Logger.getLogger(MByHaveRunner.class.getName());

	private final MByHaveConfiguration configuration;

    private final List<org.moresbycoffee.have.domain.Story> stories;

    /** Containers store any value for further use. The containers can identified by the container name. */
    @SuppressWarnings("rawtypes")
    private final Map<String, Container> containerMap = new HashMap<String, Container>();
    /** The history of the return values of the steps. */
    @SuppressWarnings("rawtypes")
    private final LinkedList<ReturnValue> returnValueHistory = new LinkedList<ReturnValue>();

//> CONSTRUCTORS

    public MByHaveRunner(final Class<?> testClass) throws MByHaveException, InitializationError {
        this(testClass, true);
    }

    MByHaveRunner(final Class<?> testClass, final boolean parseStoryFiles) throws MByHaveException, InitializationError {

        configuration = MByHaveConfigurator.configure(testClass, parseStoryFiles);

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
        runStep(testObject, given, configuration.getCandidates().get(Given.class));
    }

    void when(final Object testObject, final String when) throws MByHaveException {
        runStep(testObject, when, configuration.getCandidates().get(When.class));
    }

    void then(final Object testObject, final String then) throws MByHaveException {
        runStep(testObject, then, configuration.getCandidates().get(Then.class));
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
     * in the package of the configuration.getTestClass(). It tries to load by the configuration.getTestClass()'s
     * ClassLoader, this runner's ClassLoader and the System testLoader as well.
     *
     * @param storyFile The name of the story file.
     * @param configuration.getTestClass() The configuration.getTestClass().
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

    private static List<org.moresbycoffee.have.domain.Story> parseStories(final Class<?> testClass) throws MByHaveException {
        final List<org.moresbycoffee.have.domain.Story> mutableStories = new ArrayList<org.moresbycoffee.have.domain.Story>();
        if (testClass.isAnnotationPresent(Story.class)) {
            final Story story = testClass.getAnnotation(Story.class);
            final String[] storyFiles = story.files();

            for (final String storyFile : storyFiles) {

                final InputStream storyIs = loadResource(storyFile, testClass);
                org.moresbycoffee.have.domain.Story storyObject;
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

    private static org.moresbycoffee.have.domain.Story parseStory(final String storyName, final InputStream storyIs) throws MByHaveException, IOException {
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
        return new org.moresbycoffee.have.domain.Story(storyName, scenarios);
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
                throw new MByHaveException("The scenario description does not contain any step description"); //TODO get the scenario description from somewhere.
            }
            steps.add(scenarioBuilder.toString());
        }
        return new Scenario(scenarioDescription, steps);
    }
    
    private Type getTypeFromReturnValue(final Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            return TypeToken.of(Object.class).getType();
        }
        
    }
    
    /**
     * Returns the first matching result from the result history. The result history contains all the results of steps.
     * 
     * @param type The type what has to be matched. (NonNull)
     * @return The found result. If there is no matching result, it will return <tt>null</tt>. (Nullable)
     */
    @SuppressWarnings("rawtypes")
    private ReturnValue getReturnValueFromHistoryByType(final Type type) {
        
        final TypeToken returnValueType = TypeToken.of(getTypeFromReturnValue(type));
        for (ReturnValue returnValue : returnValueHistory) {
            if (returnValueType.isAssignableFrom(returnValue.getType())) {
                return returnValue;
            }
        }
        return null;
    }
    
    private void runCandidate(final Object testObject, final StepCandidate candidate, final Matcher matcher, final String step) throws MByHaveException {

    	LOG.fine("Run stepCandiate: " + candidate.getStepDefinition());
    	final Map<Integer, MethodParameter> positions = candidate.getParameterPositions();
        int i = 1;

        final SortedMap<Integer, Object> methodParameters = new TreeMap<Integer, Object>();
        for (final MethodParameter param : candidate.getReturnValueParameters()) {
            
            @SuppressWarnings("rawtypes")
            final ReturnValue paramObject = getReturnValueFromHistoryByType(param.getType());
            
            methodParameters.put(Integer.valueOf(param.getParamPos()), paramObject);
        }
        
        for (final MethodParameter param : positions.values()) {
            
            final int starts = matcher.start(i);
            final int ends   = matcher.end(i);

            final String paramValue = step.substring(starts, ends);
            
            LOG.finer("Param Value: " + paramValue + " Group: " + matcher.group(i));
            i++;
            
            final Object paramObject;
            if (TypeToken.of(Container.class).isAssignableFrom(param.getType())) {
                
                if (containerMap.containsKey(paramValue)) {
                    //TODO check the type.
                    paramObject = containerMap.get(paramValue);
                } else {
                    
                    try {
                        @SuppressWarnings("rawtypes")
                        Container container = Container.class.getDeclaredConstructor(Type.class).newInstance(param.getType());
                        containerMap.put(paramValue, container);
                        paramObject = container;
                    } catch (Exception e) {
                        /* This exception should not occur ever. */
                        throw new MByHaveException("Container object can't be instantiated.", e);
                    } 
                }
            } else {
                paramObject = paramValue;
            }
            
            methodParameters.put(Integer.valueOf(param.getParamPos()), paramObject);

            LOG.finer("Parameter name: " + param.getParamName() + " Value: " + paramObject);
        }

        LOG.finer("Num of params: " + candidate.getMethod().getParameterTypes().length);

        final Object result = invokeMethod(candidate.getMethod(), testObject, methodParameters.values().toArray());
        
        if (!Void.TYPE.equals(candidate.getMethod().getReturnType())) {
            final Type returnType = candidate.getMethod().getGenericReturnType();
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final ReturnValue resultObject = new ReturnValue(returnType, result);
            returnValueHistory.addFirst(resultObject);
        }

    }

    private void invokeMethods(final Collection<Method> methods, final Object target) {
        for (final Method method : methods) {
            invokeMethod(method, target);
        }
    }

    private Object invokeMethod(final Method method, final Object target, final Object... parameters) {
        try {
            return method.invoke(target, parameters);
        } catch (final IllegalArgumentException e) {
            throw new MByHaveException("The parameters could not be matched.", e);
        } catch (final IllegalAccessException e) {
            throw new MByHaveException("The annotated method should be public.", e);
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
        /* Clean up the cached values. */
        returnValueHistory.clear();
        containerMap.clear();
        
        /* Run the steps. */
        for (final String step : scenario.getSteps()) {
            processStep(testObject, step);
        }
    }

    private void processStep(final Object testObject, final String step) throws MByHaveException {
        LOG.info("Process step: " + step);

        for (final StepKeyword keyword : configuration.getKeywords().values()) {
            if (step.startsWith(keyword.getKeyword())) {

            	/* Gets rid of the keyword and the leading and trailing whitespace. */
            	final String trimmedStep = step.substring(keyword.getKeyword().length()).trim();

                runStep(testObject, trimmedStep, configuration.getCandidates().get(keyword.getAnnotation()));
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
        mainDescription   = Description.createSuiteDescription(configuration.getTestClass().getName());

        for (final org.moresbycoffee.have.domain.Story story : stories) {

            final Description storyDescription = Description.createSuiteDescription(++storyIndex + ". " + story.getName().replace("\n", " "));
            final List<ScenarioDescription> scenarioDescriptions = new ArrayList<ScenarioDescription>();

            int scenarioIndex = 0;
            for (final Scenario scenario : story.getScenario()) {
                final Description scenarioDescription = Description.createSuiteDescription(storyIndex + "." + (++scenarioIndex) + ". " + scenario.getDescription().replace("\n", " "));
                final List<StepDescription> stepDescriptions = new ArrayList<StepDescription>();
                int stepIndex = 0;
                for (final String step : scenario.getSteps()) {
                    final Description stepDescription = Description.createTestDescription(configuration.getTestClass(), storyIndex + "." + scenarioIndex + "." + (++stepIndex) + ". " + step.replace("\n", " "));
                    stepDescriptions.add(new StepDescription(step, stepDescription));
                    scenarioDescription.addChild(stepDescription);
                }

                scenarioDescriptions.add(new ScenarioDescription(scenarioDescription, stepDescriptions));
                storyDescription.addChild(scenarioDescription);
            }

            storyDescriptions.add(new StoryDescription(storyDescription, scenarioDescriptions));
            mainDescription.addChild(storyDescription);
        }
        return mainDescription;
    }

    /** {@inheritDoc} */
    @Override
    public void run(final RunNotifier notifier) {
        notifier.fireTestStarted(getDescription());

        try {
            invokeMethods(configuration.getBeforeClassMethods(), null);
            for (final StoryDescription storyDescription : storyDescriptions) {
                notifier.fireTestStarted(storyDescription.getDescription());
                for (final ScenarioDescription scenarioDescription : storyDescription.getScenarios()) {
                    notifier.fireTestStarted(scenarioDescription.getDescription());

                    /* Clean up the cached values. */
                    returnValueHistory.clear();
                    containerMap.clear();

                    final Object testObject = configuration.getTestClass().newInstance();
                    invokeMethods(configuration.getBeforeMethods(), testObject);
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
                    invokeMethods(configuration.getAfterMethods(), testObject);
                    notifier.fireTestFinished(scenarioDescription.getDescription());
                }
                notifier.fireTestFinished(storyDescription.getDescription());
            }
            invokeMethods(configuration.getAfterClassMethods(), null);
        } catch (final Throwable t) {
            notifier.fireTestFailure(new Failure(mainDescription, t));
        }
        notifier.fireTestFinished(mainDescription);
    }

    private static class StoryDescription {

        private final Description               description;
        private final List<ScenarioDescription> scenarios;

        /**
         * @param description The JUnit description.
         * @param scenarios The JUnit descriptions of the scenarios.
         */
        private StoryDescription(final Description description, final List<ScenarioDescription> scenarios) {
            super();
            this.description = description;
            this.scenarios   = scenarios;
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

        private final Description           description;
        private final List<StepDescription> steps;

        /**
         * @param description The JUnit description.
         * @param steps The description wrapper objects of the steps of the scenario.
         */
        private ScenarioDescription(final Description description, final List<StepDescription> steps) {
            super();
            this.description = description;
            this.steps       = steps;
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
        private StepDescription(final String step, final Description description) {
            super();
            this.step        = step;
            this.description = description;
        }

        /**
         * @return The <i>step</i>
         */
        private String getStep() {
            return step;
        }

        /**
         * @return The JUnit description of the step.
         */
        private Description getDescription() {
            return description;
        }
    }




}
