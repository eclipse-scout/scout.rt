/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IReadOnlyMenu;

/**
 * Visitor calling {@link IMenu#handleOwnerValueChanged(Object)} on menus, if the menu type allows it.
 */
public class MenuOwnerChangedVisitor implements Consumer<IMenu> {

  private final Object m_ownerValue;
  private final Set<? extends IMenuType> m_menuTypes;

  public MenuOwnerChangedVisitor(Object ownerValue, Set<? extends IMenuType> menuTypes) {
    m_ownerValue = ownerValue;
    m_menuTypes = menuTypes;
  }

  @Override
  public void accept(IMenu menu) {
    if (!Collections.disjoint(menu.getMenuTypes(), m_menuTypes) && !(menu instanceof IReadOnlyMenu)) {
      menu.handleOwnerValueChanged(m_ownerValue);
    }
  }
}
