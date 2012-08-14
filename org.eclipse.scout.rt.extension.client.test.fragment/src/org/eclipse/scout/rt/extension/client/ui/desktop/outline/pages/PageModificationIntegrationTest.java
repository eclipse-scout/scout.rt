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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.extension.client.Activator;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.APageFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.BPageFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.DynamicOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.AModificationPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.BModificationPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.C1ModificationPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.C2ModificationPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.IModifiablePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.PageModificationNodePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.PageModificationOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification.PageModificationTablePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.DesktopAnchorFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageModificationExtension;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageModificationIntegrationTest {

  @Test
  public void testDisabledExtensions() throws ProcessingException {
    PageExtensionManager manager = Activator.getDefault().getPagesExtensionManager();
    List<PageModificationExtension> extensions = manager.getPageModificationExtensions();
    assertNotNull(extensions);
    for (PageModificationExtension ext : extensions) {
      if (ext.getPageClass() == BModificationPageWithNodes.class && ext.getPageFilter() == null) {
        Assert.fail("Disabled pageModification is available for page " + BModificationPageWithNodes.class.getSimpleName());
      }
    }
  }

  @Test
  public void testFilters() throws ProcessingException {
    PageExtensionManager manager = Activator.getDefault().getPagesExtensionManager();
    List<PageModificationExtension> extensions = manager.getPageModificationExtensions();
    assertNotNull(extensions);
    for (PageModificationExtension ext : extensions) {
      if (ext.getPageClass() == C1ModificationPageWithNodes.class) {
        // desktop anchor and filters
        assertTrue(ext.getPageFilter() != null);
        assertSame(CompositePageFilter.class, ext.getPageFilter().getClass());
        //
        CompositePageFilter filter = (CompositePageFilter) ext.getPageFilter();
        assertEquals(3, filter.size());
        assertSame(DesktopAnchorFilter.class, filter.getFilters()[0].getClass());
        assertSame(APageFilter.class, filter.getFilters()[1].getClass());
        assertSame(BPageFilter.class, filter.getFilters()[2].getClass());
        //
        DesktopAnchorFilter anchor = (DesktopAnchorFilter) filter.getFilters()[0];
        assertNull(anchor.getOutlineFilterClass());
        assertSame(PageModificationNodePage.class, anchor.getParentPageFilterClass());
        //
        assertFalse(filter.accept(null, null, new P_Page()));
        assertTrue(filter.accept(null, new PageModificationNodePage(), new P_Page()));
      }
    }
  }

  @Test
  public void testExtensionsOnNodePage() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageModificationNodePage extensionPage = new PageModificationNodePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    assertSame(extensionPage, outline.getActivePage());
    //
    assertEquals(4, extensionPage.getChildNodeCount());
    assertModified(true, AModificationPageWithNodes.class, extensionPage.getChildPage(0));
    assertModified(false, BModificationPageWithNodes.class, extensionPage.getChildPage(1));
    assertModified(true, C1ModificationPageWithNodes.class, extensionPage.getChildPage(2));
    assertModified(true, C2ModificationPageWithNodes.class, extensionPage.getChildPage(3));
  }

  @Test
  public void testExtensionsOnTablePage() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageModificationTablePage extensionPage = new PageModificationTablePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    assertSame(extensionPage, outline.getActivePage());
    extensionPage.ensureChildrenLoaded();
    outline.selectNode(extensionPage.getChildNode(0));
    //
    assertEquals(1, extensionPage.getChildNodeCount());
    assertModified(true, BModificationPageWithNodes.class, extensionPage.getChildPage(0));
  }

  @Test
  public void testExtensionsOnOutline() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageModificationOutline outline = new PageModificationOutline();
    ITreeNode rootNode = outline.getRootNode();
    assertNotNull(rootNode);
    //
    assertEquals(4, rootNode.getChildNodeCount());
    assertModified(true, AModificationPageWithNodes.class, rootNode.getChildNode(0));
    assertModified(false, BModificationPageWithNodes.class, rootNode.getChildNode(1));
    assertModified(true, C1ModificationPageWithNodes.class, rootNode.getChildNode(2));
    assertModified(true, C2ModificationPageWithNodes.class, rootNode.getChildNode(3));
  }

  private void assertModified(boolean expected, Class<?> expectedType, ITreeNode page) {
    assertTrue(page instanceof IModifiablePage);
    assertTrue(expectedType.isInstance(page));
    assertEquals(expected, ((IModifiablePage) page).isModified());
  }

  private static class P_Page extends AbstractPageWithNodes {
  }
}
