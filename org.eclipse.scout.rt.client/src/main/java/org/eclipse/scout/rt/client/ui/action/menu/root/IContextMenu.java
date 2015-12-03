/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu.root;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

/**
 * This is an invisible root menu container. Subclasses of this interface are used of form fields as an invisible root
 * menu.
 */
public interface IContextMenu extends IMenu {

  String PROP_CURRENT_MENU_TYPES = "currentMenuTypes";

  /**
   * @return the owner field, table, tree of the context menu
   */
  IPropertyObserver getOwner();

  /**
   * @return the menu types for the current owner value
   */
  Set<? extends IMenuType> getCurrentMenuTypes();

  void addContextMenuListener(ContextMenuListener listener);

  void removeContextMenuListener(ContextMenuListener listener);

}
