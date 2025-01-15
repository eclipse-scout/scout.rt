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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.selenium.junit.AbstractSeleniumTest;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class is used to call injected and execute test specific JavaScript code during Selenium test execution.
 *
 * @see src/test/resources/selenium.js
 */
public final class SeleniumJavaScript {

  private static final Logger LOG = LoggerFactory.getLogger(SeleniumJavaScript.class);

  private SeleniumJavaScript() {
  }

  /**
   * Injects the source of the <code>selenium.js</code> JavaScript file into the HTML document. After that you may call
   * the helper functions from selenium.js in your Selenium test-code by using the executeScript() function or by
   * calling the static methods of this class.
   */
  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  public static void injectSeleniumScript(AbstractSeleniumTest test) {
    test.waitUntilScoutSession();
    try (InputStream is = test.getClass().getClassLoader().getResourceAsStream("selenium.js")) {
      String js = IOUtility.readStringUTF8(is);
      String scriptAsJsString = "'" + js
          .replaceAll("\\r", "") // remove \r
          .replaceAll("\\n$", "") // remove trailing \n
          .replaceAll("\\\\", "\\\\\\\\") // escape backslashes
          .replaceAll("'", "\\\\'") // escape single quotes
          .replaceAll("\\n", "\\\\n' + \n'") + // split content at line breaks (but preserve them to allow single-line comments)
          "'";
      executeScript(test, "$('body').prependElement('<script>').text(" + scriptAsJsString + ");");
      LOG.info("Injected selenium.js");
    }
    catch (IOException e) {
      throw new ProcessingException("resource", e);
    }
  }

  public static Object executeScript(AbstractSeleniumTest test, String javaScript, Object... param) {
    if (test.getDriver() instanceof JavascriptExecutor) {
      JavascriptExecutor executor = (JavascriptExecutor) test.getDriver();
      return executor.executeScript(javaScript, param);
    }
    throw new UnsupportedOperationException();
  }

  /**
   * Scrolls the given (scrollable) group-box to the bottom by setting scrollTop. This method uses the injected
   * JavaScript method <code>scout.selenium.scrollTopBottom()</code>.
   */
  public static void scrollGroupBoxToBottom(AbstractSeleniumTest test, Class<? extends AbstractGroupBox> groupBoxClass) {
    test.waitUntilElementClickable(groupBoxClass);
    String modelClassSelector = SeleniumUtil.getModelClassCssSelector(groupBoxClass);
    String jQuerySelector = "$('div[" + modelClassSelector + "] > .group-box-body')";
    executeScript(test, "scout.selenium.scrollToBottom(" + jQuerySelector + ")");
  }

  public static void scrollTableToRight(AbstractSeleniumTest test, Class<? extends AbstractTable> tableClass) {
    test.waitUntilElementClickable(tableClass);
    String modelClassSelector = SeleniumUtil.getModelClassCssSelector(tableClass);
    String jQuerySelector = "$('div[" + modelClassSelector + "] > .table-data')";
    executeScript(test, "scout.selenium.scrollToRight(" + jQuerySelector + ")");
  }

  public static void scrollTreeToBottom(AbstractSeleniumTest test, Class<? extends AbstractTree> treeClass) {
    test.waitUntilElementClickable(treeClass);
    String modelClassSelector = SeleniumUtil.getModelClassCssSelector(treeClass);
    String jQuerySelector = "$('div[" + modelClassSelector + "] > .tree-data')";
    executeScript(test, "scout.selenium.scrollToBottom(" + jQuerySelector + ")");
  }

  /**
   * Simulates we have a touch device by calling <code>scout.selenium.setSupportsTouch(boolean)</code>.
   */
  public static void setSupportsTouch(AbstractSeleniumTest test, boolean supportsTouch) {
    executeScript(test, "scout.selenium.setSupportsTouch(" + supportsTouch + ")");
  }

  /**
   * @return <code>window.scrollY</code>
   */
  public static Object getWindowScrollY(AbstractSeleniumTest test) {
    return executeScript(test, "return window.scrollY;");
  }

  /**
   * Enables/Disables responsive mode.
   */
  public static void setResponsiveEnabled(AbstractSeleniumTest test, boolean enabled) {
    executeScript(test, "scout.ResponsiveManager.get().setActive(" + ((enabled) ? "true" : "false") + ");");
  }
}
