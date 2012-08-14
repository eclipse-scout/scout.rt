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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

/**
 * Composite page filter class containing other page filters. Invoking {@link #accept(IOutline, IPage, IPage)} returns
 * <code>true</code>, iff all contained filters return <code>true</code>. Otherwiese <code>false</code>.
 * 
 * @since 3.9.0
 */
public class CompositePageFilter implements IPageExtensionFilter {

  private final List<IPageExtensionFilter> m_filters;

  public CompositePageFilter(IPageExtensionFilter... filters) {
    m_filters = new LinkedList<IPageExtensionFilter>();
    if (filters != null) {
      for (IPageExtensionFilter f : filters) {
        addFilter(f);
      }
    }
  }

  public boolean addFilter(IPageExtensionFilter filter) {
    if (filter == null) {
      return false;
    }
    return m_filters.add(filter);
  }

  public boolean addFilterAtBegin(IPageExtensionFilter filter) {
    if (filter == null) {
      return false;
    }
    m_filters.add(0, filter);
    return true;
  }

  public boolean removeFilter(IPageExtensionFilter filter) {
    return m_filters.remove(filter);
  }

  public boolean isEmpty() {
    return m_filters.isEmpty();
  }

  public int size() {
    return m_filters.size();
  }

  public IPageExtensionFilter[] getFilters() {
    return m_filters.toArray(new IPageExtensionFilter[m_filters.size()]);
  }

  /**
   * Returns <code>true</code> if all contained page filters return <code>true</code>. Otherwise <code>false</code>.
   * 
   * @see IPageExtensionFilter#accept(IOutline, IPage, IPage)
   */
  @Override
  public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
    for (IPageExtensionFilter filter : m_filters) {
      if (!filter.accept(outline, parentPage, affectedPage)) {
        return false;
      }
    }
    return true;
  }
}
