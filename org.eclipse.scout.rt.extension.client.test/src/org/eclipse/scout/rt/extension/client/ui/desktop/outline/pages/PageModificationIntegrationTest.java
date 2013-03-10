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
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageAnchorFilter;
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
    Assert.assertNotNull(extensions);
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
    Assert.assertNotNull(extensions);
    for (PageModificationExtension ext : extensions) {
      if (ext.getPageClass() == C1ModificationPageWithNodes.class) {
        // desktop anchor and filters
        Assert.assertTrue(ext.getPageFilter() != null);
        Assert.assertSame(CompositePageFilter.class, ext.getPageFilter().getClass());
        //
        CompositePageFilter filter = (CompositePageFilter) ext.getPageFilter();
        Assert.assertEquals(3, filter.size());
        Assert.assertSame(PageAnchorFilter.class, filter.getFilters()[0].getClass());
        Assert.assertSame(APageFilter.class, filter.getFilters()[1].getClass());
        Assert.assertSame(BPageFilter.class, filter.getFilters()[2].getClass());
        //
        PageAnchorFilter anchor = (PageAnchorFilter) filter.getFilters()[0];
        Assert.assertNull(anchor.getOutlineFilterClass());
        Assert.assertSame(PageModificationNodePage.class, anchor.getParentPageFilterClass());
        //
        Assert.assertFalse(filter.accept(null, null, new P_Page()));
        Assert.assertTrue(filter.accept(null, new PageModificationNodePage(), new P_Page()));
      }
    }
  }

  @Test
  public void testExtensionsOnNodePage() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageModificationNodePage extensionPage = new PageModificationNodePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    Assert.assertSame(extensionPage, outline.getActivePage());
    //
    Assert.assertEquals(4, extensionPage.getChildNodeCount());
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
    Assert.assertSame(extensionPage, outline.getActivePage());
    extensionPage.ensureChildrenLoaded();
    outline.selectNode(extensionPage.getChildNode(0));
    //
    Assert.assertEquals(1, extensionPage.getChildNodeCount());
    assertModified(true, BModificationPageWithNodes.class, extensionPage.getChildPage(0));
  }

  @Test
  public void testExtensionsOnOutline() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageModificationOutline outline = new PageModificationOutline();
    ITreeNode rootNode = outline.getRootNode();
    Assert.assertNotNull(rootNode);
    //
    Assert.assertEquals(4, rootNode.getChildNodeCount());
    assertModified(true, AModificationPageWithNodes.class, rootNode.getChildNode(0));
    assertModified(false, BModificationPageWithNodes.class, rootNode.getChildNode(1));
    assertModified(true, C1ModificationPageWithNodes.class, rootNode.getChildNode(2));
    assertModified(true, C2ModificationPageWithNodes.class, rootNode.getChildNode(3));
  }

  private static void assertModified(boolean expected, Class<?> expectedType, ITreeNode page) {
    Assert.assertTrue(page instanceof IModifiablePage);
    Assert.assertTrue(expectedType.isInstance(page));
    Assert.assertEquals(expected, ((IModifiablePage) page).isModified());
  }

  private static class P_Page extends AbstractPageWithNodes {
  }
}
