/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IReadOnlyMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

/**
 * Visitor calling {@link IMenu#handleOwnerValueChanged(Object)} on menus, if the menu type allows it.
 */
public class MenuOwnerChangedVisitor implements IActionVisitor {

  private final Object m_ownerValue;
  private final Set<? extends IMenuType> m_menuTypes;

  public MenuOwnerChangedVisitor(Object ownerValue, Set<? extends IMenuType> menuTypes) {
    m_ownerValue = ownerValue;
    m_menuTypes = menuTypes;
  }

  @Override
  public int visit(IAction action) {
    if (action instanceof IMenu && !Collections.disjoint(((IMenu) action).getMenuTypes(), m_menuTypes) && !(action instanceof IReadOnlyMenu)) {
      IMenu menu = (IMenu) action;
      try {
        menu.handleOwnerValueChanged(m_ownerValue);
      }
      catch (RuntimeException ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
    return CONTINUE;
  }

}
