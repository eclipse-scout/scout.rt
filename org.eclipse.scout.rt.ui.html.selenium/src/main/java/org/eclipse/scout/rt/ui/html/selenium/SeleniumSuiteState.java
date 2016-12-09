/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
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
public class SeleniumSuiteState {

  private static boolean m_suiteStarted = false;

  private static WebDriver m_driver;

  public static void setUpBeforeClass() {
    m_suiteStarted = true;
    m_driver = SeleniumDriver.setUpDriver();
    System.out.println("Selenium driver started by SeleniumTestSuite");
  }

  public static void tearDownAfterClass() {
    m_suiteStarted = false;
    m_driver.quit();
    m_driver = null;
    System.out.println("Selenium driver terminated by SeleniumTestSuite");
  }

  public static boolean isSuiteStarted() {
    return m_suiteStarted;
  }

  public static WebDriver getDriver() {
    return m_driver;
  }

}
