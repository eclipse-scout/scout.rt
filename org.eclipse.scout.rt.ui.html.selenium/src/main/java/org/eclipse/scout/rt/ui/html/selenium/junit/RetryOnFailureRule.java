/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RetryOnFailureRule implements TestRule {

  public RetryOnFailureRule(AbstractSeleniumTest test) {
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        RetryOnFailure retryAnnotation = description.getAnnotation(RetryOnFailure.class);
        if (retryAnnotation == null && description.getTestClass() != null) {
          retryAnnotation = description.getTestClass().getAnnotation(RetryOnFailure.class);
        }
        int retryCount = (retryAnnotation == null ? 0 : retryAnnotation.value());

        // No retries --> run immediately
        if (retryCount < 1) {
          base.evaluate();
          return;
        }

        for (int i = 0; i < retryCount + 1; i++) {
          try {
            base.evaluate();
            if (i > 0) {
              System.out.println("@@@ Test succeeded (after " + (i == 1 ? "1 retry" : i + " retries") + ")");
            }
            return; // success
          }
          catch (Throwable t) {
            if (i >= retryCount) {
              // This was the last attempt -> give up and fail
              System.out.println("@@@ Test failed (" + (i + 1) + "/" + (retryCount + 1) + ") --> giving up!");
              throw t;
            }
            System.out.println("@@@ Test failed (" + (i + 1) + "/" + (retryCount + 1) + ") --> retrying " + (retryCount - i) + " more time" + (retryCount - i > 1 ? "s" : "") + "...");
          }
        }
      }
    };
  }
}
