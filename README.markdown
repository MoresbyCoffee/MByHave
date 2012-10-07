<div style="clear: both;">
<p>
MByHave is a <a href="http://en.wikipedia.org/wiki/Behavior_Driven_Development">BDD (Behaviour Driven Development)</a> tool for Java. Very similar to <a href="http://jbehave.org/">JBehave</a> but it is more simple and easy to integrate to JUnit.<br />
MByHave is delivered with a JUnit runner (plugin) which can parse story files and match them to the annotated test file methods.</p>

</div>


<h2>Pros and cons</h2>

<h4>Pros</h4>
<ul>
<li>Very simple</li>
<ul>
<li>Easy-to-use</li>
<li>Easy to configure</li>
</ul>
<li>JUnit plugin</li>
<ul>
<li>Supports @BeforeClass, @Before, @After and @AfterClass JUnit annotations.</li>
</ul>
</ul>

<h4>Cons</h4>
<ul>
<li
>No reporting subsystem</li>
<li>Just a JUnit plugin</li>
</ul>

<h2>How to use</h2>

<h3>Step 0.</h3>
<p>Before you could use you have to download the lib from <a href="https://github.com/MoresbyCoffee/MByHave/downloads">the download page</a> or you can configure the maven to pull it out from the repository:</p>
<pre>
&lt;repositories&gt;
    ...
    &lt;repository&gt;
        &lt;id&gt;Sonatype oss&lt;/id&gt;
        &lt;url&gt;https://oss.sonatype.org/content/repositories/snapshots&lt;/url&gt;
    &lt;/repository&gt;
    ...
&lt;/repositories&gt;
...
&lt;dependencies&gt;
    ...
    &lt;dependency&gt;
        &lt;groupId&gt;org.moresbycoffee&lt;/groupId&gt;
        &lt;artifactId&gt;have&lt;/artifactId&gt;
        &lt;version&gt;1.0.0-SNAPSHOT&lt;/version&gt;
    &lt;/dependency&gt;
    ...
&lt;/dependencies&gt;
</pre>
<h3>Step 1. - Annotate your methods.</h3>

<p>You can use the following annotations on your methods: @Given, @When and @Then. 
Each annotation takes a array parameter, called value, this array contains the patters which will be matched to the step descriptions later. 
The pattern can contain any text and placeholders of the method parameters. 
The parameters during the matching will be picked up from the step description and past to the method as defined parameter.</p>
<p><strong>Comment:</strong>
Currently there is no autoconvert, the parameters of the methods have to be Strings.</p>

<h4>Examples:</h4>

Given without parameter:
<pre class="brush:java">
@Given("something")
public void givenMethodName() {
    &lt;&lt;test logic&gt;&gt;
}
</pre>

Given with parameters:

<pre class="brush:java">
@Given("something with two parameters: $param1 and $param2)
public void givenWithParameter(final String param1, final String param2) {
    &lt;&lt;test logic>>
}
</pre>

When with parameters:

<pre class="brush:java">
@When("anything is $param")
public void whenMethod(final String param) {
    &lt;&lt;test logic&gt;&gt;
}
</pre>

Then with parameters:
<pre class="brush:java">
@Then("the result is $param")
public void thenMethod(final String param) {
    &lt;&lt;test logic&gt;&gt;
}
</pre>

Then with multiple patterns:
<pre class="brush:java">
@Then({ "the result is $param", "the result should be $param" })
public void thenMethod(final String param) {
    &lt;&lt;test logic&gt;&gt;
}
</pre>

<p><s>The methods can't send information to each other through the MByHave API, the state of the test has to stored in the test class.</s></p>

<h3>Step 2. - Write tests</h3>
<p>There are 3 ways to write tests but the third way is the officially suggested way.</p>

<h4>1 way: Step parser</h4>
<p>The annotated step methods can be invoked from code as individual steps. The MByHave contains three methods to invoke the annotated methods with step description text.</p>
<p>The MByHave#given(String) method tries to match a @Given annotated method to the parameter string. The MByHave#when(String) does the same with @When methods, the mByHave#then(String) with the @Then methods.</p>

<h5>Example:</h5>

<pre class="brush:java">
new MByHave(this).given("something").
                  when("anything is first test").
                  then("the result is success");
</pre>

<p>This test will invoke first the  givenMethodName() method then the  whenMethod() with "first test" parameter and last the thenMethod() with "success" parameter.</p>
<p><strong>Comment:</strong> There is no restriction on the order and the number of the step descriptions. For example you can create a test like:</p>
<pre class="brush:java">
@Test
public void testFirstWay() {
    new MByHave(this).given(...).
                          given(...).
                          then(...).
                          given(...).
                          when(...).
                          then(...).
                          given(...);
}
</pre>

<h4>2. way: Scenario parser</h4>
<p>A series of steps is called as Scenario. A scenario also can be specified and run as one string. The MByHave has a runScenario method that parses and matches the steps to the annotated step methods.</p>

<h5>Example:</h5>

<pre class="brush:java">
@Test
public void testSecondWay() {
 new mByHave(this).runScenario("Scenario this is a scenario description" +
                                      "Given something" +
                                      "When anything is second test" +
                                      "Then the result is success")
}
</pre>

<p>As in the first way the test will invoke the givenMethodName(), the whenMethod() with "second test" and the thenMethod() with "success" parameters.</p>

<h4>3. way: Story file parser</h4>
<p>This is the most common way. The Scenarios can be organized in Story files. Each story file can contain one or more scenarios. The MByHave parses the stories and runs them. Each scenario runs on a newly created object, so the test class has to contain a default constructor. (or has to contain constructor not at all).</p>
<p>To run these tests you have to apply the MByHaveRunner JUnit runner and a Story annotation on the step definition class. The story description file has to defined as a parameter of the Story annotation and has to be in the package root or the same package as the step definition class is.</p>

<h5>Example:</h5>
<p>Test class:</p>
<pre class="brush:java">
@RunWith(mByHaveRunner.class)
@Story("storyFile.story")
public class TestSteps {
    
    @Given("something")
    public void givenMethodName() {
        &lt;&lt;test logic&gt;&gt;
    }

    @Given("something with two parameters: $param1 and $param2)
    public void givenWithParameter(final String param1, final String param2) {
        &lt;&lt;test logic&gt;&gt;
    }

    @When("anything is $param")
    public void whenMethod(final String param) {
        &lt;&lt;test logic&gt;&gt;
    }

    @Then("the result is $param")
    public void thenMethod(final String param) {
        &lt;&lt;test logic&gt;&gt;
    }
}
</pre>

<p>storyFile.story</p>
<pre class="brush:plain" >
Scenario definition
#Comment
Scenario this is a first scenario description
Given something
When anything is first test
The result is success

Scenario definition
#Comment
Scenario this is a second scenario description
Given something with two parameters first test parameter and second test parameter
When anything is second test
The result is success
</pre>

<p>Running the test the following methods will be invoked:</p>
<ol>
<li>First scenario</li>
<ol>
<li>StepTest instantiation</li>
<li>givenMethodName()</li>
<li>whenMethod("first test")</li>
<li>thenMethod("success")</li>
</ol>
<li>Second scenario</li>
<ol>
<li>StepTest instantiation</li>
<li>givenWithParameter("first test parameter", "second test parameter")</li>
<li>whenMethod("first test")</li>
<li>thenMethod("success")</li>
</ol>
</ol>

<h2>Extensions</h2>

<h3>ReturnValue</h3>
<p>The purpose of this class is to store return values from the <tt>step definition methods</tt>.</p>
<p>If a <tt>step definition method</tt> has <tt>ReturnValue</tt> parameter, the parameter placeholder should be in <tt>step definition pattern</tt> because this value is passed to the method by the MByHave framework.</p>
<p>The return value parameter will always represent the last return value of which the type matches to the ReturnValue's generic parameter (<code>T</code>).</p>
<p>If there is no matching return value, a <tt>null</tt> object will be provided.</p>

<h5>Example:</h5>
<p>Test class:</p>
<pre class="brush:java">
private static final String TEST_STRING = "TestString";

@Given("a string return value")
public String returnValue() {
    return TEST_STRING;
}

@Then("the string return value is available in this method")
public void assertReturnValue(ReturnValue<String> returnValue) {
    assertEquals(TEST_STRING, returnValue.getValue());
}
</pre>

<h3>Container</h3>
<p>The container stores a value and the type of the value.</p>
<p>The container can be used to cache and pick up a named parameter. If a <tt>Container</tt> parameter is added to a <tt>step</tt> definition method, MByHave will pass there a cached container. The container will be picked up from a named cache (map) by the string in the <tt>step description</tt> matching to parameter placeholder.</p>
<p>So the container's name is defined in the <tt>step description</tt> and therefore two steps with the same <tt>step definition method</tt> can use different containers.</p>
<p>If there is no container existing under the given name, MByHave will create one with a <tt>null</tt>value.</p>


<h5>Example:</h5>
<p>Test class:</p>
<pre class="brush:java">
    @When("a $text is added to a container called: $container")
    public void addToContainer(final String text, final Container<String> container) {
        container.setValue(text);
    }

    @Then("the $container container should contain the $value")
    public void checkContainer(final Container<String> container, final String value) {
        assertEquals(value, container.getValue());
    }
</pre>
<p>Story file</p>
<pre class="brush:plain" >
When a blah blah is added to a container called: testContainer
Then the testContainer container should contain the blah blah
</pre>

<h3>Step priority</h3>
<p>Sometimes it can happen that two patterns are matching to the same step description. 
To handle these situations the <strong>step priority</strong> has been added to the MByHave.
With the step priority you can prioritize your steps and define a proper order among them.
The step with highest priority will be tired first and then the second highest etc.
</p>

<h5>Example:</h5>
<p>Test class:</p>
<pre class="brush:java">
	private boolean firstTest = false; 
    
    @Given(value = "a method with a $parameter", priority = 1)
    public void givenTestMethodA(final String parameter) {
        fail();
    }
    
    @Given(value = "a method with a $parameter plus something", priority = 2)
    public void givenTestMethodB(final String parameter) {
        firstTest = true;
    }
    
    /**
     * Tests priority in one direction. Methods in <i>false, right</i> order.
     */
    @Test 
    public void testGivenPriority() {
        new MByHave(this).given("a method with a parameter plus something");
        assertTrue(firstTest);
    }
</pre>

<h2>Change log</h2>
<h5>v1.0 Beta</h5>
<ul>
<li>Multi pattern definitions</li>
<li>Step definition priority</li>
<li><tt>ReturnValue</tt> parameter type</li>
<li><tt>Container</tt> parameter type</li>
</ul>

<h2>Roadmap</h2>
<h5>v1.0</h5>
<ul>
<li><s>maven repository deployment</s></li>
<li>more javadoc</li>
<li><s>&#64;Before, &#64;After, &#64;BeforeClass and &#64;AfterClass support</s></li>
<li><s>code cleanup - split up MByHaveRunner.<s></li>
<li><s>Container support</s></li>
<li><s>ReturnValue support</s></li>
<li>NetBeans test result fix</li>
</ul>

<h5>v1.1</h5>
<ul>
<li>auto parameter type conversion</li>
</ul>
<h5>v1.2</h5>
<ul>
<li><tt>And</tt> keyword</li>
<li>&#64;Rule Junit annotation support.</li>
</ul>
<h5>v2.0</h5>
<ul>
<li>spring integration</li>
</ul>
<h5>v3.0</h5>
<ul>
<li>android integration</li>
</ul>


<h2>License</h2>
<p>The code is under BSD License but <a href="http://www.junit.org/">JUnit</a> with the <a href="http://www.junit.org/license">Common Public Licence</a> and <a href="http://paranamer.codehaus.org/">Paranamer</a> with <a href="http://paranamer.codehaus.org/info/license.html">Creative Common License</a> are linked.</p>