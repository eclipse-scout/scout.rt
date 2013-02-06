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

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;

/**
 * This class references a page and describes the places in the existing outline structure, where the referenced page is
 * added.
 * 
 * @since 3.9.0
 */
public class PageContributionExtension extends AbstractPageExtension {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PageContributionExtension.class);

  private final Class<? extends IPage> m_pageClass;
  private final double m_order;

  public PageContributionExtension(Class<? extends IPage> pageClass, double order) {
    this(null, pageClass, order);
  }

  public PageContributionExtension(IPageExtensionFilter pageFilter, Class<? extends IPage> pageClass, double order) {
    super(pageFilter);
    m_pageClass = pageClass;
    m_order = order;
  }

  public Class<? extends IPage> getPageClass() {
    return m_pageClass;
  }

  public double getOrder() {
    return m_order;
  }

  /**
   * Creates and returns a new page instance described by this page contribution. The contributed page must either
   * implement a constructor with the parent page or outline
   * 
   * @param outline
   * @param parentPage
   * @return
   * @throws ProcessingException
   */
  public IPage createContribution(IOutline outline, IPageWithNodes parentPage) throws ProcessingException {
    if (outline == null) {
      throw new IllegalArgumentException("outline must not be null");
    }
    IPage page;
    if (parentPage != null) {
      // 1. try with single parameter parent page
      page = BeanUtility.createInstance(getPageClass(), parentPage);
      if (page != null) {
        return page;
      }
      // 2.a try with outline and parent page
      page = BeanUtility.createInstance(getPageClass(), outline, parentPage);
      if (page != null) {
        return page;
      }
      // 2.b try with outline and parent page (reverse order)
      page = BeanUtility.createInstance(getPageClass(), parentPage, outline);
      if (page != null) {
        return page;
      }
    }
    // 3. try with single parameter outline
    page = BeanUtility.createInstance(getPageClass(), outline);
    if (page != null) {
      return page;
    }
    // 4.a try with outline and parent page
    page = BeanUtility.createInstance(getPageClass(), outline, parentPage);
    if (page != null) {
      return page;
    }
    // 4.b try with outline and parent page (reverse order)
    page = BeanUtility.createInstance(getPageClass(), parentPage, outline);
    if (page != null) {
      return page;
    }
    // 5. try default constructor
    page = BeanUtility.createInstance(getPageClass());
    if (page != null) {
      return page;
    }
    if (parentPage == null) {
      // 6. try with single parameter constructor and null parameter
      page = BeanUtility.createInstance(getPageClass(), new Object[]{null});
      if (page != null) {
        return page;
      }
    }
    throw new ProcessingException("Cannot create new instance of class [" + getPageClass() + "] with arguments outline=[" + outline + "], parentPage=[" + parentPage + "]");
  }
}
