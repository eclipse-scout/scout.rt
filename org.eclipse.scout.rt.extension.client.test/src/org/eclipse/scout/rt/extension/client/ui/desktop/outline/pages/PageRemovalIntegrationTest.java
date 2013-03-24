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
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.AbstractCRemovePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.BRemovePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.C2RemovePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.PageRemoveNodePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.PageRemoveOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageAnchorFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageRemoveExtension;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageRemovalIntegrationTest {

  @Test
  public void testDisabledExtensions() throws ProcessingException {
    PageExtensionManager manager = Activator.getDefault().getPagesExtensionManager();
    List<PageRemoveExtension> extensions = manager.getPageRemovalExtensions();
    Assert.assertNotNull(extensions);
    for (PageRemoveExtension ext : extensions) {
      if (ext.getPageClass() == BRemovePageWithNodes.class && ext.getPageFilter() == null) {
        Assert.fail("Disabled pageRemoval is available for page " + BRemovePageWithNodes.class.getSimpleName());
      }
    }
  }

  @Test
  public void testFilters() throws ProcessingException {
    PageExtensionManager manager = Activator.getDefault().getPagesExtensionManager();
    List<PageRemoveExtension> extensions = manager.getPageRemovalExtensions();
    Assert.assertNotNull(extensions);
    for (PageRemoveExtension ext : extensions) {
      if (ext.getPageClass() == AbstractCRemovePageWithNodes.class) {
        // filters only
        Assert.assertTrue(ext.getPageFilter() != null);
        Assert.assertSame(CompositePageFilter.class, ext.getPageFilter().getClass());
        //
        CompositePageFilter filter = (CompositePageFilter) ext.getPageFilter();
        Assert.assertEquals(2, filter.size());
        Assert.assertSame(APageFilter.class, filter.getFilters()[0].getClass());
        Assert.assertSame(BPageFilter.class, filter.getFilters()[1].getClass());
        //
        Assert.assertTrue(filter.accept(null, null, new P_Page()));
      }
      else if (ext.getPageClass() == C2RemovePageWithNodes.class) {
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
        Assert.assertSame(BRemovePageWithNodes.class, anchor.getParentPageFilterClass());
        //
        Assert.assertFalse(filter.accept(null, null, new P_Page()));
        Assert.assertTrue(filter.accept(null, new BRemovePageWithNodes(), new P_Page()));
      }
    }
  }

  @Test
  public void testExtensionsOnNodePage() throws ProcessingException {
    // affected by fragment.xml entries
    PageRemoveNodePage extensionPage = new PageRemoveNodePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    Assert.assertSame(extensionPage, outline.getActivePage());
    Assert.assertEquals(1, extensionPage.getChildNodeCount());
    Assert.assertSame(BRemovePageWithNodes.class, extensionPage.getChildNode(0).getClass());
  }

  @Test
  public void testExtensionsOnOutline() throws ProcessingException {
    // affected by fragment.xml entries
    PageRemoveOutline outline = new PageRemoveOutline();
    ITreeNode rootNode = outline.getRootNode();
    Assert.assertNotNull(rootNode);
    Assert.assertEquals(0, rootNode.getChildNodeCount());
  }

  private static class P_Page extends AbstractPageWithNodes {
  }
}
