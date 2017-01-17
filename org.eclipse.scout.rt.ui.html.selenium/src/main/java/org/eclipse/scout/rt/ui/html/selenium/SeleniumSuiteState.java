/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.selenium;

import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumDriver;
import org.openqa.selenium.WebDriver;

/**
 * This static helper is used to be integrated in a selenium test suite, as it allows to re-use the same web driver
 * instance over multiple tests. This makes the tests run faster, since starting/stopping the web driver is a slow
 * operation.
 */
public final class SeleniumSuiteState {

  private static boolean s_suiteStarted = false;
  private static WebDriver s_driver;

  private SeleniumSuiteState() {
  }

  public static void setUpBeforeClass() {
    s_suiteStarted = true;
    s_driver = SeleniumDriver.setUpDriver();
    System.out.println("Selenium driver started by SeleniumTestSuite");
  }

  public static void tearDownAfterClass() {
    s_suiteStarted = false;
    if (s_driver != null) {
      s_driver.quit();
      s_driver = null;
    }
    System.out.println("Selenium driver terminated by SeleniumTestSuite");
  }

  public static boolean isSuiteStarted() {
    return s_suiteStarted;
  }

  public static WebDriver getDriver() {
    return s_driver;
  }
}
