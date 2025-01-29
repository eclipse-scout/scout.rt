/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium;

import org.eclipse.scout.rt.platform.BEANS;
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
    s_driver = BEANS.get(SeleniumDriver.class).createDriver();
    s_suiteStarted = true;
    LOG.info("Selenium driver started by {}", SeleniumSuiteState.class.getSimpleName());
  }

  public static void tearDownAfterClass() {
    s_suiteStarted = false;
    if (s_driver != null) {
      s_driver.quit();
      s_driver = null;
    }
    LOG.info("Selenium driver terminated by {}", SeleniumSuiteState.class.getSimpleName());
  }

  public static boolean isSuiteStarted() {
    return s_suiteStarted;
  }

  public static WebDriver getDriver() {
    return s_driver;
  }
}
