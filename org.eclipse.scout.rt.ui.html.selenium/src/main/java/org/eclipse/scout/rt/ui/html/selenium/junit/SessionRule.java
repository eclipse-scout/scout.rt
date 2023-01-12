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

import java.net.URL;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.eclipse.scout.rt.ui.html.selenium.annotation.UrlParams;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;

/**
 * Test rule to control login/logout for selenium tests.
 * <p>
 * This is <i>not</i> realized as @Before/@After methods, because this would not allow us to control the execution order
 * of the various test rules, namely the {@link ScreenshotRule}, which would take the screenshot too late (after
 * logout).
 */
public class SessionRule extends TestWatcher {

  private final WebDriver m_driver;

  public SessionRule(AbstractSeleniumTest test) {
    m_driver = test.getDriver();
  }

  @Override
  protected void starting(Description description) {
    // Before each test method, ensure a new session is created.
    URL webAppUrl = SeleniumUtil.getWebAppUrl();
    if (description.getTestClass().isAnnotationPresent(UrlParams.class)) {
      String urlParams = description.getTestClass().getAnnotation(UrlParams.class).value();
      if (!StringUtility.isNullOrEmpty(urlParams)) {
        UriBuilder builder = new UriBuilder(webAppUrl);
        builder.queryString(urlParams);
        webAppUrl = builder.createURL();
      }
    }
    m_driver.get(webAppUrl.toString());
  }

  @Override
  protected void finished(Description description) {
    // After each test-method, call the logout URL to destroy the current session
    // (without creating a new one automatically)
    m_driver.get(SeleniumUtil.getLogoutUrl().toString());
  }
}
