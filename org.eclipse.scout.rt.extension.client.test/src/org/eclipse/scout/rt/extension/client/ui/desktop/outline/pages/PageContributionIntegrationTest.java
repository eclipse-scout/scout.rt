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
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.AContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.BContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.CContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.DContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.PageContributionNodePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.PageContributionOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageAnchorFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageContributionExtension;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageExtensionManager;
import org.eclipse.scout.rt.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.9.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageContributionIntegrationTest {

  @Test
  public void testDisabledExtensions() throws ProcessingException {
    PageExtensionManager manager = Activator.getDefault().getPagesExtensionManager();
    List<PageContributionExtension> extensions = manager.getPageContributionExtensions();
    Assert.assertNotNull(extensions);
    for (PageContributionExtension ext : extensions) {
      if (ext.getPageClass() == CContributionPageWithNodes.class) {
        Assert.fail("Disabled pageContribution is available for page " + CContributionPageWithNodes.class.getSimpleName());
      }
    }
  }

  @Test
  public void testFilters() throws ProcessingException {
    PageExtensionManager manager = Activator.getDefault().getPagesExtensionManager();
    List<PageContributionExtension> extensions = manager.getPageContributionExtensions();
    Assert.assertNotNull(extensions);
    for (PageContributionExtension ext : extensions) {
      if (ext.getPageClass() == DContributionPageWithNodes.class) {
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
        Assert.assertSame(CContributionPageWithNodes.class, anchor.getParentPageFilterClass());
        //
        Assert.assertFalse(filter.accept(null, null, new P_Page()));
        Assert.assertTrue(filter.accept(null, new CContributionPageWithNodes(), new P_Page()));
      }
    }
  }

  @Test
  public void testExtensionsOnNodePage() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageContributionNodePage extensionPage = new PageContributionNodePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    Assert.assertSame(extensionPage, outline.getActivePage());
    Assert.assertEquals(4, extensionPage.getChildNodeCount());
    Assert.assertSame(AContributionPageWithNodes.class, extensionPage.getChildNode(0).getClass());
    Assert.assertSame(PageContributionNodePage.P_NodePage.class, extensionPage.getChildNode(1).getClass());
    Assert.assertSame(BContributionPageWithNodes.class, extensionPage.getChildNode(2).getClass());
    Assert.assertSame(PageContributionNodePage.P_NodePage.class, extensionPage.getChildNode(3).getClass());
  }

  @Test
  public void testExtensionsOnOutline() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageContributionOutline outline = new PageContributionOutline();
    ITreeNode rootNode = outline.getRootNode();
    Assert.assertNotNull(rootNode);
    Assert.assertEquals(3, rootNode.getChildNodeCount());
    Assert.assertSame(PageContributionOutline.P_NodePage.class, rootNode.getChildNode(0).getClass());
    Assert.assertSame(PageContributionOutline.P_NodePage.class, rootNode.getChildNode(1).getClass());
    Assert.assertSame(AContributionPageWithNodes.class, rootNode.getChildNode(2).getClass());
  }

  private static class P_Page extends AbstractPageWithNodes {
  }
}
