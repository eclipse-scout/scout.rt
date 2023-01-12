/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.root;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

/**
 * This is an invisible root menu container. Subclasses of this interface are used of form fields as an invisible root
 * menu.
 */
public interface IContextMenu extends IMenu {

  String PROP_CURRENT_MENU_TYPES = "currentMenuTypes";

  /**
   * @return the menu types for the current owner value
   */
  Set<? extends IMenuType> getCurrentMenuTypes();

  IFastListenerList<ContextMenuListener> contextMenuListeners();

  default void addContextMenuListener(ContextMenuListener listener) {
    contextMenuListeners().add(listener);
  }

  default void removeContextMenuListener(ContextMenuListener listener) {
    contextMenuListeners().remove(listener);
  }
}
