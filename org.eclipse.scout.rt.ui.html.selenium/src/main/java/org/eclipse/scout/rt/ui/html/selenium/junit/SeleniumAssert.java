/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.selenium.junit;

import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.getAttributeModelClass;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumExpectedConditions;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public final class SeleniumAssert {

  private SeleniumAssert() {
    super();
  }

  /**
   * Fails when the given element does not contain all of the given CSS classes.
   *
   * @param element
   * @param expectedCssClass
   *          A single CSS class-name or multiple CSS class-names separated by space. Example: <code>'menu-item'</code>
   *          or <code>'menu-item selected'</code>. If multiple CSS class-names are given, the given element must have
   *          all of these classes, otherwise the assert will fail.
   */
  public static void assertCssClass(AbstractSeleniumTest test, WebElement element, String expectedCssClass) {
    test.waitUntil(SeleniumExpectedConditions.elementToHaveCssClass(element, expectedCssClass));
  }

  /**
   * Fails when the given element contains at least one of the given CSS classes.
   *
   * @param element
   * @param expectedCssClass
   */
  public static void assertCssClassNotExists(AbstractSeleniumTest test, WebElement element, String expectedCssClass) {
    test.waitUntil(ExpectedConditions.not(SeleniumExpectedConditions.elementToHaveCssClass(element, expectedCssClass)));
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

  public static void assertActiveElement(AbstractSeleniumTest test, WebElement expectedElement) {
    WebElement actualActiveElement = test.getDriver().switchTo().activeElement();
    if (!actualActiveElement.equals(expectedElement)) {
      throw new AssertionError(String.format("expected element=%s to be focused, but active element was=%s",
          SeleniumUtil.debugElement(expectedElement), SeleniumUtil.debugElement(actualActiveElement)));
    }
  }

  public static void assertElementStaleness(AbstractSeleniumTest test, WebElement element) {
    assertTrue(test.waitUntilElementStaleness(element));
  }

}
