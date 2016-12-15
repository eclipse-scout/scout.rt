/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.selenium.util;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for Selenium tests
 */
public class SeleniumUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SeleniumUtil.class);

  private static final String DEFAULT_WEB_APP_URL = "http://localhost:8082/"; // /?logging=1

  /**
   * DOM attribute as used by Scout widgets to identify the Scout Java model class.
   */
  private static final String ATTR_DATA_MODELCLASS = "data-modelclass";

  /**
   * Slow down factor is used on CI-server to prevent test failures because sometimes the machine is slow - thus we wait
   * longer on the CI-server than on the local server. By default the value is 0.1 which means, on the local machine we
   * wait only 10% of the time we wait on the CI-server. The CI-server uses 1.0 as wait-factor (see pom.xml)
   */
  private static final double SLOW_DOWN_FACTOR = 0.1;

  public static By byAttributeValue(String attribute, String value) {
    return byAttributeValue(attribute, value, null);
  }

  public static By byAttributeValue(String attribute, String value, String xPathIndex) {
    return By.xpath(String.format(".//*[@%s='%s'][%s]", attribute, value, ObjectUtility.nvl(xPathIndex, "1")));
  }

  /**
   * Returns an XPath selector that matches an element with a text which is equals the given text.
   */
  public static By byText(String text) {
    return By.xpath(".//*[text() = '" + text + "']");
  }

  /**
   * Returns an XPath selector that matches an element with a text which contains the given text.
   */
  public static By byContainsText(String text) {
    return By.xpath(".//*[contains(text(), '" + text + "')]");
  }

  /**
   * Returns an XPath selector that matches an element with the given CSS class and containing the given text.
   */
  public static By byCssClassAndContainsText(String cssClass, String text) {
    return byCssClassAndText(cssClass, "contains", text);
  }

  /**
   * Returns an XPath selector that matches an element with the given CSS class and starts with the given text.
   */
  public static By byCssClassAndStartsWithText(String cssClass, String text) {
    return byCssClassAndText(cssClass, "starts-with", text);
  }

  /**
   * Returns an XPath selector that matches an element with the given CSS class and containing the given text.
   */
  private static By byCssClassAndText(String cssClass, String findFunction, String text) {
    return By.xpath("//*[contains(@class, '" + cssClass + "') and " + findFunction + "(text(), '" + text + "')]");
  }

  /**
   * @param element
   *          Type of HTML element (input, button, etc.)
   * @return
   */
  public static By byElement(String element) {
    return By.xpath(element);
  }

  public static WebElement getParent(WebElement e) {
    return e.findElement(By.xpath(".."));
  }

  /**
   * @return An attribute from the given DOM element <em>or</em> when the attribute does not exist on the element, the
   *         same attribute from the parent of the given element (which is convenient for tests with form-fields).
   */
  public static String getAttribute(WebElement element, String attributeName) {
    // find attribute on element (buttons)
    String attributeValue = element.getAttribute(attributeName);
    if (attributeValue != null) {
      return attributeValue;
    }
    // if not found, check if the parent has the attribute (required for form-fields)
    return SeleniumUtil.getParent(element).getAttribute(attributeName);
  }

  public static String getAttributeModelClass(WebElement element) {
    return getAttribute(element, ATTR_DATA_MODELCLASS);
  }

  public static boolean isModelClass(WebElement element, Class<?> modelClass) {
    String modelClassStr = modelClass.getName();
    String modelClassAttr = getAttributeModelClass(element);
    return modelClassStr.equals(modelClassAttr);
  }

  /**
   * Sleep for 200 ms.
   */
  public static void shortPause() {
    SleepUtil.sleepSafe(200, TimeUnit.MILLISECONDS);
  }

  /**
   * Sleep for 1 second.
   */
  public static void pause() {
    SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
  }

  /**
   * Sleep for given number of seconds.
   */
  public static void pause(int seconds) {
    SleepUtil.sleepSafe(seconds, TimeUnit.SECONDS);
  }

  /**
   * Same as pause(TimeUnit, int), but waits longer on CI-server.
   */
  public static void variablePause(TimeUnit timeUnit, int duration) {
    duration = (int) ((double) timeUnit.toMillis(duration) * getSlowDownFactor());
    if (duration > 0) {
      SleepUtil.sleepSafe(duration, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Same as pause(int), but waits longer on CI-server.
   */
  public static void variablePause(int seconds) {
    variablePause(TimeUnit.SECONDS, seconds);
  }

  /**
   * @return a CSS selector for the given model class, with double quoted strings. Example:<br>
   *         <code>data-modelclass="com.bsiag.widgets.FooBar$MainBox"</code>
   */
  public static String getModelClassCssSelector(Class<?> modelClass) {
    return ATTR_DATA_MODELCLASS + "=\"" + modelClass.getName() + "\"";
  }

  public static By byModelClass(String modelClass) {
    return byModelClass(modelClass, null);
  }

  public static By byModelClass(String modelClass, String xPathIndex) {
    return byAttributeValue(ATTR_DATA_MODELCLASS, modelClass, xPathIndex);
  }

  public static By byModelClass(Class<?> modelClass) {
    return byModelClass(modelClass, null);
  }

  public static By byModelClass(Class<?> modelClass, String xPathIndex) {
    return byModelClass(modelClass.getName(), xPathIndex);
  }

  /**
   * Creates an Xpath expression that matches an element with the given model-class and an attribute that does NOT
   * contain the given value.
   */
  public static By byModelClassAttributeContainsNot(Class<?> modelClass, String attribute, String value) {
    return By.xpath(String.format("//*[@" + ATTR_DATA_MODELCLASS + "='%s' and contains(@%s, '%s') = false]",
        modelClass.getName(), attribute, value));
  }

  /**
   * Calls <code>element#sendKeys</code> with a little delay after each key-press (which gives a more human behavior
   * while typing into a text-field).
   */
  public static void sendKeysDelayed(WebElement element, String characters) {
    sendKeysOneByOne(element, characters, 100);
  }

  /**
   * Calls <code>element#sendKeys</code> but makes a single call for each character in the characters String.
   */
  public static void sendKeysOneByOne(WebElement element, String characters) {
    sendKeysOneByOne(element, characters, 0);
  }

  /**
   * Calls <code>element#sendKeys</code> but makes a single call for each character in the characters String. Between
   * each call the Thread is blocked for the given delay.
   */
  public static void sendKeysOneByOne(WebElement element, String characters, long delayMs) {
    for (int i = 0; i < characters.length(); i++) {
      element.sendKeys(String.valueOf(characters.charAt(i)));
      if (delayMs > 0) {
        SleepUtil.sleepSafe(delayMs, TimeUnit.MILLISECONDS);
      }
    }
  }

  public static void sendKeysDelayed(WebElement element, Keys... keys) {
    for (int i = 0; i < keys.length; i++) {
      element.sendKeys(keys[i]);
      SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    }
  }

  public static boolean takeScreenShotOnFailure() {
    return TypeCastUtility.castValue(System.getProperty("take.screenshot.on.failure"), boolean.class);
  }

  public static double getSlowDownFactor() {
    String slowDownFactor = System.getProperty("slow.down.factor");
    if (slowDownFactor != null) {
      try {
        return Double.parseDouble(slowDownFactor);
      }
      catch (NumberFormatException e) {
        LOG.warn("Failed to convert property slow.down.factor {} into a double", slowDownFactor);
      }
    }
    return SLOW_DOWN_FACTOR;
  }

  public static String getWebAppUrl() {
    String webAppUrl = System.getProperty("web.app.url");
    if (webAppUrl == null) {
      webAppUrl = DEFAULT_WEB_APP_URL;
    }
    return webAppUrl;
  }

  public static String getLogoutUrl() {
    return getWebAppUrl() + "logout";
  }

  /**
   * Use this method to get some debug-output for a WebElement. This is useful when you're debugging Selenium tests with
   * System.out or logging.
   */
  public static String debugElement(WebElement element) {
    if (element == null) {
      return "null";
    }
    else {
      String text = element.getText();
      if (text != null && text.length() > 250) {
        text = text.substring(0, 250) + "...";
      }
      return element.getTagName() + "[class='" + element.getAttribute("class")
          + "' " + ATTR_DATA_MODELCLASS + "='" + element.getAttribute(ATTR_DATA_MODELCLASS)
          + "' id='" + element.getAttribute("id")
          + "' style='" + element.getAttribute("style")
          + (text != null ? " ' text='" + text : "") +
          "']";
    }
  }

  /**
   * @return Whether or not the VM runs on Windows 8.x.
   */
  public static boolean isWindows8() {
    return StringUtility.containsString(System.getProperty("os.name"), "Windows 8");
  }

  public static boolean isMacOS() {
    return StringUtility.containsString(System.getProperty("os.name"), "Mac OS");
  }
}
