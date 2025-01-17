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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumScreenshotOnFailureProperty;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test rule that takes a screenshot of the current state when a test fails and the property
 * {@link SeleniumScreenshotOnFailureProperty} is set to {@code true}.
 */
public class ScreenshotRule implements TestRule {
  private static final Logger LOG = LoggerFactory.getLogger(ScreenshotRule.class);

  private final WebDriver m_driver;
  private final boolean m_active;

  public ScreenshotRule(AbstractSeleniumTest test) {
    m_driver = test.getDriver();
    boolean active = SeleniumUtil.takeScreenShotOnFailure();
    if (active && !(m_driver instanceof TakesScreenshot)) {
      LOG.warn("Cannot take screenshots on failure because driver cannot take screenshots ({})", m_driver);
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

  @SuppressWarnings("findbugs:RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public void captureScreenshot(Description description) {
    try {
      File screenshotDir = new File("target/surefire-reports/");
      if (!screenshotDir.exists()) { // Ensure directory is there
        boolean success = screenshotDir.mkdirs();
        if (!success) {
          throw new PlatformException("Error creating screenshot directories '{}'.", screenshotDir);
        }
      }
      String timestamp = DateUtility.format(new Date(), "yyyyMMddhhmmss");
      String className = description.getClassName();
      String methodName = description.getMethodName();
      File screenshotFile = new File(screenshotDir, "screenshot-" + timestamp + "-" + className + "." + methodName + ".png");

      try (FileOutputStream out = new FileOutputStream(screenshotFile)) {
        LOG.info("Test failed, took as screenshot: " + screenshotFile);
        out.write(((TakesScreenshot) m_driver).getScreenshotAs(OutputType.BYTES));
      }
    }
    catch (IOException e) { // NOSONAR
      LOG.error("Could not take a screenshot because of: {}", e.getMessage(), e);
    }
  }
}
