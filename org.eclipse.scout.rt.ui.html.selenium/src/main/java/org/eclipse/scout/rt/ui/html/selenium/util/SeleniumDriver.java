/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.exec.OS;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumDriverConfigProperty;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumDriverProperty;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumDriverRemoteServerUrlProperty;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumSimulateSlowNetworkProperty;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SeleniumDriver {
  private static final Logger LOG = LoggerFactory.getLogger(SeleniumDriver.class);

  public static WebDriver setUpDriver() {
    return BEANS.get(SeleniumDriver.class).createDriver();
  }

  public WebDriver createDriver() {
    if (CONFIG.getPropertyValue(SeleniumDriverConfigProperty.class) != null) {
      loadDriverProperties();
    }
    else {
      legacySetup();
    }

    // ensure proxy properties do not contain an empty string
    String proxyHostProperty = "http.proxyHost";
    String proxyHost = System.getProperty(proxyHostProperty);
    if (!StringUtility.hasText(proxyHost)) {
      System.clearProperty(proxyHostProperty);
    }
    String proxyPortProperty = "http.proxyPort";
    String proxyPort = System.getProperty(proxyPortProperty);
    if (!StringUtility.hasText(proxyPort)) {
      System.clearProperty(proxyPortProperty);
    }

    // If remote server URL is set, then the driver connects to selenium driver instance usually deployed with docker
    // see also https://github.com/SeleniumHQ/docker-selenium
    RemoteWebDriver driver;
    if (StringUtility.hasText(getRemoteServerUrl())) {
      driver = createRemoteDriver();
    }
    else {
      driver = createLocalDriver();
    }

    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10000));
    // Set window size roughly to the minimal supported screen size
    // (1280x1024 minus some borders for browser toolbar and windows taskbar)
    driver.manage().window().setPosition(new Point(0, 0));
    driver.manage().window().setSize(new Dimension(getWindowWidth(), getWindowHeight()));

    if (BooleanUtility.nvl(CONFIG.getPropertyValue(SeleniumSimulateSlowNetworkProperty.class))) {
      setSlowNetwork(driver);
    }

    Capabilities caps = driver.getCapabilities();
    LOG.info("Selenium driver configured with driver={} browser.name={} browser.version={}", driver.getClass().getName(), caps.getBrowserName(), caps.getBrowserVersion());
    return driver;
  }

  protected int getWindowHeight() {
    return 900;
  }

  protected int getWindowWidth() {
    return 1200;
  }

  protected void loadDriverProperties() {
    Map<String, String> driverProperties = CONFIG.getPropertyValue(SeleniumDriverConfigProperty.class);
    if (CollectionUtility.hasElements(driverProperties)) {
      for (Entry<String, String> entry : driverProperties.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (StringUtility.hasText(key) && StringUtility.hasText(value)) {
          System.setProperty(key, value);
        }
      }
    }
  }

  protected String getRemoteServerUrl() {
    return CONFIG.getPropertyValue(SeleniumDriverRemoteServerUrlProperty.class);
  }

  protected ClientConfig getClientConfig() {
    try {
      return ClientConfig.defaultConfig().baseUrl(new URL(getRemoteServerUrl()));
    }
    catch (MalformedURLException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  /**
   * Old legacy code which will be used if no scout selenium config properties are set.
   */
  protected void legacySetup() {
    // web-driver executable
    String webdriverChromeDriver = System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
    if (StringUtility.isNullOrEmpty(webdriverChromeDriver)) {
      webdriverChromeDriver = OS.isFamilyWindows() ? "/seleniumDrivers/chromedriver.exe" : "/seleniumDrivers/chromedriver";
    }

    File chromeDriver = new File(webdriverChromeDriver);
    if (!chromeDriver.exists()) {
      LOG.info("Chrome driver executable not found at path: {}", chromeDriver);
      URL webdriverChromeDriverResource = SeleniumDriver.class.getResource(webdriverChromeDriver);
      if (webdriverChromeDriverResource != null) {
        chromeDriver = new File(webdriverChromeDriverResource.getFile());
        webdriverChromeDriver = chromeDriver.getAbsolutePath();
      }
    }
    if (!StringUtility.matches(webdriverChromeDriver, ".+\\.exe", Pattern.CASE_INSENSITIVE) && chromeDriver.exists() && !chromeDriver.canExecute()) {
      boolean success = chromeDriver.setExecutable(true);
      if (!success) {
        throw new PlatformException("Error making '{}' executable.", chromeDriver);
      }
    }

    System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, webdriverChromeDriver);
    LOG.info("set property '" + ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY + "': " + webdriverChromeDriver);

    // log-file for web-driver
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File logFile = new File(tmpDir, "webdriver.log");
    String logFilePath = logFile.getAbsolutePath();
    System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFilePath);
    LOG.info("set property '" + ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY + "': " + logFilePath);

    // set web-driver in verbose mode
    System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
  }

  protected RemoteWebDriver createRemoteDriver() {
    return (RemoteWebDriver) RemoteWebDriver.builder()
        .oneOf(getDriverOptions())
        .config(getClientConfig())
        .build();
  }

  protected RemoteWebDriver createLocalDriver() {
    return (RemoteWebDriver) RemoteWebDriver.builder()
        .oneOf(getDriverOptions())
        .build();
  }

  protected MutableCapabilities getDriverOptions() {
    String driver = ObjectUtility.nvl(CONFIG.getPropertyValue(SeleniumDriverProperty.class), SeleniumProperties.DRIVER_CHROME);
    switch (driver) {
      case SeleniumProperties.DRIVER_CHROME:
        return getChromeOptions();
      case SeleniumProperties.DRIVER_FIREFOX:
        return getFirefoxOptions();
      case SeleniumProperties.DRIVER_EDGE:
        return getEdgeOptions();
      default:
        throw new IllegalArgumentException("Unknown driver " + driver);
    }
  }

  protected ChromeOptions getChromeOptions() {
    ChromeOptions options = new ChromeOptions();

    // Set logging preferences (see BrowserLogRule)
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    options.setCapability(ChromeOptions.LOGGING_PREFS, logPrefs);

    // Add command line arguments
    options.addArguments("--lang=en");
    options.addArguments("--verbose");
    options.addArguments("--remote-allow-origins=*");
    // The following two lines are a replacement for --disable-infobars since this option
    // does not remove the "Chrome is being controlled..." info-bar anymore.
    // See: https://stackoverflow.com/questions/49169990/disable-infobars-argument-unable-to-hide-the-infobar-with-the-message-chrome-is
    options.setExperimentalOption("useAutomationExtension", false);
    options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
    return options;
  }

  protected FirefoxOptions getFirefoxOptions() {
    return new FirefoxOptions();
  }

  protected EdgeOptions getEdgeOptions() {
    return new EdgeOptions();
  }

  /**
   * Set slow network conditions. You can do the same thing in the Chrome developer tools.
   */
  protected void setSlowNetwork(RemoteWebDriver driver) {
    Map<String, Object> map = new HashMap<>();
    map.put("offline", false);
    map.put("latency", 199); // ms
    map.put("download_throughput", 200 * 1024); // bytes
    map.put("upload_throughput", 50 * 1024); // bytes
    LOG.info("Simulate slow network conditions. Config={}", map);

    try {
      CommandExecutor executor = driver.getCommandExecutor();
      executor.execute(new Command(driver.getSessionId(), "setNetworkConditions", Collections.singletonMap("network_conditions", map)));
    }
    catch (IOException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }
}
