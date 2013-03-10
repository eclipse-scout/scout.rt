/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.action.menu.internal;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class MenuAnchorFilterTest {

  @Test
  public void testAcceptNullAnchorClass() {
    MenuAnchorFilter filter = new MenuAnchorFilter(null);
    Assert.assertFalse(filter.accept(null, null, null));
    Assert.assertFalse(filter.accept(new Object(), null, null));
    Assert.assertFalse(filter.accept(null, new Object(), null));
    Assert.assertTrue(filter.accept(new Object(), new Object(), null));
  }

  @Test
  public void testAcceptAnchorMenu() {
    MenuAnchorFilter filter = new MenuAnchorFilter(P_AnchorMenu.class);
    Assert.assertFalse(filter.accept(new Object(), new Object(), null));
    //
    Object anchorAndContainerMenu = new P_AnchorMenu();
    Assert.assertTrue(filter.accept(anchorAndContainerMenu, anchorAndContainerMenu, null));
    Assert.assertTrue(filter.accept(anchorAndContainerMenu, anchorAndContainerMenu, null));
    //
    Object wrongAnchorMenu = new P_OtherAnchorMenu();
    Assert.assertFalse(filter.accept(wrongAnchorMenu, wrongAnchorMenu, null));
    Assert.assertTrue(filter.accept(anchorAndContainerMenu, wrongAnchorMenu, null));
  }

  @Test
  public void testAcceptAnchorNodePage() {
    MenuAnchorFilter filter = new MenuAnchorFilter(P_AnchorPage.class);
    Assert.assertFalse(filter.accept(new Object(), new Object(), null));
    //
    Object anchorAndContainerNodePage = new P_AnchorPage();
    Assert.assertTrue(filter.accept(anchorAndContainerNodePage, anchorAndContainerNodePage, null));
    Assert.assertTrue(filter.accept(anchorAndContainerNodePage, anchorAndContainerNodePage, null));
    //
    Object wrongAnchorNodePage = new P_OtherAnchorPage();
    Assert.assertFalse(filter.accept(wrongAnchorNodePage, wrongAnchorNodePage, null));
    Assert.assertTrue(filter.accept(anchorAndContainerNodePage, wrongAnchorNodePage, null));
  }

  @Test
  public void testAcceptPolymorphAnchor() {
    MenuAnchorFilter filter = new MenuAnchorFilter(P_AbstractPolymorphAnchorPage.class);
    Object p1 = new P_PolymorphAnchorPage1();
    Assert.assertTrue(filter.accept(p1, p1, null));
    Assert.assertTrue(filter.accept(p1, p1, null));
    //
    Object p2 = new P_PolymorphAnchorPage2();
    Assert.assertTrue(filter.accept(p2, p2, null));
    Assert.assertTrue(filter.accept(p1, p2, null));
  }

  private static class P_AnchorMenu extends AbstractMenu {
  }

  private static class P_OtherAnchorMenu extends AbstractMenu {
  }

  private static class P_AnchorPage extends AbstractPageWithNodes {
  }

  private static class P_OtherAnchorPage extends AbstractPageWithNodes {
  }

  private abstract static class P_AbstractPolymorphAnchorPage extends AbstractPageWithNodes {
  }

  private static class P_PolymorphAnchorPage1 extends P_AbstractPolymorphAnchorPage {
  }

  private static class P_PolymorphAnchorPage2 extends P_AbstractPolymorphAnchorPage {
  }
}
