/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.selenium.util;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.selenium.util.TextComparator.Contains;
import org.eclipse.scout.rt.ui.html.selenium.util.TextComparator.Equals;
import org.eclipse.scout.rt.ui.html.selenium.util.TextComparator.EqualsIgnoreCase;
import org.eclipse.scout.rt.ui.html.selenium.util.TextComparator.StartsWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * This class provides <code>ExceptedCondition</code>s used in Scout Selenium tests. It is used in addition to
 * Selenium's class {@link ExpectedConditions}.
 */
public final class SeleniumExpectedConditions {

  private SeleniumExpectedConditions() {
  }

  /**
   * Same as {@link ExpectedConditions#elementToBeClickable(By)} but with a parent element which is also resolved lazy.
   */
  public static ExpectedCondition<WebElement> childElementToBeClickable(final WebElement parent, final By locator) {
    return driver -> {
      List<WebElement> elements = parent.findElements(locator);
      if (elements.size() == 1) {
        return ExpectedConditions.elementToBeClickable(elements.get(0)).apply(driver);
      }
      else {
        return null;
      }
    };
  }

  /**
   * Used to wait until the given element is focused.
   */
  public static ExpectedCondition<Boolean> elementToBeFocused(final WebElement element) {
    return new ExpectedCondition<>() {
      @Override
      public Boolean apply(WebDriver driver) {
        try {
          WebElement activeElement = driver.switchTo().activeElement();
          return activeElement.equals(element);
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          return null;
        }
      }

      @Override
      public String toString() {
        return String.format("element '%s' is not focused, but should be", element);
      }
    };
  }

  /**
   * Used to wait until the given element is <em>not</em> focused anymore.
   */
  public static ExpectedCondition<Boolean> elementNotToBeFocused(final WebElement element) {
    return new ExpectedCondition<>() {
      @Override
      public Boolean apply(WebDriver driver) {
        try {
          WebElement activeElement = driver.switchTo().activeElement();
          return !activeElement.equals(element);
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          return null;
        }
      }

      @Override
      public String toString() {
        return String.format("element '%s' is still focused, but shouldn't", element);
      }
    };
  }

  /**
   * Used to wait until a radio-button with the given text inside the given group is checked.
   */
  public static ExpectedCondition<WebElement> radioButtonToBeChecked(final WebElement radioButtonGroup, final String radioButtonText) {
    return driver -> {
      List<WebElement> elements = radioButtonGroup.findElements(By.cssSelector(".field.checked > .label"));
      if (elements.size() == 1) {
        return elements.get(0);
      }
      return null;
    };
  }

  /**
   * Used to wait until a check-box inside the given field has the requested checked state (checked or unchecked).
   */
  public static ExpectedCondition<WebElement> checkBoxToBeChecked(final WebElement checkBoxField, final boolean checked) {
    return driver -> {
      String selector = checked ? ".checked" : ":not(.checked)";
      List<WebElement> elements = checkBoxField.findElements(By.cssSelector(".check-box" + selector));
      if (elements.size() == 1) {
        return elements.get(0);
      }
      return null;
    };
  }

  /**
   * Waits until the given element has the requested attribute name and contains the given value. Example: the attribute
   * "class" must contain the string "status".
   */
  public static ExpectedCondition<Boolean> attributeToContainValue(final WebElement element, final String attributeName, final String value) {
    return attributeToCompareValue(new Contains(), element, attributeName, value);
  }

  /**
   * Waits until the given element has the requested attribute name and exactly the given value. Example: the attribute
   * "class" must be equals "error-status".
   */
  public static ExpectedCondition<Boolean> attributeToEqualsValue(final WebElement element, final String attributeName, final String value) {
    return attributeToCompareValue(new Equals(), element, attributeName, value);
  }

  /**
   * Waits until the given element has the requested attribute name and starts with the given value. Example: the attribute
   * "class" must start with "error-".
   */
  public static ExpectedCondition<Boolean> attributeToStartsWithValue(final WebElement element, final String attributeName, final String value) {
    return attributeToCompareValue(new StartsWith(), element, attributeName, value);
  }

  /**
   * Waits until the given element has the requested attribute name and exactly the given value, ignoring case. Example:
   * the attribute "class" must be equals "error-status".
   */
  public static ExpectedCondition<Boolean> attributeToEqualsIgnoreCaseValue(final WebElement element, final String attributeName, final String value) {
    return attributeToCompareValue(new EqualsIgnoreCase(), element, attributeName, value);
  }

  private static ExpectedCondition<Boolean> attributeToCompareValue(final TextComparator comparator, final WebElement element, final String attributeName, final String value) {
    return new ExpectedCondition<>() {
      private String m_actualValue = null;

      @Override
      public Boolean apply(WebDriver driver) {
        try {
          m_actualValue = element.getAttribute(attributeName);
          if (m_actualValue == null) {
            return null;
          }
          else {
            return comparator.compare(value, m_actualValue);
          }
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          return null;
        }
      }

      @Override
      public String toString() {
        return String.format("element '%s' should have attribute '%s' with value '%s' using text-comparator '%s', but has value '%s'",
            element, attributeName, value, comparator.getClass().getSimpleName(), m_actualValue);
      }
    };
  }

  /**
   * Waits until the given element has the requested CSS class.
   */
  public static ExpectedCondition<Boolean> elementToHaveCssClass(final WebElement element, final String cssClass) {
    return new ExpectedCondition<>() {
      private String m_actualValue = null;

      @Override
      public Boolean apply(WebDriver driver) {
        try {
          m_actualValue = element.getAttribute("class");
          if (m_actualValue == null) {
            return null;
          }
          return hasCssClass(m_actualValue, cssClass);
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          return null;
        }
      }

      @Override
      public String toString() {
        return String.format("element '%s' should have class '%s', but has '%s'",
            element, cssClass, m_actualValue);
      }
    };
  }

  /**
   * Waits until the given element doesn't have the requested CSS class.
   */
  public static ExpectedCondition<Boolean> elementNotToHaveCssClass(final WebElement element, final String cssClass) {
    return new ExpectedCondition<>() {
      private String m_actualValue = null;

      @Override
      public Boolean apply(WebDriver driver) {
        try {
          m_actualValue = element.getAttribute("class");
          if (m_actualValue == null) {
            return null;
          }
          return notHasCssClass(m_actualValue, cssClass);
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          return null;
        }
      }

      @Override
      public String toString() {
        return String.format("element '%s' should not have class '%s', but has '%s'",
            element, cssClass, m_actualValue);
      }
    };
  }

  private static boolean hasCssClass(String cssClass, String expectedCssClass) throws AssertionError {
    if (cssClass == null || expectedCssClass == null) {
      return false;
    }
    String[] cssClasses = cssClass.split(" ");
    String[] expectedCssClasses = expectedCssClass.split(" ");
    return CollectionUtility.containsAll(Arrays.asList(cssClasses), expectedCssClasses);
  }

  private static boolean notHasCssClass(String cssClass, String expectedCssClass) throws AssertionError {
    if (cssClass == null || expectedCssClass == null) {
      return false;
    }
    String[] cssClasses = cssClass.split(" ");
    String[] expectedCssClasses = expectedCssClass.split(" ");
    for (String expectedCssClassPart : expectedCssClasses) {
      if (CollectionUtility.contains(Arrays.asList(cssClasses), expectedCssClassPart)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param parentElement
   *     if not null, findElement below the given parent, if null, findElements in document
   * @param divClass
   *     css class of child div
   * @return Number of child divs found by the expected condition
   */
  public static ExpectedCondition<List<WebElement>> containerToHaveNumberOfChildDivs(final WebElement parentElement, final String divClass, final int numDivs) {
    return new ExpectedCondition<>() {
      @Override
      public List<WebElement> apply(WebDriver driver) {
        try {
          By by = By.className(divClass);
          List<WebElement> childDivs = parentElement != null ? parentElement.findElements(by) : driver.findElements(by);
          if (numDivs == childDivs.size()) {
            return childDivs;
          }
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          // NOP
        }
        return null;
      }

      @Override
      public String toString() {
        return String.format("container should have %d " + divClass, numDivs);
      }
    };
  }

  /**
   * @param parentElement
   *     if not null, findElement below the given parent, if null, findElements in document
   * @return The table-rows found by the expected condition
   */
  public static ExpectedCondition<List<WebElement>> tableToHaveNumberOfRows(final WebElement parentElement, final int numRows) {
    return containerToHaveNumberOfChildDivs(parentElement, "table-row", numRows);
  }

  /**
   * @param parentElement
   *     if not null, findElement below the given parent, if null, findElements in document
   * @param rowText
   *     optional text of element table-row, compared with 'contains'. When rowText is null no text comparison is
   *     done and only the number of rows is checked.
   * @param numRows
   *     expected number of rows the table must have
   * @return The table-rows found by the expected condition
   */
  public static ExpectedCondition<List<WebElement>> tableToHaveNumberOfRows(final WebElement parentElement, final String rowText, final int numRows) {
    return new ExpectedCondition<>() {
      @Override
      public List<WebElement> apply(WebDriver driver) {
        try {
          By by = By.className("table-row");
          List<WebElement> tableRows = parentElement != null ? parentElement.findElements(by) : driver.findElements(by);
          // we must have exactly the requested count of rows (too many rows is as bad as too few rows)
          if (numRows != tableRows.size()) {
            return null;
          }
          // and each one of these rows must contain the given text
          if (rowText != null) {
            for (WebElement tableRow : tableRows) {
              if (!tableRow.getText().contains(rowText)) {
                return null;
              }
            }
          }
          return tableRows;
        }
        catch (StaleElementReferenceException e) { // NOSONAR
          // NOP
        }
        return null;
      }

      @Override
      public String toString() {
        return String.format("table should have %d rows with text '%s'", numRows, rowText);
      }
    };
  }

  public static ExpectedCondition<Boolean> scriptToReturnTrue(final String script, final Object... args) {
    return new ExpectedCondition<>() {
      @Override
      public Boolean apply(WebDriver driver) {
        if (!(driver instanceof JavascriptExecutor)) {
          throw new UnsupportedOperationException();
        }
        Object o = ((JavascriptExecutor) driver).executeScript(script, args);
        return o instanceof Boolean && ((Boolean) o).booleanValue();
      }

      @Override
      public String toString() {
        return "script should 'true': " + script;
      }
    };
  }
}
