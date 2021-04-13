/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.selenium.util;

import static org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil.byModelClass;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.ui.html.selenium.junit.AbstractSeleniumTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public final class GoToUtil {

  private GoToUtil() {
  }

  /**
   * Go to the given outline by selecting the given outline-view-button in the outline-switcher menu in the top-left
   * corner of the application.
   */
  public static void goToOutline(AbstractSeleniumTest test, Class<? extends IOutlineViewButton> outlineViewButtonClass, Class<? extends IOutline> outlineClass) {
    test.waitUntilDataRequestPendingDone();
    test.waitUntilElementClickable(By.className("view-menu")).click();
    WebElement popup = test.waitUntilElementClickable(By.className("view-menu-popup"));

    test.waitUntilElementClickable(popup.findElement(byModelClass(outlineViewButtonClass))).click();
    test.waitUntilElementStaleness(popup);
    SeleniumUtil.shortPause();
    test.waitUntilDataRequestPendingDone();

    test.waitUntilElementClickable(outlineClass);
    SeleniumUtil.shortPause();
    test.waitUntilDataRequestPendingDone();
  }

  /**
   * Activates the view at the given index (1-based), and returns the activated view.
   */
  public static WebElement goToView(AbstractSeleniumTest test, int viewTabIndex) {
    test.waitUntilDataRequestPendingDone();
    WebElement viewTab = test.waitUntilElementClickable(By.cssSelector(String.format(".simple-tab:nth-child(%s)", viewTabIndex)));
    // Selecting by mouse may not always work (e.g. if a dialog covers the tab) -> select by keyboard
    test.switchTo().activeElement().sendKeys(Keys.chord(Keys.CONTROL, viewTabIndex + ""));
    test.waitUntilDataRequestPendingDone();
    test.assertCssClass(viewTab, "selected");
    return test.waitUntilView();
  }

}
