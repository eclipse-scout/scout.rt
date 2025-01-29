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

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.remote.http.ClientConfig;

public class SeleniumProperties {

  public static final String DRIVER_CHROME = "chrome";
  public static final String DRIVER_EDGE = "edge";
  public static final String DRIVER_FIREFOX = "firefox";

  /**
   * One of {@link #DRIVER_CHROME}, {@link #DRIVER_EDGE}, {@link #DRIVER_FIREFOX}
   */
  public static class SeleniumDriverProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.driver";
    }

    @Override
    public String description() {
      return "Driver to use ('chrome', 'edge', 'firefox')";
    }
  }

  /**
   * Properties loaded as system properties. Will be used by selenium driver services. See constants in those services.
   *
   * @see ChromeDriverService
   * @see EdgeDriverService
   * @see FirefoxDriverService
   * @see ClientConfig#defaultConfig()
   */
  public static class SeleniumDriverConfigProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.driverConfig";
    }

    @Override
    public String description() {
      return "Possibility to set arbitrary selenium driver properties";
    }
  }

  /**
   * If remote server URL is set, then the driver connects to selenium driver instance usually deployed with docker
   * (<a href="https://github.com/SeleniumHQ/docker-selenium">docker-selenium</a>).
   * Usually, remote selenium driver is available at {@code http://localhost:4444}.
   */
  public static class SeleniumDriverRemoteServerUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.driverRemoteServerUrl";
    }

    @Override
    public String description() {
      return "If remote server URL is set, then the driver connects to selenium driver instance usually deployed with docker.";
    }
  }

  public static class SeleniumSimulateSlowNetworkProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.simulateSlowNetwork";
    }

    @Override
    public String description() {
      return "If set, unit tests simulate slow network";
    }
  }

  public static class SeleniumWebAppUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.webAppUrl";
    }

    @Override
    public String description() {
      return "URL to web application under test";
    }
  }

  public static class SeleniumWebQueryParamsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.webQueryParams";
    }

    @Override
    public String description() {
      return "Query params appended to web application under test";
    }
  }

  public static class SeleniumScreenshotOnFailureProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.screenshotOnFailure";
    }

    @Override
    public String description() {
      return "If set, then a screenshot is taken in case of a failed test";
    }
  }
}
