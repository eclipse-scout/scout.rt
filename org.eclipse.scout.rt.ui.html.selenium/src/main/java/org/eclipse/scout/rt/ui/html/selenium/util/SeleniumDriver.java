/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.selenium.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
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
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumDriverSlowNetworkProperty;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumWebdriverPropertiesProperty;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumProperties.SeleniumWebdriverProperty;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SeleniumDriver {
  private static final Logger LOG = LoggerFactory.getLogger(SeleniumDriver.class);

  public static WebDriver setUpDriver() {
    return BEANS.get(SeleniumDriver.class).createDriver();
  }

  public WebDriver createDriver() {
    if (CONFIG.getPropertyValue(SeleniumWebdriverPropertiesProperty.class) != null) {
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

    try {
      RemoteWebDriver driver;
      if (StringUtility.hasText(getRemoteServerUrl())) {
        driver = createRemoteDriver();
      }
      else {
        driver = createLocalDriver();
      }

      driver.manage().timeouts().setScriptTimeout(10000, TimeUnit.SECONDS);
      // Set window size roughly to the minimal supported screen size
      // (1280x1024 minus some borders for browser toolbar and windows taskbar)
      driver.manage().window().setPosition(new Point(0, 0));
      driver.manage().window().setSize(new Dimension(getWindowWidth(), getWindowHeight()));

      if (BooleanUtility.nvl(CONFIG.getPropertyValue(SeleniumDriverSlowNetworkProperty.class))) {
        setSlowNetwork(driver);
      }

      Capabilities caps = driver.getCapabilities();
      LOG.info("Selenium driver configured with driver={} browser.name={} browser.version={}", driver.getClass().getName(), caps.getBrowserName(), caps.getVersion());
      return driver;
    }
    catch (SessionNotCreatedException e) {
      LOG.error("Failed to init driver ({})\n" +
          "* Most likely your Chrome browser version is not supported by the ChromeDriver version configured in the pom.xml.\n" +
          "* Update the properties 'chromedriver_base_url' and 'chromedriver_hash_*' in your local pom.xml to run Selenium tests in your browser, but don't commit that change.\n" +
          "* Look for a suitable ChromeDriver version here: https://chromedriver.storage.googleapis.com/index.html", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  protected int getWindowHeight() {
    return 900;
  }

  protected int getWindowWidth() {
    return 1200;
  }

  protected void loadDriverProperties() {
    Map<String, String> driverProperties = CONFIG.getPropertyValue(SeleniumWebdriverPropertiesProperty.class);
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
    return System.getProperty("webdriver.remote.server"); // loaded from SeleniumWebdriverPropertiesProperty
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
      System.out.println("Chrome driver executable not found at path: " + chromeDriver);
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
        .url(getRemoteServerUrl())
        .oneOf(getDriverOptions())
        .build();
  }

  protected RemoteWebDriver createLocalDriver() {
    String driver = ObjectUtility.nvl(CONFIG.getPropertyValue(SeleniumWebdriverProperty.class), SeleniumProperties.DRIVER_CHROME);
    switch (driver) {
      case SeleniumProperties.DRIVER_CHROME:
        // TODO [7.0] bsh: Remove workaround, when Chrome bug is fixed
        // <WORKAROUND> https://bugs.chromium.org/p/chromedriver/issues/detail?id=1552
        Map<String, String> env = new HashMap<>();
        env.put("LANG", "en_US.UTF-8");
        System.out.println("Using custom environment variables for driver: " + new JSONObject(env).toString(2));
        return new ChromeDriver(
            new Builder()
                .usingAnyFreePort()
                .withEnvironment(env) // <--
                .build(),
            getChromeOptions());
      //RemoteWebDriver driver = new ChromeDriver(options)
      // </WORKAROUND>
      case SeleniumProperties.DRIVER_FIREFOX:
        return new FirefoxDriver(getFirefoxOptions());
      case SeleniumProperties.DRIVER_EDGE:
        return new EdgeDriver(getEdgeOptions());
      default:
        throw new IllegalArgumentException("Unknown driver " + driver);
    }
  }

  protected MutableCapabilities getDriverOptions() {
    String driver = ObjectUtility.nvl(CONFIG.getPropertyValue(SeleniumWebdriverProperty.class), SeleniumProperties.DRIVER_CHROME);
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
    options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

    // Add command line arguments
    options.addArguments("--lang=en");
    options.addArguments("--verbose");
    options.addArguments("--remote-allow-origins=*");
    // With ChromeDriver v75 W3C mode was introduced. This breaks several existing tests, because of two reasons:
    // 1. all offsets are now calculated from the center of an element, and not from the upper-left corner
    // 2. copy command (CTRL + C) does not work anymore. This may be related to a bug in ChromeDriver, but the bugfix
    //    mentioned here does not seem to solve the problem (note: document.execCommand('copy') doesn't work either)
    //    See: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2975
    options.setExperimentalOption("w3c", false);
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
