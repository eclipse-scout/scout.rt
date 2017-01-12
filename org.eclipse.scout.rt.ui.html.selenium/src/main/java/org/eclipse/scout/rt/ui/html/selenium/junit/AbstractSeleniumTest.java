/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.selenium.junit;

import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.shortPause;
import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.variablePause;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.ui.html.selenium.SeleniumSuiteState;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumDriver;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumExpectedConditions;
import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public abstract class AbstractSeleniumTest {

  private static WebDriver s_driver;

  private final IgnoreTestOnMacOSRule m_ignoreTestOnMacOSRule = new IgnoreTestOnMacOSRule(this);
  private final SessionRule m_sessionRule = new SessionRule(this);
  private final ScreenshotRule m_screenshotRule = new ScreenshotRule(this);

  /**
   * Wrap screenshot rule by session rule to make sure, screenshot is taken BEFORE the session is logged out.
   *
   * @see https://github.com/junit-team/junit/issues/906
   */
  @Rule
  public final TestRule m_mainRule = RuleChain.outerRule(m_ignoreTestOnMacOSRule)
      .around(m_sessionRule)
      .around(m_screenshotRule);

  /**
   * When we're running in a suite, the suite deals with starting the Driver, When the test runs alone, it must start
   * the Driver itself.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    if (SeleniumSuiteState.isSuiteStarted()) {
      s_driver = SeleniumSuiteState.getDriver();
    }
    else {
      s_driver = SeleniumDriver.setUpDriver();
      System.out.println("Selenium driver started by AbstractSeleniumTest");
    }
  }

  /**
   * When we're running in a suite, the suite deals with stopping the Driver, When the test runs alone, it must stop the
   * Driver itself.
   */
  @AfterClass
  public static void tearDownAfterClass() {
    if (!SeleniumSuiteState.isSuiteStarted()) {
      s_driver.quit();
      System.out.println("Selenium driver terminated by AbstractSeleniumTest");
    }
    s_driver = null;
  }

  public WebDriver getDriver() {
    return s_driver;
  }

  /**
   * Reloads the page (Ctrl + R).
   */
  public void refresh() {
    s_driver.navigate().refresh();
  }

  public TargetLocator switchTo() {
    return s_driver.switchTo();
  }

  public WebElement findElement(By by) {
    return findElement(null, by);
  }

  public List<WebElement> findElements(By by) {
    return findElements(null, by);
  }

  public List<WebElement> findElements(WebElement parent, By by) {
    if (parent == null) {
      return s_driver.findElements(by);
    }
    else {
      return parent.findElements(by);
    }
  }

  public WebElement findElement(WebElement parent, By by) {
    if (parent == null) {
      return s_driver.findElement(by);
    }
    else {
      return parent.findElement(by);
    }
  }

  /**
   * Finds the first {@link WebElement} that represents the given model class.
   */
  public WebElement findElement(Class<?> modelClass) {
    return findElement(null, modelClass);
  }

  /**
   * Finds the first {@link WebElement} that represents the given model class, and which is located somewhere beneath
   * the given parent.
   */
  public WebElement findElement(WebElement parent, Class<?> modelClass) {
    return findElement(parent, SeleniumUtil.byModelClass(modelClass));
  }

  /**
   * Finds the first <code>input</code> {@link WebElement} that represents the given model class (e.g. text- or smart
   * field).
   */
  public WebElement findInputField(Class<?> modelClass) {
    return findInputField(null, modelClass);
  }

  /**
   * Finds the first <code>input</code> {@link WebElement} that represents the given model class, and which is located
   * somewhere beneath the given parent (e.g. text- or smart field).
   */
  public WebElement findInputField(WebElement parent, Class<?> modelClass) {
    return findElement(parent, modelClass).findElement(By.tagName("input"));
  }

  /**
   * Finds the first <code>texst area</code> {@link WebElement} that represents the given model class.
   */
  public WebElement findTextArea(Class<?> modelClass) {
    return findTextArea(null, modelClass);
  }

  /**
   * Finds the first <code>text area</code> {@link WebElement} that represents the given model class, and which is
   * located somewhere beneath the given parent.
   */
  public WebElement findTextArea(WebElement parent, Class<?> modelClass) {
    return findElement(parent, modelClass).findElement(By.tagName("textarea"));
  }

  /**
   * Fills the given value into the first <code>input</code> {@link WebElement} that represents the given model class.
   * In turn, that field is the focus owner.
   */
  public void fillInputField(Class<? extends IValueField<?>> modelClass, String value) {
    fillInputField(null, modelClass, value);
  }

  /**
   * Fills the given value into the first <code>input</code> {@link WebElement} that represents the given model class,
   * and which is located somewhere beneath the given parent. In turn, that field is the focus owner.
   */
  public void fillInputField(WebElement parent, Class<? extends IValueField<?>> modelClass, String value) {
    WebElement inputField = waitUntilInputFieldClickable(parent, modelClass);
    fillInputField(inputField, value);
  }

  /**
   * Fills in the given value into the given {@link WebElement}.
   */
  public void fillInputField(WebElement inputField, String value) {
    inputField.click();
    variablePause(1);
    inputField.clear();
    variablePause(2);
    inputField.sendKeys(value);
    variablePause(2);
    switchTo().activeElement().sendKeys(Keys.TAB);
    variablePause(2);
    switchTo().activeElement().sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));
    shortPause();
  }

  /**
   * Finds the message box top on stack.
   */
  public WebElement waitUntilMessageBox() {
    return waitUntilMessageBox("last()");
  }

  /**
   * Finds the message box at the given DOM position (1-based).
   */
  public WebElement waitUntilMessageBox(String xPathIndex) {
    return waitUntilElementClickable(SeleniumUtil.byModelClass(MessageBox.class, xPathIndex));
  }

  /**
   * Finds the dialog top on stack.
   */
  public WebElement waitUntilDialog() {
    return waitUntilDialog("last()");
  }

  /**
   * Finds the dialog at the given DOM position (1-based).
   */
  public WebElement waitUntilDialog(String xPathIndex) {
    return waitUntilElementClickable(By.xpath(String.format("//div[contains(@class, 'dialog')][%s]", xPathIndex)));
  }

  /**
   * Finds the current form-view (does not match detail-forms!).
   */
  public WebElement waitUntilView() {
    return waitUntilElementClickable(By.cssSelector(".form.view"));
  }

  /**
   * Finds the message box button at the given index position (1-based, or last() to get the last button).
   */
  public WebElement findFirstMessageBoxButton(WebElement messageBox) {
    return findMessageBoxButton(messageBox, "1");
  }

  /**
   * Finds the message box button at the given index position (1-based, or last() to get the last opened).
   */
  public WebElement findMessageBoxButton(WebElement messageBox, String xPathIndex) {
    return messageBox.findElement(By.xpath(String.format("//*[@class='box-button unfocusable'][%s]", xPathIndex)));
  }

  public boolean elementNotExists(String modelClass) {
    return s_driver.findElements(SeleniumUtil.byModelClass(modelClass)).isEmpty();
  }

  public boolean elementNotExists(Class<?> modelClass) {
    return s_driver.findElements(SeleniumUtil.byModelClass(modelClass)).isEmpty();
  }

  public boolean waitUntilElementStaleness(WebElement element) {
    return waitUntil(ExpectedConditions.stalenessOf(element));
  }

  public WebElement waitUntilElementClickable(Class<?> modelClass) {
    return waitUntilElementClickable(null, modelClass);
  }

  public WebElement waitUntilElementClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, SeleniumUtil.byModelClass(modelClass));
  }

  public WebElement waitUntilElementClickable(By by) {
    return waitUntilElementClickable(null, by);
  }

  public WebElement waitUntilElementClickable(WebElement parent, By locator) {
    if (parent == null) {
      return waitUntil(ExpectedConditions.elementToBeClickable(locator));
    }
    else {
      return waitUntil(SeleniumExpectedConditions.childElementToBeClickable(parent, locator));
    }
  }

  public WebElement waitUntilInputFieldClickable(Class<?> modelClass) {
    return waitUntilInputFieldClickable(null, modelClass);
  }

  public WebElement waitUntilInputFieldClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.tagName("input"));
  }

  protected WebElement waitUntilTextAreaClickable(Class<?> modelClass) {
    return waitUntilTextAreaClickable(null, modelClass);
  }

  protected WebElement waitUntilTextAreaClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.tagName("textarea"));
  }

  public WebElement waitUntilCheckBoxClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.className("check-box"));
  }

  public void clickCheckBox(WebElement parent, Class<?> modelClass) {
    clickCheckBox(parent.findElement(SeleniumUtil.byModelClass(modelClass)));
  }

  public void clickCheckBox(Class<?> modelClass) {
    clickCheckBox(findElement(SeleniumUtil.byModelClass(modelClass)));
  }

  public void clickCheckBox(WebElement checkBoxField) {
    findElement(checkBoxField, By.className("check-box")).click();
  }

  /**
   * Waits until the given check-box has the requested checked state.
   */
  public WebElement waitUntilCheckBoxChecked(WebElement checkBoxField, boolean checked) {
    return waitUntil(SeleniumExpectedConditions.checkBoxToBeChecked(checkBoxField, checked));
  }

  /**
   * Waits until the given check-box is checked.
   */
  public WebElement waitUntilCheckBoxChecked(WebElement checkBoxField) {
    return waitUntilCheckBoxChecked(checkBoxField, true);
  }

  public <V> V waitUntil(Function<WebDriver, V> isTrue) {
    return new WebDriverWait(s_driver, 10).until(isTrue);
  }

  public WebElement waitForFirstTableRow() {
    String xpath = "//div[contains(@class, 'table-row')][1]";
    return waitUntilElementClickable(By.xpath(xpath));
  }

  /**
   * Waits until a radio-button within the given radio button group is checked. The button is identified by its text.
   */
  public WebElement waitUntilRadioButtonChecked(WebElement radioButtonGroup, String radioButtonText) {
    return waitUntil(SeleniumExpectedConditions.radioButtonToBeChecked(radioButtonGroup, radioButtonText));
  }

  /**
   * Clicks the element at the given offset position (from top-left corner).
   */
  public void clickAtOffset(WebElement element, int xOffset, int yOffset) {
    Actions builder = new Actions(getDriver());
    builder.moveToElement(element, xOffset, yOffset).click().build().perform();
  }

  public Set<String> getWindowHandles() {
    return s_driver.getWindowHandles();
  }

  public String getWindowHandle() {
    return s_driver.getWindowHandle();
  }

  /**
   * @return the OS dependent "control key" for use in key combinations like <code>Ctrl-C</code> ({@link Keys#COMMAND}
   *         on Mac OS X, {@link Keys#CONTROL} on all other systems).
   */
  public Keys getOsDependentCtrlKey() {
    // MacOS uses command key instead of ctrl
    Keys ctrlKey = Keys.CONTROL;
    if (SeleniumUtil.isMacOS()) {
      ctrlKey = Keys.COMMAND;
    }
    return ctrlKey;
  }
}
