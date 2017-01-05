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
package org.eclipse.scout.rt.ui.html.selenium.util;

import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.byModelClass;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.ui.html.selenium.junit.AbstractSeleniumTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public final class GoToUtil {

  private GoToUtil() {
  }

  /**
   * Go to the given outline by selecting the given outline-view-button in the outline-switcher menu in the top-left
   * corner of the application.
   */
  public static void goToOutline(AbstractSeleniumTest test, Class<? extends IOutlineViewButton> outlineViewButtonClass, Class<? extends IOutline> outlineClass) {
    // When we're on another form-tab, we must click twice on the outline-switcher tab to open the menu
    if (!test.findElement(By.className("view-button-tab")).isDisplayed()) {
      Actions builder = new Actions(test.getDriver());
      builder.moveToElement(test.waitUntilElementClickable(By.className("view-button-box")), 5, 5).click().build().perform();
      SeleniumUtil.shortPause();
    }
    test.waitUntilElementClickable(By.className("view-button-tab")).click();
    SeleniumUtil.shortPause();

    // When popup is not open yet - click again on the outline-switcher tab
    if (test.findElements(By.className("popup")).isEmpty()) {
      test.waitUntilElementClickable(By.className("view-button-tab")).click();
      SeleniumUtil.shortPause();
    }

    test.waitUntilElementClickable(byModelClass(outlineViewButtonClass)).click();
    SeleniumUtil.shortPause();
    test.waitUntilElementClickable(outlineClass);
    SeleniumUtil.shortPause();
  }

  /**
   * Activates the view at the given index (1-based), and returns the activated view.
   */
  public static WebElement goToView(AbstractSeleniumTest test, int viewTabIndex) {
    WebElement viewTab = test.waitUntilElementClickable(By.cssSelector(String.format(".desktop-view-tab:nth-child(%s)", viewTabIndex)));
    test.clickAtOffset(viewTab, 15, 5); // cannot click in the middle of the view-tab because its sometimes overlaid with a dialog
    return test.waitUntilView();
  }

}
