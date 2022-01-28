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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.exec.OS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class SeleniumDriver {

  static final int WINDOW_HEIGHT = 900;
  static final int WINDOW_WIDTH = 1200;

  private SeleniumDriver() {
  }

  private static void logProperty(String property, String value) {
    System.out.println("set property '" + property + "': " + ObjectUtility.nvl(value, "[not set]"));
  }

  public static WebDriver setUpDriver() {
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
      chromeDriver.setExecutable(true);
    }

    System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, webdriverChromeDriver);
    logProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, webdriverChromeDriver);

    // log-file for web-driver
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File logFile = new File(tmpDir, "webdriver.log");
    String logFilePath = logFile.getAbsolutePath();
    System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFilePath);
    logProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, logFilePath);

    // set web-driver in verbose mode
    System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
    logProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

    // Prepare options
    ChromeOptions options = new ChromeOptions();
    String chromeBinary = System.getProperty("chrome.binary");
    logProperty("chrome.binary", chromeBinary);
    if (StringUtility.hasText(chromeBinary)) {
      options.setBinary(chromeBinary);
    }

    // Set logging preferences (see BrowserLogRule)
    LoggingPreferences logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

    // Add command line arguments
    options.addArguments("--lang=en");
    options.addArguments("--verbose");
    // With ChromeDriver v75 W3C mode was introduced. This breaks several existing tests, because of two reasons:
    // 1. all offsets are now calculated from the center of an element, and not from the upper-left corner anymore
    // 2. copy command (CTRL + C) does not work anymore. This may be related to a bug in ChromeDriver, but the bugfix
    //    mentioned here does not seem to solve the problem (note: document.execCommand('copy') doesn't work either)
    //    See: https://bugs.chromium.org/p/chromedriver/issues/detail?id=2975
    options.setExperimentalOption("w3c", false);
    // The following two lines are a replacement for --disable-infobars since this option
    // does not remove the "Chrome is being controlled..." info-bar anymore.
    // See: https://stackoverflow.com/questions/49169990/disable-infobars-argument-unable-to-hide-the-infobar-with-the-message-chrome-is
    options.setExperimentalOption("useAutomationExtension", false);
    options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

    // TODO [7.0] bsh: Remove workaround, when Chrome bug is fixed
    // <WORKAROUND> https://bugs.chromium.org/p/chromedriver/issues/detail?id=1552
    Map<String, String> env = new HashMap<>();
    env.put("LANG", "en_US.UTF-8");
    System.out.println("Using custom environment variables for driver: " + new JSONObject(env).toString(2));
    try {
      RemoteWebDriver driver = new ChromeDriver(
        new Builder()
          .usingAnyFreePort()
          .withEnvironment(env) // <--
          .build(),
        options);
      // RemoteWebDriver driver = new ChromeDriver(options)
      // </WORKAROUND>
      driver.manage().timeouts().setScriptTimeout(10000, TimeUnit.SECONDS);
      // Set window size roughly to the minimal supported screen size
      // (1280x1024 minus some borders for browser toolbar and windows taskbar)
      driver.manage().window().setPosition(new Point(0, 0));
      driver.manage().window().setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

      // Start unit tests with the following VM property to simulate slow network:
      // -Dslow.network=true
      if (System.getProperty("slow.network") != null) {
        setSlowNetwork(driver);
      }

      Capabilities caps = driver.getCapabilities();
      System.out.println("Selenium driver configured with driver=" + driver.getClass().getName()
        + " browser.name=" + caps.getBrowserName()
        + " browser.version=" + caps.getVersion());
      return driver;

    } catch (SessionNotCreatedException e) {
      System.out.println("* Most likely your Chrome browser version is not supported by the ChromeDriver version configured in the pom.xml.");
      System.out.println("* Update the properties 'chromedriver_base_url' and 'chromedriver_hash_*' in your local pom.xml to run Selenium tests in your browser, but don't commit that change.");
      System.out.println("* Look for a suitable ChromeDriver version here: http://chromedriver.storage.googleapis.com/index.html");
      throw new RuntimeException(e);
    }
  }

  /**
   * Set slow network conditions. You can do the same thing in the Chrome developer tools.
   */
  private static void setSlowNetwork(RemoteWebDriver driver) {
    Map<String, Object> map = new HashMap<>();
    map.put("offline", false);
    map.put("latency", 199); // ms
    map.put("download_throughput", 200 * 1024); // bytes
    map.put("upload_throughput", 50 * 1024); // bytes
    System.out.println("Simulate slow network conditions. Config=" + map);

    try {
      CommandExecutor executor = driver.getCommandExecutor();
      executor.execute(new Command(driver.getSessionId(), "setNetworkConditions", Collections.singletonMap("network_conditions", map)));
    }
    catch (IOException e) {
      System.err.println(e);
    }
  }
}
