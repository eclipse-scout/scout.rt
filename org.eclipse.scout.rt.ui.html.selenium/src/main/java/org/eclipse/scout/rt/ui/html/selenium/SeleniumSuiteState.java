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
package org.eclipse.scout.rt.ui.html.selenium;

import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumDriver;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This static helper is used to be integrated in a selenium test suite, as it allows to re-use the same web driver
 * instance over multiple tests. This makes the tests run faster, since starting/stopping the web driver is a slow
 * operation.
 */
public final class SeleniumSuiteState {
  private static final Logger LOG = LoggerFactory.getLogger(SeleniumSuiteState.class);

  private static boolean s_suiteStarted = false;
  private static WebDriver s_driver;

  private SeleniumSuiteState() {
  }

  public static void setUpBeforeClass() {
    try {
      s_driver = SeleniumDriver.setUpDriver();
    }
    catch (Exception e) {
      LOG.error("Failed to create Selenium driver instance. Reason:\n" +
          "<<<" + e.getMessage() + ">>>\n" +
          "Info: if there's a version mismatch between the browser and the web-driver you may need to " +
          "update the web-driver in your pom.xml (for Chrome check the 'chromedriver_base_url' property). " +
          "When Maven has trouble to update the web-driver on a system you should check if there are " +
          "running processes using the old web-driver binary and kill these processes first.", e);
      throw e;
    }
    s_suiteStarted = true;
    LOG.info("Selenium driver started by SeleniumTestSuite");
  }

  public static void tearDownAfterClass() {
    s_suiteStarted = false;
    if (s_driver != null) {
      s_driver.quit();
      s_driver = null;
    }
    LOG.info("Selenium driver terminated by SeleniumTestSuite");
  }

  public static boolean isSuiteStarted() {
    return s_suiteStarted;
  }

  public static WebDriver getDriver() {
    return s_driver;
  }
}
