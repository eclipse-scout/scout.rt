/*
 * Copyright (c) 2010-2025 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.selenium;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractMapConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriverService;

public class SeleniumProperties {

  public static final String DRIVER_CHROME = "chrome";
  public static final String DRIVER_EDGE = "edge";
  public static final String DRIVER_FIREFOX = "firefox";

  /**
   * One of {@link #DRIVER_CHROME}, {@link #DRIVER_EDGE}, {@link #DRIVER_FIREFOX}
   */
  public static class SeleniumWebdriverProperty extends AbstractStringConfigProperty {

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
   * Properties loaded as system properties. Will be used by driver services
   *
   * @see ChromeDriverService
   * @see EdgeDriverService
   */
  public static class SeleniumWebdriverPropertiesProperty extends AbstractMapConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.webdriver";
    }

    @Override
    public String description() {
      return "Possibility to set arbitrary selenium driver properties";
    }
  }

  public static class SeleniumDriverSlowNetworkProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.driver.slow.network";
    }

    @Override
    public String description() {
      return "If set, unit tests simulate slow network";
    }

    @Override
    public Boolean getDefaultValue() {
      // support for legacy system property
      return System.getProperty("slow.network") != null;
    }
  }

  public static class SeleniumWebAppUrlProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.web.app.url";
    }

    @Override
    public String description() {
      return "Driver to use ('chrome', 'edge', 'firefox')";
    }

    @Override
    public String getDefaultValue() {
      // support for legacy system property
      return System.getProperty("web.app.url", "http://localhost:8082/");
    }
  }

  public static class SeleniumQueryParamsProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.query.params";
    }

    @Override
    public String description() {
      return "Driver to use ('chrome', 'edge', 'firefox')";
    }

    @Override
    public String getDefaultValue() {
      // support for legacy system property
      return System.getProperty("query.params", "debug=true");
    }
  }

  public static class SeleniumScreenshotOnFailureProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.selenium.screenshot.on.failure";
    }

    @Override
    public String description() {
      return "Driver to use ('chrome', 'edge', 'firefox')";
    }

    @Override
    public Boolean getDefaultValue() {
      // support for legacy system property
      return TypeCastUtility.castValue(System.getProperty("take.screenshot.on.failure"), boolean.class);
    }
  }
}
