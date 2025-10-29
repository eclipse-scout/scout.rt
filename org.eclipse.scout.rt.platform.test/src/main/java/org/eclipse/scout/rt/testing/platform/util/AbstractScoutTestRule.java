/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

public abstract class AbstractScoutTestRule implements TestRule {

  /**
   * Regularly a {@link TestRule} can only be applied for the whole class, this method may be used to apply it just for a certain {@link IRunnable} (e.g. use it only within one method).
   * <p>
   * Example usage for {@link MockConfigPropertyRule}:
   * <pre>
   * &commat;Test
   * public void testLorem() {
   *   MockConfigPropertyRule&lt;Integer&gt; rule = new MockConfigPropertyRule&lt;&gt;(LoremConfigProperty.class, 42);
   *   rule.run(() -> {
   *     assertEquals(73, BEANS.get(LoremService.class).doSomething());
   *
   *     // manipulate the config property
   *     rule.setValue(43);
   *
   *     assertEquals(97, BEANS.get(LoremService.class).doSomething());
   *   });
   * }
   * </pre>
   * <p>
   */
  public void run(IRunnable runnable) {
    try {
      apply(new Statement() {
        @Override
        public void evaluate() throws Exception {
          runnable.run();
        }
      }, null).evaluate();
    }
    catch (RuntimeException | Error e) {
      throw e; // re-throw immediately
    }
    catch (Throwable throwable) {
      throw new ProcessingException("Error during runnable", throwable);
    }
  }
}
