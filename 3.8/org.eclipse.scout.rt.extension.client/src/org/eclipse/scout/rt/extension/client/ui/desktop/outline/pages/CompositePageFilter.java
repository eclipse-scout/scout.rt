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

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.internal.AbstractCompositeExtensionFilter;

/**
 * Composite page filter class containing other page filters. Invoking {@link #accept(IOutline, IPage, IPage)} returns
 * <code>true</code>, iff all contained filters return <code>true</code>. Otherwise <code>false</code>.
 * 
 * @since 3.9.0
 */
public class CompositePageFilter extends AbstractCompositeExtensionFilter<IPageExtensionFilter> implements IPageExtensionFilter {

  public CompositePageFilter(IPageExtensionFilter... filters) {
    super(filters);
  }

  /**
   * Returns <code>true</code> if all contained page filters return <code>true</code>. Otherwise <code>false</code>.
   * 
   * @see IPageExtensionFilter#accept(IOutline, IPage, IPage)
   */
  @Override
  public boolean accept(IOutline outline, IPage parentPage, IPage affectedPage) {
    for (IPageExtensionFilter filter : getFilters()) {
      if (!filter.accept(outline, parentPage, affectedPage)) {
        return false;
      }
    }
    return true;
  }
}
