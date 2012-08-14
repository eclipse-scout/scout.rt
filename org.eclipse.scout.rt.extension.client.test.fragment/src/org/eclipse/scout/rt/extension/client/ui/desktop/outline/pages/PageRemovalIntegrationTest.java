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
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.AbstractCRemovePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.BRemovePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.C2RemovePageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.PageRemoveNodePage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.remove.PageRemoveOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.DesktopAnchorFilter;
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
    assertNotNull(extensions);
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
    assertNotNull(extensions);
    for (PageRemoveExtension ext : extensions) {
      if (ext.getPageClass() == AbstractCRemovePageWithNodes.class) {
        // filters only
        assertTrue(ext.getPageFilter() != null);
        assertSame(CompositePageFilter.class, ext.getPageFilter().getClass());
        //
        CompositePageFilter filter = (CompositePageFilter) ext.getPageFilter();
        assertEquals(2, filter.size());
        assertSame(APageFilter.class, filter.getFilters()[0].getClass());
        assertSame(BPageFilter.class, filter.getFilters()[1].getClass());
        //
        assertTrue(filter.accept(null, null, new P_Page()));
      }
      else if (ext.getPageClass() == C2RemovePageWithNodes.class) {
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
        assertSame(BRemovePageWithNodes.class, anchor.getParentPageFilterClass());
        //
        assertFalse(filter.accept(null, null, new P_Page()));
        assertTrue(filter.accept(null, new BRemovePageWithNodes(), new P_Page()));
      }
    }
  }

  @Test
  public void testExtensionsOnNodePage() throws ProcessingException {
    // affected by fragment.xml entries
    PageRemoveNodePage extensionPage = new PageRemoveNodePage();
    DynamicOutline outline = new DynamicOutline(extensionPage);
    assertSame(extensionPage, outline.getActivePage());
    assertEquals(1, extensionPage.getChildNodeCount());
    assertSame(BRemovePageWithNodes.class, extensionPage.getChildNode(0).getClass());
  }

  @Test
  public void testExtensionsOnOutline() throws ProcessingException {
    // affected by fragment.xml entries
    PageRemoveOutline outline = new PageRemoveOutline();
    ITreeNode rootNode = outline.getRootNode();
    assertNotNull(rootNode);
    assertEquals(0, rootNode.getChildNodeCount());
  }

  private static class P_Page extends AbstractPageWithNodes {
  }
}
