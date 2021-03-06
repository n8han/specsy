// Copyright © 2010-2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.specsy.junit

import org.junit.Test
import org.junit.Assert._
import org.hamcrest.CoreMatchers._
import org.junit.runner._
import org.junit.runners.Suite
import org.junit.runner.notification._
import scala.collection.mutable.Buffer
import org.junit.internal.builders.JUnit4Builder

class JUnitLearningTest {
  val events = Buffer[String]()

  // execution order of methods within a class is compiler-specific
  val eventsRunOrder1 = Buffer(
    "testRunStarted",
    "testStarted test1(net.orfjackal.specsy.junit.DummyTest1)",
    "testFinished test1(net.orfjackal.specsy.junit.DummyTest1)",
    "testStarted test2(net.orfjackal.specsy.junit.DummyTest2)",
    "testFinished test2(net.orfjackal.specsy.junit.DummyTest2)",
    "testStarted test3(net.orfjackal.specsy.junit.DummyTest2)",
    "testFinished test3(net.orfjackal.specsy.junit.DummyTest2)",
    "testRunFinished")
  val eventsRunOrder2 = Buffer(
    "testRunStarted",
    "testStarted test1(net.orfjackal.specsy.junit.DummyTest1)",
    "testFinished test1(net.orfjackal.specsy.junit.DummyTest1)",
    "testStarted test3(net.orfjackal.specsy.junit.DummyTest2)",
    "testFinished test3(net.orfjackal.specsy.junit.DummyTest2)",
    "testStarted test2(net.orfjackal.specsy.junit.DummyTest2)",
    "testFinished test2(net.orfjackal.specsy.junit.DummyTest2)",
    "testRunFinished")

  @Test
  def running_individual_test_classes() {
    val core = new JUnitCore
    core.addListener(new SpyRunListener)
    val result = core.run(classOf[DummyTest1], classOf[DummyTest2])

    assertThat(events, is(anyOf(equalTo(eventsRunOrder1), equalTo(eventsRunOrder2))))
    assertThat(result.getRunCount, is(3))
  }

  @Test
  def running_test_suites() {
    val core = new JUnitCore
    core.addListener(new SpyRunListener)
    val result = core.run(classOf[DummySuite])

    // the suite itself is not seen by the RunListener
    // - the output is exactly the same as when running individual test classes
    assertThat(events, is(anyOf(equalTo(eventsRunOrder1), equalTo(eventsRunOrder2))))
    assertThat(result.getRunCount, is(3))
  }

  @Test
  def values_returned_by_suite_runners() {
    val suiteRunner = new Suite(classOf[DummySuite], new JUnit4Builder)
    assertThat(suiteRunner.testCount, is(3))

    val desc = suiteRunner.getDescription
    assertThat(desc.getDisplayName, is("net.orfjackal.specsy.junit.DummySuite"))

    // contains the full tree of suites and tests
    val children = desc.getChildren
    assertThat(children.size, is(2))
    assertThat(children.get(0).getDisplayName, is("net.orfjackal.specsy.junit.DummyTest1"))
    assertThat(children.get(0).getChildren.size, is(1))
    assertThat(children.get(1).getDisplayName, is("net.orfjackal.specsy.junit.DummyTest2"))
    assertThat(children.get(1).getChildren.size, is(2))
  }


  private class SpyRunListener extends RunListener {
    override def testIgnored(description: Description) {
      events.append("testIgnored " + description.getDisplayName)
    }

    override def testAssumptionFailure(failure: Failure) {
      events.append("testAssumptionFailure " + failure.getDescription.getDisplayName)
    }

    override def testFailure(failure: Failure) {
      events.append("testFailure " + failure.getDescription.getDisplayName)
    }

    override def testFinished(description: Description) {
      events.append("testFinished " + description.getDisplayName)
    }

    override def testStarted(description: Description) {
      events.append("testStarted " + description.getDisplayName)
    }

    override def testRunFinished(result: Result) {
      events.append("testRunFinished")
    }

    override def testRunStarted(description: Description) {
      events.append("testRunStarted")
    }
  }
}

@RunWith(classOf[Suite])
@Suite.SuiteClasses(Array(classOf[DummyTest1], classOf[DummyTest2]))
class DummySuite

class DummyTest1 {
  @Test def test1() {}
}

class DummyTest2 {
  @Test def test2() {}

  @Test def test3() {}
}
