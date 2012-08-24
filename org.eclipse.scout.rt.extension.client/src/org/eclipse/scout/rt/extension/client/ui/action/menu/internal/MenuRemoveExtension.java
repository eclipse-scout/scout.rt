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
public class MenuRemoveExtension extends AbstractMenuExtension {

  public MenuRemoveExtension(Class<? extends IMenu> menuClass, IMenuExtensionFilter filter) {
    super(menuClass, filter);

  }

  @Override
  public boolean accept(Object anchor, Object container, IMenu menu) {
    if (menu == null) {
      throw new IllegalArgumentException("menu must not be null");
    }
    if (!getMenuClass().isInstance(menu)) {
      return false;
    }
    return super.accept(anchor, container, menu);
  }
}
