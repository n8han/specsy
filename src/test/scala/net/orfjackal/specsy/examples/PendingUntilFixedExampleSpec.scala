// Copyright © 2010-2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.specsy.examples

import org.junit.runner.RunWith
import net.orfjackal.specsy.{Spec, Specsy}

@RunWith(classOf[Specsy])
class PendingUntilFixedExampleSpec extends Spec {

  "An acceptance test for an already implemented feature" >> {
    // Test code...
  }

  "An acceptance test whose feature has not yet been implemented" >> AcceptanceTestHelpers.pendingUntilFixed {
    // Test code which is still failing...
    assert(false, "this feature is not implemented")
  }
}

object AcceptanceTestHelpers {

  // When this method is in a helper class, it's easy to find all pending tests
  // by searching for all usages of this method with your IDE.
  def pendingUntilFixed(closure: => Unit) {
    try {
      closure
    } catch {
      case e =>
        System.err.println("This test is pending until fixed:")
        e.printStackTrace()
        return // test is pending
    }
    throw new AssertionError("This test would now pass. Remove the 'pendingUntilFixed' tag.")
  }
}
