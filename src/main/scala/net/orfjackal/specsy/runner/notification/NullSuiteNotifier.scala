// Copyright © 2010-2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.specsy.runner.notification

import net.orfjackal.specsy.core.Path
import java.lang.String

class NullSuiteNotifier extends SuiteNotifier {
  def fireTestFound(path: Path, name: String, location: Object) {}

  def submitTestRun(testRun: Runnable) {}

  def fireTestStarted(path: Path): TestNotifier = new NullTestNotifier
}
