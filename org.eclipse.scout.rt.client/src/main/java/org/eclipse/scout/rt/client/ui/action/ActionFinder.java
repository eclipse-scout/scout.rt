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
package org.eclipse.scout.rt.client.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

/**
 * search an actions tree to find an action
 */
public class ActionFinder {

  public ActionFinder() {
  }

  public <T extends IAction> T findAction(List<? extends IAction> actionTree, Class<T> searchType) {
    List<T> filteredActions = findActions(actionTree, searchType, true, true);
    if (filteredActions.size() > 0) {
      return filteredActions.get(0);
    }
    return null;
  }

  public <T extends IAction> List<T> findActions(List<? extends IAction> actionTree, Class<T> searchType, boolean recursive) {
    return findActions(actionTree, searchType, recursive, false);
  }

  @SuppressWarnings("unchecked")
  private <T extends IAction> List<T> findActions(List<? extends IAction> actionTree, Class<T> searchType, boolean recursive, boolean oneMatchSearch) {
    List<T> list = new ArrayList<T>();

    for (IAction action : actionTree) {
      if (searchType.isAssignableFrom(action.getClass())) {
        list.add((T) action);
        if (oneMatchSearch) {
          return list;
        }
      }

      if (recursive && action instanceof IActionNode<?>) {
        List<? extends IAction> childActions = ((IActionNode<?>) action).getChildActions();
        List<T> filteredChildActions = findActions(childActions, searchType, true, oneMatchSearch);
        list.addAll(filteredChildActions);
      }
    }

    return list;
  }
}
