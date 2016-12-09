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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Test rule that takes a screenshot of the current state when a test fails and the system property
 * "take.screenshot.on.failure" is set to "true".
 */
public class ScreenshotRule implements TestRule {

  private final WebDriver m_driver;
  private final boolean m_active;

  public ScreenshotRule(AbstractSeleniumTest test) {
    m_driver = test.getDriver();
    boolean active = SeleniumUtil.takeScreenShotOnFailure();
    if (active && !(m_driver instanceof TakesScreenshot)) {
      System.err.println("WARNING: Cannot take screenshots on failure because driver cannot take screenshots (" + m_driver + ")");
      active = false;
    }
    m_active = active;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        }
        catch (Throwable t) {
          if (m_active) {
            captureScreenshot(description);
          }
          throw t;
        }
      }
    };
  }

  public void captureScreenshot(Description description) {
    try {
      File screenshotDir = new File("target/surefire-reports/");
      screenshotDir.mkdirs(); // Ensure directory is there
      String timestamp = DateUtility.format(new Date(), "yyyyMMddhhmmss");
      String className = description.getClassName();
      String methodName = description.getMethodName();
      File screenshotFile = new File(screenshotDir, "screenshot-" + timestamp + "-" + className + "." + methodName + ".png");

      FileOutputStream out = new FileOutputStream(screenshotFile);
      System.out.println("Test failed, took as screenshot: " + screenshotFile);
      out.write(((TakesScreenshot) m_driver).getScreenshotAs(OutputType.BYTES));
      out.close();
    }
    catch (Exception e) {
      System.err.println("Could not take a screenshot because of: " + e.getMessage());
    }
  }
}
