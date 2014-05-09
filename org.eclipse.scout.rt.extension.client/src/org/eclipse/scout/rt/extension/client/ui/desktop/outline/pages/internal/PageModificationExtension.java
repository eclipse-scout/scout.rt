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

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageModifier;

/**
 * This class references a page modification and describes a set of pages, the given modification is applied to.
 * 
 * @since 3.9.0
 */
public class PageModificationExtension extends AbstractPageExtension {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageModificationExtension.class);

  private final Class<? extends IPage> m_pageClass;
  private final Class<? extends IPageModifier<? extends IPage>> m_pageModifier;

  public PageModificationExtension(Class<? extends IPage> pageClass, Class<? extends IPageModifier<? extends IPage>> pageModifier) {
    this(null, pageClass, pageModifier);
  }

  public PageModificationExtension(IPageExtensionFilter pageFilter, Class<? extends IPage> pageClass, Class<? extends IPageModifier<? extends IPage>> pageModifier) {
    super(pageFilter);
    if (pageClass == null) {
      throw new IllegalArgumentException("pageClass must not be null");
    }
    if (pageModifier == null) {
      throw new IllegalArgumentException("pageModifier must not be null");
    }
    // check assignability of given page class along with the type parameter defined on the page modifier
    Class<?> pageModifierPageType = TypeCastUtility.getGenericsParameterClass(pageModifier, IPageModifier.class);
    if (pageModifierPageType == null) {
      LOG.warn("could not determine generic type parameter of page modifier '" + pageModifier.getName() + ";");
    }
    else if (!pageModifierPageType.isAssignableFrom(pageClass)) {
      throw new IllegalArgumentException("pageClass must be assignalbe to the generic type of given pageModifier. [pageClass: '"
          + pageClass.getName() + "', generic type on pageModifier: '" + pageModifierPageType.getName() + "'");
    }
    m_pageClass = pageClass;
    m_pageModifier = pageModifier;
  }

  public Class<? extends IPage> getPageClass() {
    return m_pageClass;
  }

  @Override
  public boolean accept(IOutline outline, IPage parentPage, IPage page) {
    if (!getPageClass().isInstance(page)) {
      return false;
    }
    return super.accept(outline, parentPage, page);
  }

  @SuppressWarnings("unchecked")
  public <T extends IPage> IPageModifier<T> createPageModifier() throws ProcessingException {
    try {
      return (IPageModifier<T>) m_pageModifier.newInstance();
    }
    catch (Exception e) {
      throw new ProcessingException("Error while instantiating page modifier '" + m_pageModifier + "'.", e);
    }
  }
}
