package org.eclipse.scout.rt.ui.html.selenium.util;

import java.io.IOException;
import java.io.InputStream;

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
   *
   * @param test
   */
  public static void injectSeleniumScript(AbstractSeleniumTest test) {
    try (InputStream is = test.getClass().getClassLoader().getResourceAsStream("selenium.js")) {
      LOG.info("Found resource selenium.js " + (is != null));
      String js = IOUtility.readStringUTF8(is);
      String oneLineJs = js.replaceAll("(\n|\r\n)", " "); // remove line breaks (unix or windows)
      LOG.info("selenium.js bytes=" + js.length() + " content (one line)=" + oneLineJs);
      executeScript(test, "$('body').prependElement('<script>').text('" + oneLineJs + "');");
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
   *
   * @param test
   * @param groupBoxClass
   */
  public static void scrollGroupBoxToBottom(AbstractSeleniumTest test, Class<? extends AbstractGroupBox> groupBoxClass) {
    String modelClassSelector = SeleniumUtil.getModelClassCssSelector(groupBoxClass);
    String jQuerySelector = "$('div[" + modelClassSelector + "] > .group-box-body')";
    executeScript(test, "scout.selenium.scrollToBottom(" + jQuerySelector + ")");
  }

  /**
   * Simulates we have a touch device by calling <code>scout.selenium.setSupportsTouch(boolean)</code>.
   *
   * @param test
   * @param supportsTouch
   */
  public static void setSupportsTouch(AbstractSeleniumTest test, boolean supportsTouch) {
    executeScript(test, "scout.selenium.setSupportsTouch(" + Boolean.toString(supportsTouch) + ")");
  }

  /**
   * @param test
   * @return <code>window.scrollY</code>
   */
  public static Object getWindowScrollY(AbstractSeleniumTest test) {
    return executeScript(test, "return window.scrollY;");
  }

}
