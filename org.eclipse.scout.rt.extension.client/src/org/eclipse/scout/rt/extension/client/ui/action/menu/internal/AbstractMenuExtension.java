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
package org.eclipse.scout.rt.extension.client.ui.action.menu.internal;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.extension.client.ui.action.menu.IMenuExtensionFilter;

/**
 * @since 3.9.0
 */
public class AbstractMenuExtension {

  private final IMenuExtensionFilter m_filter;
  private final Class<? extends IMenu> m_menuClass;

  public AbstractMenuExtension(Class<? extends IMenu> menuClass, IMenuExtensionFilter filter) {
    if (menuClass == null) {
      throw new IllegalArgumentException("menuClass must not be null");
    }
    m_menuClass = menuClass;
    m_filter = filter;
  }

  public Class<? extends IMenu> getMenuClass() {
    return m_menuClass;
  }

  public IMenuExtensionFilter getFilter() {
    return m_filter;
  }

  public boolean accept(Object anchor, Object container, IMenu menu) {
    if (anchor == null || container == null) {
      return false;
    }
    if (getFilter() != null) {
      return getFilter().accept(anchor, container, menu);
    }
    return true;
  }
}
