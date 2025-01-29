/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.junit;

import org.eclipse.scout.rt.ui.html.selenium.annotation.RetryOnFailure;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryOnFailureRule implements TestRule {
  private static final Logger LOG = LoggerFactory.getLogger(RetryOnFailureRule.class);

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
              LOG.info("@@@ Test succeeded (after {})", (i == 1 ? "1 retry" : i + " retries"));
            }
            return; // success
          }
          catch (Throwable t) {
            if (i >= retryCount) {
              // This was the last attempt -> give up and fail
              LOG.info("@@@ Test failed ({}/{}) --> giving up!", (i + 1), (retryCount + 1));
              throw t;
            }
            LOG.info("@@@ Test failed ({}/{}) --> retrying {} more time{}...", (i + 1), (retryCount + 1), (retryCount - i), (retryCount - i > 1 ? "s" : ""));
          }
        }
      }
    };
  }
}
