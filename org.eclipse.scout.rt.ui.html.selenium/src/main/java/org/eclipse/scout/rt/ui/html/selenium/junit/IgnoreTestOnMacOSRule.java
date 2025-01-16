/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.selenium.junit;

import org.eclipse.scout.rt.ui.html.selenium.annotation.IgnoreTestOnMacOS;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test rule that ignores tests annotated with @{@link IgnoreTestOnMacOS}. This is sometimes necessary to make all tests
 * "green", because apparently the Selenium driver cannot send all key strokes correctly in Mac OS (e.g. see
 * <a href="https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5919">...</a>).
 */
public class IgnoreTestOnMacOSRule implements TestRule {
  private static final Logger LOG = LoggerFactory.getLogger(IgnoreTestOnMacOSRule.class);

  private final boolean m_macOS;

  public IgnoreTestOnMacOSRule(AbstractSeleniumTest test) {
    m_macOS = SeleniumUtil.isMacOS();
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        if (m_macOS && description.getAnnotation(IgnoreTestOnMacOS.class) != null) {
          String className = description.getClassName();
          String methodName = description.getMethodName();
          LOG.warn("Ignoring test {}.{} because the VM is running on Mac OS and the test is annotated with @{}.", className, methodName, IgnoreTestOnMacOS.class.getSimpleName());
          return;
        }
        base.evaluate();
      }
    };
  }
}
