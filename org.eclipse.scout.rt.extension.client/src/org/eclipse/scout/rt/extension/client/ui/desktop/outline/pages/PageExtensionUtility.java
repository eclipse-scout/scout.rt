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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.extension.client.Activator;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.AbstractExtensibleOutline;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageContributionExtension;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageExtensionManager;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageModificationExtension;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal.PageRemoveExtension;

/**
 * Utility for applying page extensions. The three abstract classes {@link AbstractExtensibleOutline},
 * {@link AbstractExtensiblePageWithNodes} and {@link AbstractExtensiblePageWithTable} should be used in general for
 * providing extensible outlines. This utility class is intended to be used in cases in which it is not possible to
 * modify the given class hierarchy.
 *
 * @since 3.9.0
 */
public final class PageExtensionUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageExtensionUtility.class);

  private PageExtensionUtility() {
    // utility class
  }

  /**
   * Adapts the given {@link IPageWithNodes} by adding new pages to the given page list and by removing or modifying
   * existing pages.
   *
   * @param nodePage
   * @param pageList
   * @throws ProcessingException
   * @see {@link AbstractExtensiblePageWithNodes}
   */
  public static void adaptPageWithNodes(IPageWithNodes nodePage, List<IPage> pageList) throws ProcessingException {
    if (nodePage == null) {
      throw new ProcessingException("nodePage must not be null");
    }
    if (pageList == null) {
      throw new ProcessingException("pageList must not be null");
    }
    IOutline outline = nodePage.getOutline();
    PageExtensionManager extensionManager = Activator.getDefault().getPagesExtensionManager();
    contributePages(outline, nodePage, extensionManager.getPageContributionExtensions(), pageList);
    removePages(outline, nodePage, extensionManager.getPageRemovalExtensions(), pageList);
    modifyPages(outline, nodePage, extensionManager.getPageModificationExtensions(), pageList);
  }

  /**
   * Adapts the given {@link IOutline} by adding new pages to the given page list and by removing or modifying
   * existing pages.
   *
   * @param outline
   * @param pageList
   * @throws ProcessingException
   * @see {@link AbstractExtensibleOutline}
   */
  public static void adaptOutline(IOutline outline, List<IPage> pageList) throws ProcessingException {
    if (outline == null) {
      throw new ProcessingException("outline must not be null");
    }
    if (pageList == null) {
      throw new ProcessingException("pageList must not be null");
    }
    PageExtensionManager extensionManager = Activator.getDefault().getPagesExtensionManager();
    contributePages(outline, null, extensionManager.getPageContributionExtensions(), pageList);
    removePages(outline, null, extensionManager.getPageRemovalExtensions(), pageList);
    modifyPages(outline, null, extensionManager.getPageModificationExtensions(), pageList);
  }

  /**
   * Adapts the given page by applying page modifications contributed by extensions.
   *
   * @param outline
   * @param parentPage
   * @param page
   * @throws ProcessingException
   * @see {@link AbstractExtensiblePageWithTable}
   */
  public static void adaptPage(IOutline outline, IPageWithTable<? extends ITable> parentPage, IPage page) throws ProcessingException {
    if (outline == null) {
      throw new ProcessingException("outline must not be null");
    }
    if (parentPage == null) {
      throw new ProcessingException("parentPage must not be null");
    }
    if (page == null) {
      throw new ProcessingException("page must not be null");
    }
    PageExtensionManager extensionManager = Activator.getDefault().getPagesExtensionManager();
    modifyPages(outline, parentPage, extensionManager.getPageModificationExtensions(), Collections.singletonList(page));
  }

  static void contributePages(IOutline outline, IPageWithNodes parentPage, List<PageContributionExtension> extensions, List<IPage> pageList) throws ProcessingException {
    if (extensions == null || extensions.isEmpty()) {
      return;
    }

    // filter matching extensions
    List<PageContributionExtension> matchingExtensions = new LinkedList<PageContributionExtension>();
    for (PageContributionExtension e : extensions) {
      if (e.accept(outline, parentPage, null)) {
        matchingExtensions.add(e);
      }
    }

    if (matchingExtensions.isEmpty()) {
      return;
    }

    Map<CompositeObject,IPage> orderedPages = new TreeMap<CompositeObject, IPage>();
    // assign synthetic order to existing pages
    int counter = 0;
    for (IPage p : pageList) {
      orderedPages.put(new CompositeObject(Double.valueOf((counter + 1) * 10), counter), p);
      counter++;
    }
    // create new pages
    for (PageContributionExtension e : matchingExtensions) {
      try {
        IPage p = e.createContribution(outline, parentPage);
        orderedPages.put(new CompositeObject(e.getOrder(), counter), p);
        counter++;
      }
      catch (Throwable t) {
        LOG.error("Exception while creating an instance of a contributed page", t);
      }
    }

    // reorder existing and add new pages
    pageList.clear();
    pageList.addAll(orderedPages.values());
  }

  static void removePages(IOutline outline, IPage parentPage, List<PageRemoveExtension> extensions, List<IPage> pageList) {
    if (extensions == null || extensions.isEmpty()) {
      return;
    }

    for (Iterator<IPage> it = pageList.iterator(); it.hasNext();) {
      IPage page = it.next();
      for (PageRemoveExtension removeExtension : extensions) {
        if (removeExtension.accept(outline, parentPage, page)) {
          it.remove();
          break;
        }
      }
    }
  }

  static void modifyPages(IOutline outline, IPage parentPage, List<PageModificationExtension> extensions, List<IPage> pageList) {
    if (extensions == null || extensions.isEmpty()) {
      return;
    }

    for (PageModificationExtension ext : extensions) {
      for (IPage page : pageList) {
        try {
          if (ext.accept(outline, parentPage, page)) {
            IPageModifier<IPage> pageModifier = ext.createPageModifier();
            pageModifier.modify(outline, parentPage, page);
          }
        }
        catch (ProcessingException e) {
          LOG.error("Exception while modifying page", e);
        }
      }
    }
  }
}
