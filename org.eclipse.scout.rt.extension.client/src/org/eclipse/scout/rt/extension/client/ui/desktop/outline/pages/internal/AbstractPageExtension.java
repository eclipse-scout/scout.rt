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

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.IPageExtensionFilter;

/**
 * Base class for page extensions providing support for filtering the context a page extension may be applied to.
 * 
 * @since 3.9.0
 */
public abstract class AbstractPageExtension {

  private final IPageExtensionFilter m_pageFilter;

  public AbstractPageExtension() {
    this(null);
  }

  public AbstractPageExtension(IPageExtensionFilter pageFilter) {
    m_pageFilter = pageFilter;
  }

  public IPageExtensionFilter getPageFilter() {
    return m_pageFilter;
  }

  /**
   * Decides whether the given context is accepted by the filter outline and page class that are probably set on this
   * page extension. The given outline and page are checked with the outline filter and page filter classes,
   * respectively.
   * <p/>
   * If the page filter class is set ({@link #setParentPageFilterClass(Class)}) then the outline filter (
   * {@link #setOutlineFilterClass(Class)}) is ignored. Additionally, the outline filter is accepted only if the given
   * parent page class is null.
   * 
   * @param outline
   *          the current context's outline.
   * @param parentPage
   *          the current context's parent page.
   * @return Returns <code>true</code> if the current context described by outline and parent page are accepted by this
   *         page extension.
   */
  public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
    IPageExtensionFilter filter = getPageFilter();
    if (filter != null) {
      return filter.accept(outline, parentPage, affectedPage);
    }
    return true;
  }
}
