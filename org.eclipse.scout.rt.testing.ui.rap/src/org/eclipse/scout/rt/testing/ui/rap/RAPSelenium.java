package org.eclipse.scout.rt.testing.ui.rap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;

public class RAPSelenium extends WebDriverBackedSelenium {

  public RAPSelenium(WebDriver baseDriver, String baseUrl) {
    super(baseDriver, baseUrl);
//    JavascriptLibrary javascriptLibrary = new JavascriptLibrary();
//    ElementFinder elementFinder = new ElementFinder(javascriptLibrary);
//    AlertOverride alertOverride = new AlertOverride();
//    ((WebDriverCommandProcessor) commandProcessor).addMethod("qxClickAt", new QxClickAt(alertOverride, elementFinder));
  }

//  @Override
//  public void click(String locator) {
//    commandProcessor.doCommand("qxClickAt", new String[]{"id=" + locator,});
//  }
//
//  @Override
//  public String getText(String locator) {
//    return super.getText("id=" + locator);
//  }

  public void waitForElementPresent(String locator) {
    for (int second = 0;; second++) {
      if (second >= 60) System.out.println("timeout");
      try {
//        if (isElementPresent("id=" + locator)) break;
        if (isElementPresent(locator)) break;
      }
      catch (Exception e) {
      }
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void clickAndWait(String locator) {
    click(locator);
    try {
      Thread.sleep(1 * 1000);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
