
Specsy
======

Specsy is a [BDD](http://dannorth.net/introducing-bdd)-style unit-level testing framework for [Scala](http://www.scala-lang.org/). Specsy safely isolates mutable state and supports writing self-documenting tests/specifications.

- Mailing list: <http://groups.google.com/group/specsy>
- Source code: <http://github.com/orfjackal/specsy>
- License: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
- Developer: [Esko Luontola](http://www.orfjackal.net/)


### Project Goals

- **Unlimited Nesting** - The specs can be organized into a nested hierarchy. This makes it possible to apply [One Assertion Per Test](http://www.artima.com/weblogs/viewpost.jsp?thread=35578) which [isolates](http://agileinaflash.blogspot.com/2009/02/first.html) the reason for a failure, because the specs are very fine-grained. This flexibility also makes writing [specification-style](http://blog.orfjackal.net/2010/02/three-styles-of-naming-tests.html) tests easier.

- **Isolated Execution** - To make it easy to write [repeatable](http://agileinaflash.blogspot.com/2009/02/first.html) tests, each spec is isolated from the side-effects of its sibling specs. By default, each spec will see only the side-effects of its parent specs. Note that Specsy discourages writing non-repeatable fat integration tests, so a [Before](http://junit.sourceforge.net/javadoc/org/junit/BeforeClass.html)/[AfterClass](http://junit.sourceforge.net/javadoc/org/junit/AfterClass.html) concept is outside the scope of this project (or at a very low priority - I have some ideas on how it could be done).

- **No Forced Words** - In order to let you choose the best possible test names, Specsy does not impose any [predefined words](http://blog.orfjackal.net/2010/05/choice-of-words-in-testing-frameworks.html) on its users.

- **Simplicity** - Specsy contains only the essential features, but does them well. Having a particular assertion syntax is not essential and it's easy to use the assertions of other testing libraries, so Specsy itself does not have assertions. Also any syntactic sugar is minimized, in order for it to be easy to know what the code does just by looking at it.

- **Parallel Execution** - Running tests [fast](http://agileinaflash.blogspot.com/2009/02/first.html) is a must for using TDD (my pain threshold for recompile and test execution is about 5-10 seconds). Specsy makes it possible to parallelize the test execution using the maximal number of CPU cores (not yet implemented).


Quick Start
-----------

If you use Maven, add the following dependency to your POM file. If you don't use Maven, download the files manually from [Maven Central Repository](http://repo1.maven.org/maven2/net/orfjackal/specsy/specsy/).

    <dependency>
        <groupId>net.orfjackal.specsy</groupId>
        <artifactId>specsy</artifactId>
        <version>1.2.0</version>
        <scope>test</scope>
    </dependency>

The default artifact is compiled using Scala 2.9.0. If you are using Scala 2.8.1, add the `scala281` classifier to your dependency:

    <dependency>
        <groupId>net.orfjackal.specsy</groupId>
        <artifactId>specsy</artifactId>
        <version>1.2.0</version>
        <classifier>scala281</classifier>
        <scope>test</scope>
    </dependency>

Then you can create a Specsy spec by extending the [Spec] trait. Annotate the class with `@RunWith` to execute it with JUnit. The following shows the structure of a spec:

    import org.junit.runner.RunWith
    import net.orfjackal.specsy._

    @RunWith(classOf[Specsy])
    class HelloWorldSpec extends Spec {

      // top-level spec; add your test code here and/or the child specs

      "..." >> {
        // first child spec
      }

      "..." >> {
        // second child spec

        "..." >> {
          // a nested child spec
        }
      }
    }

You can add test code to any of the blocks between curly braces - semantically there is no difference between the top-level spec and all the nested child specs. There can be as many or few nested specs as you wish (including zero). A child spec will see the side-effects of its parent specs, but it cannot see any side-effects from its sibling specs. Potentially every leaf child spec may be executed in its own thread (not yet implemented - a better test runner than JUnit is needed first).

Specsy does not contain its own assertion syntax, so you can use the assertions from [JUnit](http://www.junit.org/), [Specs](http://code.google.com/p/specs/), [ScalaTest](http://www.artima.com/scalatest/) or any other framework which makes it possible. Refer to the documentation of those frameworks for instructions on how to use their assertions in another framework.


Documentation
-------------

[FibonacciSpec] is an example of how to use descriptive [specification-style](http://blog.orfjackal.net/2010/02/three-styles-of-naming-tests.html) test names:

    @RunWith(classOf[Specsy])
    class FibonacciSpec extends Spec {
      val sequenceLength = 10
      val fib = new Fibonacci().sequence(sequenceLength)
      assertThat(fib.length, is(sequenceLength))

      "The first two Fibonacci numbers are 0 and 1" >> {
        assertThat(fib(0), is(0))
        assertThat(fib(1), is(1))
      }
      "Each remaining number is the sum of the previous two" >> {
        for (i <- 2 until fib.length) {
          assertThat(fib(i), is(fib(i - 1) + fib(i - 2)))
        }
      }
    }


### Assertions

To use the assertions from JUnit (although they don't always play well with Scala's generics), add the following imports to your test file:

    import org.junit.Assert._
    import org.hamcrest.CoreMatchers._

To use the assertions from Specs, mix the test class with the [org.specs.SpecsMatchers](http://code.google.com/p/specs/wiki/MatchersGuide#Use_specs_matchers_alone) trait:

    @RunWith(classOf[Specsy])
    class SomeSpec extends Spec with SpecsMatchers {
    }

To use the assertions from ScalaTest, mix the test class with the [org.scalatest.matchers.ShouldMatchers](http://www.scalatest.org/scaladoc/doc-1.2/org/scalatest/matchers/ShouldMatchers.html) trait or one of the other matcher traits:

    @RunWith(classOf[Specsy])
    class SomeSpec extends Spec with ShouldMatchers {
    }

Any other assertions are also OK. All that is needed is that they throw an exception when the assertion fails.


### Isolated Execution Model

[StackSpec] illustrates the isolated execution model. As you notice, the stack is a mutable data structure and it is being modified in nearly every child spec. But each child spec can trust that it sees only the modifications of its parent specs, so there are no weird order-dependent test failures - everything just works as expected.

    @RunWith(classOf[Specsy])
    class StackSpec extends Spec {
      val stack = new scala.collection.mutable.Stack[String]

      "An empty stack" >> {

        "is empty" >> {
          assertTrue(stack.isEmpty)
        }
        "After a push, the stack is no longer empty" >> {
          stack.push("a push")
          assertFalse(stack.isEmpty)
        }
      }

      "When objects have been pushed onto a stack" >> {
        stack.push("pushed first")
        stack.push("pushed last")

        "the object pushed last is popped first" >> {
          val poppedFirst = stack.pop()
          assertThat(poppedFirst, is("pushed last"))
        }
        "the object pushed first is popped last" >> {
          stack.pop()
          val poppedLast = stack.pop()
          assertThat(poppedLast, is("pushed first"))
        }
        "After popping all objects, the stack is empty" >> {
          stack.pop()
          stack.pop()
          assertTrue(stack.isEmpty)
        }
      }
    }

A rule of thumb is that out of all sibling specs (i.e. child specs with the same parent) always *exactly one sibling spec is executed during a test run*. So when the closure of a spec is executed and Specsy encounters a child spec declaration, it will selectively execute one of its child specs (right where it is declared) and skip the others. Then a fresh instance of the test class is created and executed, until all child specs have been executed.


### Non-Isolated Execution Model

In some cases it may be desirable to avoid the isolation of side-effects; perhaps it would make the tests harder to organize (e.g. writing tests for a multi-step process) or it would affect performance too much (e.g. side-effect free parameterized tests). For those situations you may call `shareSideEffects()` which will cause all child specs of the current spec to see each other's side-effects. [ShareSideEffectsExampleSpec] illustrates this:

    @RunWith(classOf[Specsy])
    class ShareSideEffectsExampleSpec extends Spec {
      var i = 0

      shareSideEffects()
      "One" >> {
        i += 1
        assertThat(i, is(1))
      }
      "Two" >> {
        i += 1
        assertThat(i, is(2))
      }
      "Three" >> {
        i += 1
        assertThat(i, is(3))
      }
    }


### "Before" and "After" Blocks

In Specsy, every parent spec acts similar to the "before" blocks in other testing frameworks. And as for "after" blocks, Specsy has a construct called *defer blocks* (influenced by [Go's defer statement](http://golang.org/doc/effective_go.html#defer)). Each spec can declare as many or few defer blocks as it wishes, and they will be executed in LIFO order when the spec exits.

[DeferBlocksExampleSpec] shows how the defer blocks can be used:

    @RunWith(classOf[Specsy])
    class DeferBlocksExampleSpec extends Spec {
      val dir = new File("temp-directory-" + UUID.randomUUID())
      assert(dir.mkdir(), "failed to create: " + dir)
      defer {
        assert(dir.delete(), "failed to delete: " + dir)
      }

      val file1 = new File(dir, "file 1.txt")
      assert(file1.createNewFile(), "failed to create: " + file1)
      defer {
        assert(file1.delete(), "failed to delete:" + file1)
      }

      "..." >> {
        // do something with the files
      }

      "..." >> {
        // child specs can also use defer blocks
        val file2 = new File(dir, "file 2.txt")
        assert(file2.createNewFile(), "failed to create: " + file2)
        defer {
          assert(file2.delete(), "failed to delete:" + file2)
        }

        // 'file2' will be deleted when this child spec exits
      }

      // will delete first 'file1' and second 'dir'
      // (or if creating 'file1' failed, then will delete only 'dir')
    }

The code duplication in the above spec could be removed by extracting a method out of it, although it requires knowledge of Scala's more advanced features. [DeferBlocksExample2Spec] does the same thing as above, but with less code:

    @RunWith(classOf[Specsy])
    class DeferBlocksExample2Spec extends Spec {
      val dir = createWithCleanup(new File("temp-directory-" + UUID.randomUUID()), _.mkdir(), _.delete())
      val file1 = createWithCleanup(new File(dir, "file 1.txt"), _.createNewFile(), _.delete())

      "..." >> {
      }

      "..." >> {
        val file2 = createWithCleanup(new File(dir, "file 2.txt"), _.createNewFile(), _.delete())
      }

      def createWithCleanup(file: File, create: File => Boolean, delete: File => Boolean): File = {
        assert(create(file), "failed to create: " + file)
        defer {
          assert(delete(file), "failed to delete: " + file)
        }
        file
      }
    }


### Parameterized Tests

Because Specsy's spec declarations are implemented as method calls which take a closure as a parameter (see [Spec]), it's simple to use the framework for parameterized tests. [ParameterizedExampleSpec] shows how to do it:

    @RunWith(classOf[Specsy])
    class ParameterizedExampleSpec extends Spec {
      val parameters = List(
        (0, 0),
        (1, 1),
        (2, 4),
        (3, 9),
        (4, 16),
        (5, 25),
        (6, 36),
        (7, 49),
        (8, 64),
        (9, 81))

      for ((n, expectedSquare) <- parameters) {
        "Square of " + n + " is " + expectedSquare >> {
          assertThat(n * n, is(expectedSquare))
        }
      }
    }

Note that the code which declares the specs must be deterministic. Otherwise the test isolation mechanism may not run all specs exactly once. Also here it might be desirable to use `shareSideEffects()` as a performance optimization, assuming that the generated specs do not have side-effects.


Version History
---------------

**1.2.0 (2011-05-17)**

- Fixed the order of tests in JUnit results
- Added `shareSideEffects()` for a non-isolated execution model
- Added Scaladocs for the methods in `Spec`
- Compiled with Scala 2.9.0 and 2.8.1

**1.1.0 (2011-05-13)**

- Made the `Spec.defer` method public, to allow it to be used from helper classes
- Renamed the implicit `Spec.specify` method for declaring nested specs, to avoid potential name clashes
- Execute child specs in the same order as they are declared (when single-threaded)
- Made output capturing disabled by default. Use the JVM option `-Dspecsy.captureOutput=true` to enable it
- Upgraded to Scala 2.9.0

**1.0.1 (2010-08-29)**

- Fixed Scala's `println()` not being captured, due to `scala.Console` being unaffected by `java.lang.System.setOut()`

**1.0.0 (2010-08-16)**

- Isolated execution model
- Unlimited nested specs
- Defer blocks
- JUnit test runner


### Known Issues

- The tests are not yet executed in parallel (a new test runner is needed)
- JUnit's test runner API does not support testing frameworks which do not know beforehand what tests there are, but which know it only after executing the tests, so at least IntelliJ IDEA cannot report test progress in real time (a new test runner is needed)
- In IntelliJ IDEA's Run tool window, you should disable "Hide Passed" and enable "Select First Failed Test When Finished". Otherwise IDEA will fail to show the failed tests


License
-------

Copyright © 2010-2011 Esko Luontola <<http://www.orfjackal.net>>  
This software is released under the Apache License 2.0.  
The license text is at <http://www.apache.org/licenses/LICENSE-2.0>


[Spec]:                         http://github.com/orfjackal/specsy/blob/master/src/main/scala/net/orfjackal/specsy/Spec.scala
[FibonacciSpec]:                http://github.com/orfjackal/specsy/blob/master/src/test/scala/net/orfjackal/specsy/examples/FibonacciSpec.scala
[StackSpec]:                    http://github.com/orfjackal/specsy/blob/master/src/test/scala/net/orfjackal/specsy/examples/StackSpec.scala
[ShareSideEffectsExampleSpec]:  http://github.com/orfjackal/specsy/blob/master/src/test/scala/net/orfjackal/specsy/examples/ShareSideEffectsExampleSpec.scala
[DeferBlocksExampleSpec]:       http://github.com/orfjackal/specsy/blob/master/src/test/scala/net/orfjackal/specsy/examples/DeferBlocksExampleSpec.scala
[DeferBlocksExample2Spec]:      http://github.com/orfjackal/specsy/blob/master/src/test/scala/net/orfjackal/specsy/examples/DeferBlocksExample2Spec.scala
[ParameterizedExampleSpec]:     http://github.com/orfjackal/specsy/blob/master/src/test/scala/net/orfjackal/specsy/examples/ParameterizedExampleSpec.scala
