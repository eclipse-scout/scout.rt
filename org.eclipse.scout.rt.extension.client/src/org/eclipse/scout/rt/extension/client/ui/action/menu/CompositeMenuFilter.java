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
package org.eclipse.scout.rt.extension.client.ui.action.menu;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.internal.AbstractCompositeExtensionFilter;

/**
 * Composite menu filter class containing other menu filters. Invoking {@link #accept(Object, Object, IMenu)} returns
 * <code>true</code>, iff all contained filters return <code>true</code>. Otherwise <code>false</code>.
 * 
 * @since 3.9.0
 */
public class CompositeMenuFilter extends AbstractCompositeExtensionFilter<IMenuExtensionFilter> implements IMenuExtensionFilter {

  public CompositeMenuFilter(IMenuExtensionFilter... filters) {
    super(filters);
  }

  /**
   * Returns <code>true</code> if all contained menu filters return <code>true</code>. Otherwise <code>false</code>.
   * 
   * @see IMenuExtensionFilter#accept(Object, Object, IMenu)
   */
  @Override
  public boolean accept(Object anchor, Object container, IMenu menu) {
    for (IMenuExtensionFilter filter : getFilters()) {
      if (!filter.accept(anchor, container, menu)) {
        return false;
      }
    }
    return true;
  }
}
