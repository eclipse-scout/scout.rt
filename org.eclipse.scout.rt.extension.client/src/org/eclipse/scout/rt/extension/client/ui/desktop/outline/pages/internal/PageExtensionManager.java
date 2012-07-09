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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.runtime.ExtensionPointTracker;
import org.eclipse.scout.commons.runtime.ExtensionPointTracker.Listener;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageModifier;
import org.osgi.framework.Bundle;

/**
 * @since 3.9.0
 */
public class PageExtensionManager implements Listener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageExtensionManager.class);

  public static final String EXTENSION_POINT_ID = org.eclipse.scout.rt.extension.client.Activator.PLUGIN_ID + ".pages";
  public static final String PAGE_CONTRIBUTION_ELEMENT = "pageContribution";
  public static final String PAGE_MODIFICATION_ELEMENT = "pageModification";
  public static final String PAGE_REMOVAL_ELEMENT = "pageRemoval";

  private final Map<IExtension, Set<Object>> m_extensionContributionsMap;
  private final List<PageContributionExtension> m_pageContributionExtensions;
  private final List<PageModificationExtension> m_pageModificationExtensions;
  private final List<PageRemoveExtension> m_pageRemoveExtensions;
  private ExtensionPointTracker m_tracker;

  public PageExtensionManager(IExtensionRegistry extensionRegistry) {
    m_extensionContributionsMap = new HashMap<IExtension, Set<Object>>();
    m_pageContributionExtensions = new LinkedList<PageContributionExtension>();
    m_pageModificationExtensions = new LinkedList<PageModificationExtension>();
    m_pageRemoveExtensions = new LinkedList<PageRemoveExtension>();
    m_tracker = new ExtensionPointTracker(extensionRegistry, EXTENSION_POINT_ID, this);
  }

  public void start() {
    if (m_tracker != null) {
      m_tracker.open();
    }
  }

  public void stop() {
    if (m_tracker != null) {
      m_tracker.close();
      m_tracker = null;
    }
  }

  public List<PageContributionExtension> getPageContributionExtensions() throws ProcessingException {
    synchronized (getLock()) {
      return new LinkedList<PageContributionExtension>(m_pageContributionExtensions);
    }
  }

  public List<PageModificationExtension> getPageModificationExtensions() throws ProcessingException {
    synchronized (getLock()) {
      return new LinkedList<PageModificationExtension>(m_pageModificationExtensions);
    }
  }

  public List<PageRemoveExtension> getPageRemovalExtensions() throws ProcessingException {
    synchronized (getLock()) {
      return new LinkedList<PageRemoveExtension>(m_pageRemoveExtensions);
    }
  }

  @Override
  public void added(IExtension extension) {
    // resolve contribution bundle
    Bundle contributor = Platform.getBundle(extension.getContributor().getName());
    if (contributor == null) {
      LOG.info("conributor bundle not found for id '" + extension.getContributor().getName() + "'");
      return;
    }
    // process contributions
    Set<Object> contributions = new HashSet<Object>();
    for (IConfigurationElement element : extension.getConfigurationElements()) {
      try {
        // check whether contribution is active
        String active = element.getAttribute("active");
        if (active != null && !TypeCastUtility.castValue(active, boolean.class)) {
          if (LOG.isInfoEnabled()) {
            LOG.info("ignoring inactive pages extension " + element.getName());
          }
          continue;
        }
        // parse contributions
        if (PAGE_CONTRIBUTION_ELEMENT.equals(element.getName())) {
          Double order = TypeCastUtility.castValue(element.getAttribute("order"), Double.class);
          Class<? extends IPage> pageClass = loadClass(contributor, IPage.class, element.getAttribute("class"));
          IPageExtensionFilter pageFilter = parseOutlineAndParentPageFilter(contributor, element);
          PageContributionExtension pageContribution = new PageContributionExtension(pageFilter, pageClass, NumberUtility.nvl(order, 123456789.0));
          m_pageContributionExtensions.add(pageContribution);
          contributions.add(pageContribution);
        }
        else if (PAGE_MODIFICATION_ELEMENT.equals(element.getName())) {
          Class<? extends IPage> pageClass = loadClass(contributor, IPage.class, element.getAttribute("page"));
          @SuppressWarnings("unchecked")
          Class<? extends IPageModifier<IPage>> modifierClass = (Class<? extends IPageModifier<IPage>>) loadClass(contributor, IPageModifier.class, element.getAttribute("class"));
          if (modifierClass == null) {
            continue;
          }
          IPageExtensionFilter pageFilter = parseOutlineAndParentPageFilter(contributor, element);
          PageModificationExtension pageModification = new PageModificationExtension(pageFilter, pageClass, modifierClass);
          m_pageModificationExtensions.add(pageModification);
          contributions.add(pageModification);
        }
        else if (PAGE_REMOVAL_ELEMENT.equals(element.getName())) {
          Class<? extends IPage> pageClass = loadClass(contributor, IPage.class, element.getAttribute("page"));
          IPageExtensionFilter pageFilter = parseOutlineAndParentPageFilter(contributor, element);
          PageRemoveExtension pageRemoveExtension = new PageRemoveExtension(pageFilter, pageClass);
          m_pageRemoveExtensions.add(pageRemoveExtension);
          contributions.add(pageRemoveExtension);
        }
        else {
          // unknown type
          LOG.warn("unknown pages contribution [" + element.getName() + "]");
        }
      }
      catch (Exception e) {
        LOG.error("could not load extension [" + EXTENSION_POINT_ID + " / " + element.getName() + "] for " + element + " in " + extension.getContributor(), e);
      }
    }
    // extension-contribution mapping
    if (!contributions.isEmpty()) {
      m_extensionContributionsMap.put(extension, contributions);
    }
  }

  private IPageExtensionFilter parseOutlineAndParentPageFilter(Bundle contributor, IConfigurationElement element) throws Exception {
    Class<? extends IOutline> outlineFilterClass = null;
    Class<? extends IPage> parentPageFilterClass = null;

    for (IConfigurationElement child : element.getChildren()) {
      if ("outline".equals(child.getName())) {
        outlineFilterClass = loadClass(contributor, IOutline.class, child.getAttribute("class"));
      }
      else if ("parentPage".equals(child.getName())) {
        parentPageFilterClass = loadClass(contributor, IPage.class, child.getAttribute("class"));
      }
    }

    return new ParentAndOutlinePageFilter(outlineFilterClass, parentPageFilterClass);
  }

  @Override
  public void removed(IExtension extension) {
    synchronized (getLock()) {
      Set<Object> contributions = m_extensionContributionsMap.remove(extension);
      m_pageContributionExtensions.removeAll(contributions);
      m_pageModificationExtensions.removeAll(contributions);
      m_pageRemoveExtensions.removeAll(contributions);
    }
  }

  private Object getLock() {
    if (m_tracker != null) {
      return m_tracker.getTrackerLock();
    }
    return new Object();
  }

  private static <T> Class<? extends T> loadClass(Bundle contributor, Class<T> type, String classname) throws Exception {
    if (contributor == null || !StringUtility.hasText(classname)) {
      return null;
    }
    @SuppressWarnings("unchecked")
    Class<T> cl = contributor.loadClass(classname);
    if (type != null && !type.isAssignableFrom(cl)) {
      throw new ProcessingException("class [" + classname + "] is not instance of [" + type.getName() + "]");
    }
    return cl;
  }
}
