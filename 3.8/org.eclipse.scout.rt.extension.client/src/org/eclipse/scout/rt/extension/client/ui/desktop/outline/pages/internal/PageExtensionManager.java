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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.internal.AbstractExtensionManager;
import org.eclipse.scout.rt.extension.client.internal.IExtensionProcessor;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.CompositePageFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageModifier;
import org.osgi.framework.Bundle;

/**
 * @since 3.9.0
 */
public class PageExtensionManager extends AbstractExtensionManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageExtensionManager.class);

  public static final String EXTENSION_POINT_ID = org.eclipse.scout.rt.extension.client.Activator.PLUGIN_ID + ".pages";
  public static final String PAGE_CONTRIBUTION_ELEMENT = "pageContribution";
  public static final String PAGE_REMOVAL_ELEMENT = "pageRemoval";
  public static final String PAGE_MODIFICATION_ELEMENT = "pageModification";

  private final List<PageContributionExtension> m_pageContributionExtensions;
  private final List<PageRemoveExtension> m_pageRemoveExtensions;
  private final List<PageModificationExtension> m_pageModificationExtensions;

  public PageExtensionManager(IExtensionRegistry extensionRegistry) {
    super(extensionRegistry, EXTENSION_POINT_ID);
    m_pageContributionExtensions = new LinkedList<PageContributionExtension>();
    m_pageRemoveExtensions = new LinkedList<PageRemoveExtension>();
    m_pageModificationExtensions = new LinkedList<PageModificationExtension>();
    initExtensionProcessors();
  }

  public List<PageContributionExtension> getPageContributionExtensions() throws ProcessingException {
    synchronized (getLock()) {
      ensureStarted();
      return new LinkedList<PageContributionExtension>(m_pageContributionExtensions);
    }
  }

  public List<PageRemoveExtension> getPageRemovalExtensions() throws ProcessingException {
    synchronized (getLock()) {
      ensureStarted();
      return new LinkedList<PageRemoveExtension>(m_pageRemoveExtensions);
    }
  }

  public List<PageModificationExtension> getPageModificationExtensions() throws ProcessingException {
    synchronized (getLock()) {
      ensureStarted();
      return new LinkedList<PageModificationExtension>(m_pageModificationExtensions);
    }
  }

  private void initExtensionProcessors() {
    addExtensionProcessor(PAGE_CONTRIBUTION_ELEMENT,
        new IExtensionProcessor<PageContributionExtension>() {
          @Override
          public PageContributionExtension processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Double order = TypeCastUtility.castValue(element.getAttribute("order"), Double.class);
            Class<? extends IPage> pageClass = loadClass(contributor, IPage.class, element.getAttribute("class"));
            IPageExtensionFilter pageFilter = parseAndCreatePageFilters(contributor, element);
            PageContributionExtension pageContribution = new PageContributionExtension(pageFilter, pageClass, NumberUtility.nvl(order, 123456789.0));
            m_pageContributionExtensions.add(pageContribution);
            return pageContribution;
          }
        });

    addExtensionProcessor(PAGE_REMOVAL_ELEMENT,
        new IExtensionProcessor<PageRemoveExtension>() {
          @Override
          public PageRemoveExtension processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Class<? extends IPage> pageClass = loadClass(contributor, IPage.class, element.getAttribute("class"));
            IPageExtensionFilter pageFilter = parseAndCreatePageFilters(contributor, element);
            PageRemoveExtension pageRemoveExtension = new PageRemoveExtension(pageFilter, pageClass);
            m_pageRemoveExtensions.add(pageRemoveExtension);
            return pageRemoveExtension;
          }
        });

    addExtensionProcessor(PAGE_MODIFICATION_ELEMENT,
        new IExtensionProcessor<PageModificationExtension>() {
          @Override
          public PageModificationExtension processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Class<? extends IPage> pageClass = loadClass(contributor, IPage.class, element.getAttribute("page"));
            @SuppressWarnings("unchecked")
            Class<? extends IPageModifier<IPage>> modifierClass = (Class<? extends IPageModifier<IPage>>) loadClass(contributor, IPageModifier.class, element.getAttribute("class"));
            if (modifierClass == null) {
              return null;
            }
            IPageExtensionFilter pageFilter = parseAndCreatePageFilters(contributor, element);
            PageModificationExtension pageModification = new PageModificationExtension(pageFilter, pageClass, modifierClass);
            m_pageModificationExtensions.add(pageModification);
            return pageModification;
          }
        });
  }

  private IPageExtensionFilter parseAndCreatePageFilters(Bundle contributor, IConfigurationElement element) throws Exception {
    Class<? extends IOutline> outlineFilterClass = null;
    Class<? extends IPage> parentPageFilterClass = null;
    CompositePageFilter compositeFilter = new CompositePageFilter();

    for (IConfigurationElement child : element.getChildren()) {
      if ("outline".equals(child.getName())) {
        outlineFilterClass = loadClass(contributor, IOutline.class, child.getAttribute("class"));
      }
      else if ("parentPage".equals(child.getName())) {
        parentPageFilterClass = loadClass(contributor, IPage.class, child.getAttribute("class"));
      }
      else if ("filter".equals(child.getName())) {
        try {
          IPageExtensionFilter filter = (IPageExtensionFilter) child.createExecutableExtension("class");
          compositeFilter.addFilter(filter);
        }
        catch (Exception e) {
          LOG.error("Exception while creating filter class [" + element.getAttribute("class") + "]", e);
        }
      }
    }
    PageAnchorFilter desktopAnchorFilter = null;
    if (outlineFilterClass != null || parentPageFilterClass != null) {
      desktopAnchorFilter = new PageAnchorFilter(outlineFilterClass, parentPageFilterClass);
      compositeFilter.addFilterAtBegin(desktopAnchorFilter);
    }
    //
    if (!compositeFilter.isEmpty()) {
      return compositeFilter;
    }
    return null;
  }

  @Override
  protected void removeContributions(Set<Object> contributions) {
    m_pageContributionExtensions.removeAll(contributions);
    m_pageRemoveExtensions.removeAll(contributions);
    m_pageModificationExtensions.removeAll(contributions);
  }
}
