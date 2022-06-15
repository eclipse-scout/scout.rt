/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.selenium.junit;

import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.shortPause;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class AbstractSeleniumTest {

  public static final int DEFAULT_WAIT_UNTIL_TIMEOUT = 10; // seconds

  private static WebDriver s_driver;

  private final IgnoreTestOnMacOSRule m_ignoreTestOnMacOSRule = new IgnoreTestOnMacOSRule(this);
  private final BrowserLogRule m_browserLogRule = new BrowserLogRule(this);
  private final SessionRule m_sessionRule = new SessionRule(this);
  private final ScreenshotRule m_screenshotRule = new ScreenshotRule(this);
  private final RetryOnFailureRule m_retryRule = new RetryOnFailureRule(this);

  /**
   * Wrap screenshot rule by session rule to make sure, screenshot is taken BEFORE the session is logged out.
   *
   * @see "https://github.com/junit-team/junit/issues/906"
   */
  @Rule
  public final TestRule m_mainRule = RuleChain
      .outerRule(m_ignoreTestOnMacOSRule)
      .around(m_retryRule)
      .around(m_browserLogRule)
      .around(m_sessionRule)
      .around(m_screenshotRule);

  private WebElement m_previousClickTarget;
  private int m_waitUntilTimeout = DEFAULT_WAIT_UNTIL_TIMEOUT; // seconds

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
      if (s_driver != null) {
        s_driver.quit();
      }
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
    return findInputField(SeleniumUtil.byModelClass(modelClass));
  }

  /**
   * Finds the first <code>input</code> {@link WebElement} that is found in the hierarchy referenced by the given
   * locator (by).
   */
  public WebElement findInputField(By by) {
    return findElement(by).findElement(By.tagName("input"));
  }

  /**
   * Finds the first <code>input</code> {@link WebElement} that represents the given model class, and which is located
   * somewhere beneath the given parent (e.g. text- or smart field).
   */
  public WebElement findInputField(WebElement parent, Class<?> modelClass) {
    return findElement(parent, modelClass).findElement(By.tagName("input"));
  }

  /**
   * Finds the first <code>text area</code> {@link WebElement} that represents the given model class.
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
    waitUntilDataRequestPendingDone();
    WebElement inputField = waitUntilInputFieldClickable(parent, modelClass);
    fillInputField(inputField, value);
  }

  /**
   * Fills in the given value into the given {@link WebElement}. The method does this:
   * <ul>
   * <li>Click on the field</li>
   * <li>Clear the field</li>
   * <li>Set the given value by sending keys and wait for pending requests</li>
   * <li>Tab out and wait for pending requests</li>
   * <li>Tab in again (Shift + Tab)</li>
   * </ul>
   */
  public void fillInputField(WebElement inputField, String value) {
    inputField.click();
    clearInput(inputField);
    inputField.sendKeys(value);
    shortPause();
    waitUntilDataRequestPendingDone();

    switchTo().activeElement().sendKeys(Keys.TAB);
    shortPause();
    waitUntilDataRequestPendingDone();

    switchTo().activeElement().sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));
  }

  public void waitUntilScoutSession() {
    waitUntil(SeleniumExpectedConditions.scriptToReturnTrue("return !!(scout.App.get() && scout.App.get().sessions && scout.App.get().sessions.length && scout.App.get().sessions[0])"));
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
    return waitUntilElementClickable(By.cssSelector(".form.view:not(.detail-form)"));
  }

  public WebElement waitUntilDetailForm() {
    return waitUntilElementClickable(By.cssSelector(".detail-form"));
  }

  public WebElement waitUntilDetailTable() {
    return waitUntilElementClickable(By.cssSelector(".detail-table"));
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
    return messageBox.findElement(By.xpath(String.format("//*[contains(@class, 'action unfocusable button box-button')][%s]", xPathIndex)));
  }

  public boolean elementNotExists(String modelClass) {
    return s_driver.findElements(SeleniumUtil.byModelClass(modelClass)).isEmpty();
  }

  public boolean elementNotExists(Class<?> modelClass) {
    return s_driver.findElements(SeleniumUtil.byModelClass(modelClass)).isEmpty();
  }

  public boolean elementExists(Class<?> modelClass) {
    return !this.elementNotExists(modelClass);
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

  public WebElement waitUntilElementClickable(WebElement element) {
    return waitUntil(ExpectedConditions.elementToBeClickable(element));
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

  public WebElement waitUntilTextAreaClickable(Class<?> modelClass) {
    return waitUntilTextAreaClickable(null, modelClass);
  }

  public WebElement waitUntilTextAreaClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.tagName("textarea"));
  }

  public WebElement waitUntilCheckBoxClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.className("check-box"));
  }

  public WebElement waitUntilButtonClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.className("button"));
  }

  public WebElement waitUntilLinkButtonClickable(WebElement parent, Class<?> modelClass) {
    return waitUntilElementClickable(parent, modelClass).findElement(By.className("link-button"));
  }

  public WebElement waitUntilMenuItemClickable(WebElement parent, String menuText) {
    return waitUntilElementClickable(parent, By.xpath("//div[contains(@class, 'menu-item')]/span[contains(@class, 'text') and contains(text(), '" + menuText + "')]/.."));
  }

  public WebElement waitUntilMenuItemClickable(String menuText) {
    return waitUntilMenuItemClickable(null, menuText);
  }

  public WebElement waitUntilTreeNodeClickable(WebElement parent, String nodeText) {
    return waitUntilElementClickable(parent, By.xpath("//div[contains(@class, 'tree-node')]/span[contains(@class, 'text') and contains(text(), '" + nodeText + "')]"));
  }

  public WebElement waitUntilTreeNodeClickable(String nodeText) {
    return waitUntilTreeNodeClickable(null, nodeText);
  }

  public WebElement waitUntilTableCellClickable(WebElement parent, String cellText) {
    return waitUntilElementClickable(parent, By.xpath("//div[contains(@class, 'table-row')]/div[contains(@class, 'table-cell')]/span[contains(@class, 'text') and contains(text(), '" + cellText + "')]"));
  }

  public WebElement waitUntilTableCellClickable(String cellText) {
    return waitUntilTableCellClickable(null, cellText);
  }

  /**
   * Waits for all pending server calls to finish.
   */
  public void waitUntilDataRequestPendingDone() {
    // Always wait a short time, because session.sendEvent() schedules the sending via setTimeout. When an
    // action is performed that causes a server request, this method might therefore not "see" the scheduled
    // server call yet.
    SeleniumUtil.pause(50, TimeUnit.MILLISECONDS);

    WebElement entryPoint = findElement(By.className("scout"));
    waitUntil(ExpectedConditions.not(SeleniumExpectedConditions.attributeToEqualsValue(entryPoint, "data-request-pending", "true")));
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

  protected <V> V waitUntil(Function<WebDriver, V> condition) {
    return waitUntil(condition, m_waitUntilTimeout);
  }

  protected <V> V waitUntil(Function<WebDriver, V> condition, int timeoutInSeconds) {
    return new WebDriverWait(s_driver, Duration.ofSeconds(timeoutInSeconds)).until(condition);
  }

  public void resetWaitUntilTimeout() {
    setWaitUntilTimeout(DEFAULT_WAIT_UNTIL_TIMEOUT);
  }

  /**
   * Sets the timeout in seconds which is used internally for every <i>waitUntil*()</i> call without explicit timeout.
   * Use this method in your test, when you must wait longer than the default timeout which is
   * {@value #DEFAULT_WAIT_UNTIL_TIMEOUT} seconds. At the end of your test you should reset the timeout to the default
   * value.
   */
  public void setWaitUntilTimeout(int timeoutInSeconds) {
    m_waitUntilTimeout = timeoutInSeconds;
  }

  /**
   * @return the timeout in seconds to be used by {@link #waitUntil(Function)}
   */
  public int getWaitUntilTimeout() {
    return m_waitUntilTimeout;
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

  /**
   * Performs select all on the given element. Since command-key + A does not work with ChromeDriver on OSX we execute
   * JavaScript on that platform. ChromeDriver developers don't seem to be motivated to fix that bug.
   *
   * @see "https://bugs.chromium.org/p/chromedriver/issues/detail?id=30"
   */
  public void selectAll(WebElement element) {
    if (SeleniumUtil.isMacOS()) {
      ((JavascriptExecutor) getDriver()).executeScript("arguments[0].select();", element);
    }
    else {
      Actions actions = new Actions(getDriver());
      actions.moveToElement(element).click().keyDown(getOsDependentCtrlKey()).sendKeys("a").keyUp(getOsDependentCtrlKey()).build().perform();
    }
  }

  public void copy(WebElement element) {
    Actions actions = new Actions(getDriver());
    actions.moveToElement(element).keyDown(getOsDependentCtrlKey()).sendKeys("c").keyUp(getOsDependentCtrlKey()).build().perform();
  }

  public void paste(WebElement element) {
    Actions actions = new Actions(getDriver());
    actions.moveToElement(element).keyDown(getOsDependentCtrlKey()).sendKeys("v").keyUp(getOsDependentCtrlKey()).build().perform();
  }

  public void doubleClickOnElement(WebElement element) {
    Actions actions = new Actions(getDriver());
    actions.moveToElement(element).doubleClick().perform();
  }

  /**
   * @return the parent element of the given element (by using findElement and xpath '..')
   */
  public WebElement findParentElement(WebElement element) {
    return element.findElement(By.xpath(".."));
  }

  /**
   * Sends a "click" event to the given element. If the same element is clicked twice in a row, a short delay is applied
   * to prevent generation of a "double click" event.
   * <p>
   * If the click caused a server call, the methods waits until the request has finished. It also waits for UI
   * animations using a .animation-wrapper to complete.
   */
  public void clickAndWait(WebElement element) {
    // If the same element is clicked twice in a row, add a small pause to prevent a double click
    if (m_previousClickTarget == element) {
      SeleniumUtil.pause(500, TimeUnit.MILLISECONDS);
    }
    m_previousClickTarget = element;

    element.click();

    // Wait for pending server calls to finish
    waitUntilDataRequestPendingDone();

    // Wait for animations to finish
    waitUntilAnimationWrapperDone();
  }

  /**
   * Waits until no elements with the CSS class <code>animation-wrapper</code> exist. Useful while testing tree node
   * expansion.
   */
  public void waitUntilAnimationWrapperDone() {
    for (WebElement animation : findElements(By.className("animation-wrapper"))) {
      waitUntilElementStaleness(animation);
    }
  }

  public void waitUntilWindowsCount(final int expectedWindowsCount) {
    waitUntil(webDriver -> {
      int numWindows = webDriver.getWindowHandles().size();
      if (numWindows == expectedWindowsCount) {
        return numWindows;
      }
      else {
        return null;
      }
    });
  }

  public void clearInput(WebElement input) {
    this.clearInput(input, false);
  }

  /**
   * Used as a replacement for WebElement#clear. Google changed the way clear() works in ChromeDriver > 2.43 and the
   * method now causes the field to lose focus, which triggers a lot of focus-out handlers that haven't called before
   * version 2.43. Thus, this method works with 'select all' and backspace. Additionally, it also triggers key/up down
   * events which is also a desired side effect of this method (clear() does not trigger key events).
   * <p>
   * Note: you can still use WebElement#clear if focus loss doesn't matter. But for a lot of widgets, especially
   * SmartFields it makes a difference.
   *
   * @param withPause
   *          whether a short pause should be made after the field has been cleared, this may be necessary for some
   *          SmartFields
   */
  public void clearInput(WebElement input, boolean withPause) {
    selectAll(input);
    input.sendKeys(Keys.BACK_SPACE);
    if (withPause) {
      SeleniumUtil.shortPause();
    }
  }

  /**
   * Fails when the given element does not contain all the given CSS classes.
   *
   * @param expectedCssClass
   *          A single CSS class-name or multiple CSS class-names separated by space. Example: <code>'menu-item'</code>
   *          or <code>'menu-item selected'</code>. If multiple CSS class-names are given, the given element must have
   *          all of these classes, otherwise the assertion will fail.
   */
  public void assertCssClass(WebElement element, String expectedCssClass) {
    waitUntil(SeleniumExpectedConditions.elementToHaveCssClass(element, expectedCssClass));
  }

  /**
   * Fails when the given element contains at least one of the given CSS classes.
   */
  public void assertCssClassNotExists(WebElement element, String expectedCssClass) {
    waitUntil(SeleniumExpectedConditions.elementNotToHaveCssClass(element, expectedCssClass));
  }

  /**
   * Hides the navigation and waits until it is gone.
   */
  public void hideDesktopNavigation() {
    findElement(By.cssSelector(".collapse-handle-body.left")).click();
    waitUntilElementClickable(By.cssSelector(".collapse-handle.both-visible"));
    findElement(By.cssSelector(".collapse-handle-body.left")).click();
    waitUntilElementStaleness(findElement(By.className("desktop-navigation")));
  }
}
