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

import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.getAttributeModelClass;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumExpectedConditions;
import org.openqa.selenium.WebElement;

public final class SeleniumAssert {

  private SeleniumAssert() {
    super();
  }

  /**
   * Fails when the given element does not contain all the given CSS classes.
   *
   * @param expectedCssClass
   *     A single CSS class-name or multiple CSS class-names separated by space. Example: <code>'menu-item'</code>
   *     or <code>'menu-item selected'</code>. If multiple CSS class-names are given, the given element must have
   *     all of these classes, otherwise the assertion will fail.
   */
  public static void assertCssClass(AbstractSeleniumTest test, WebElement element, String expectedCssClass) {
    test.waitUntil(SeleniumExpectedConditions.elementToHaveCssClass(element, expectedCssClass));
  }

  /**
   * Fails when the given element contains at least one of the given CSS classes.
   */
  public static void assertCssClassNotExists(AbstractSeleniumTest test, WebElement element, String expectedCssClass) {
    test.waitUntil(SeleniumExpectedConditions.elementNotToHaveCssClass(element, expectedCssClass));
  }

  public static void assertInputFieldValue(AbstractSeleniumTest test, WebElement inputField, String expectedValue) {
    test.waitUntil(SeleniumExpectedConditions.attributeToEqualsValue(inputField, "value", expectedValue));
  }

  public static void assertInputFieldValueIgnoreCase(AbstractSeleniumTest test, WebElement inputField, String expectedValue) {
    test.waitUntil(SeleniumExpectedConditions.attributeToEqualsIgnoreCaseValue(inputField, "value", expectedValue));
  }

  public static void assertModelClass(WebElement element, Class<?> modelClass) {
    String modelClassStr = modelClass.getName();
    String modelClassAttr = getAttributeModelClass(element);
    if (!modelClassStr.equals(modelClassAttr)) {
      throw new AssertionError("expected data-modelclass='" + modelClassStr + "' actual='" + modelClassAttr + "'");
    }
  }

  public static void assertSameModelClass(WebElement elementA, WebElement elementB) {
    String modelClassA = StringUtility.emptyIfNull(getAttributeModelClass(elementA));
    String modelClassB = StringUtility.emptyIfNull(getAttributeModelClass(elementB));
    if (!modelClassA.equals(modelClassB)) {
      throw new AssertionError("expected element A+B to be the same but A was='" + modelClassA + " and B was='" + modelClassB + "'");
    }
  }

  public static void assertActiveElement(AbstractSeleniumTest test, WebElement element) {
    test.waitUntil(SeleniumExpectedConditions.elementToBeFocused(element));
  }

  public static void assertElementStaleness(AbstractSeleniumTest test, WebElement element) {
    assertTrue(test.waitUntilElementStaleness(element));
  }
}
