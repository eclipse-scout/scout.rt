/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.selenium.junit;

import org.eclipse.scout.rt.ui.html.selenium.annotation.IgnoreTestOnMacOS;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule that ignores tests annotated with @{@link IgnoreTestOnMacOS}. This is sometimes necessary to make all tests
 * "green", because apparently the Selenium driver cannot send all key strokes correctly in Mac OS (e.g. see
 * https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/5919). <b>DO NOT USE THIS ANNOTATION WITHOUT
 * A GOOD REASON!</b>
 */
public class IgnoreTestOnMacOSRule implements TestRule {

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
          System.err.println("Ignoring test " + className + "." + methodName + " because the VM is running on Mac OS and the test is annotated with @" + IgnoreTestOnMacOS.class.getSimpleName() + ".");
          return;
        }
        base.evaluate();
      }
    };
  }
}
