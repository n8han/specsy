// Copyright © 2010-2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.specsy.examples

import org.junit.runner.RunWith
import net.orfjackal.specsy.{Spec, Specsy}

@RunWith(classOf[Specsy])
class HelloWorldSpec extends Spec {

  // top-level spec; add your test code here and/or the child specs

  "name of the spec" >> {
    // first child spec
  }

  "name of the spec" >> {
    // second child spec

    "name of the spec" >> {
      // a nested child spec
    }
  }
}
