/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.window.desktop.menu;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;

/**
 * <h3>DesktopUtility</h3> ...
 * 
 * @since 1.0.0 06.05.2008
 */
public final class DesktopUtility {

  private DesktopUtility() {
  }

  public static IAction findDesktopMenu(String qName, ISwtEnvironment environment) {
    IClientSession clientSession = environment.getClientSession();
    if (clientSession != null && clientSession.getDesktop() != null) {
      for (IMenu menu : clientSession.getDesktop().getMenus()) {
        IAction found = findActionImplRec(qName, menu);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static IAction findActionImplRec(String qName, IAction action) {
    if (action.getClass().getName().equals(qName)) {
      return action;
    }
    else {
      if (action instanceof IActionNode<?>) {
        IActionNode<?> actionNode = (IActionNode<?>) action;
        for (IAction subMenu : actionNode.getChildActions()) {
          IAction found = findActionImplRec(qName, subMenu);
          if (found != null) {
            return found;
          }
        }
      }
      return null;
    }
  }

  public static IAction findToolAction(String qName, ISwtEnvironment environment) {
    IClientSession clientSession = environment.getClientSession();
    if (clientSession != null) {
      for (IAction a : clientSession.getDesktop().getActions()) {
        IAction found = findActionImplRec(qName, a);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

}
