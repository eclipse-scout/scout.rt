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
public class MenuAnchorFilter implements IMenuExtensionFilter {

  private final Class<?> m_anchorClass;

  public MenuAnchorFilter(Class<?> anchorClass) {
    m_anchorClass = anchorClass;
  }

  public Class<?> getAnchorClass() {
    return m_anchorClass;
  }

  @Override
  public boolean accept(Object anchor, Object container, IMenu menu) {
    if (anchor == null || container == null) {
      return false;
    }
    if (getAnchorClass() == null) {
      return true;
    }
    return getAnchorClass().isInstance(anchor);
  }
}
