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
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.AContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.BContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.CContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.DContributionPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.PageContributionNodePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.contribution.PageContributionOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageAnchorFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageContributionExtension;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageExtensionManager;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
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
    assertNotNull(extensions);
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
    assertNotNull(extensions);
    for (PageContributionExtension ext : extensions) {
      if (ext.getPageClass() == DContributionPageWithNodes.class) {
        // desktop anchor and filters
        assertTrue(ext.getPageFilter() != null);
        assertSame(CompositePageFilter.class, ext.getPageFilter().getClass());
        //
        CompositePageFilter filter = (CompositePageFilter) ext.getPageFilter();
        assertEquals(3, filter.size());
        assertSame(PageAnchorFilter.class, filter.getFilters()[0].getClass());
        assertSame(APageFilter.class, filter.getFilters()[1].getClass());
        assertSame(BPageFilter.class, filter.getFilters()[2].getClass());
        //
        PageAnchorFilter anchor = (PageAnchorFilter) filter.getFilters()[0];
        assertNull(anchor.getOutlineFilterClass());
        assertSame(CContributionPageWithNodes.class, anchor.getParentPageFilterClass());
        //
        assertFalse(filter.accept(null, null, new P_Page()));
        assertTrue(filter.accept(null, new CContributionPageWithNodes(), new P_Page()));
      }
    }
  }

  @Test
  public void testExtensionsOnNodePage() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageContributionNodePage extensionPage = new PageContributionNodePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    assertSame(extensionPage, outline.getActivePage());
    assertEquals(4, extensionPage.getChildNodeCount());
    assertSame(AContributionPageWithNodes.class, extensionPage.getChildNode(0).getClass());
    assertSame(PageContributionNodePage.P_NodePage.class, extensionPage.getChildNode(1).getClass());
    assertSame(BContributionPageWithNodes.class, extensionPage.getChildNode(2).getClass());
    assertSame(PageContributionNodePage.P_NodePage.class, extensionPage.getChildNode(3).getClass());
  }

  @Test
  public void testExtensionsOnOutline() throws ProcessingException {
    // affected by fragment.xml entries
    Activator.getDefault().getPagesExtensionManager();
    PageContributionOutline outline = new PageContributionOutline();
    ITreeNode rootNode = outline.getRootNode();
    assertNotNull(rootNode);
    assertEquals(3, rootNode.getChildNodeCount());
    assertSame(PageContributionOutline.P_NodePage.class, rootNode.getChildNode(0).getClass());
    assertSame(PageContributionOutline.P_NodePage.class, rootNode.getChildNode(1).getClass());
    assertSame(AContributionPageWithNodes.class, rootNode.getChildNode(2).getClass());
  }

  private static class P_Page extends AbstractPageWithNodes {
  }
}
