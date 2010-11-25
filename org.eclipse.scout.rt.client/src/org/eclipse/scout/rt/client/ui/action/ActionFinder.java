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
package org.eclipse.scout.rt.client.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

/**
 * search an actions tree to find an action
 */
public class ActionFinder {

  public ActionFinder() {
  }

  public <T extends IAction> T findAction(IAction[] actionTree, Class<T> searchType) {
    return findAction(Arrays.asList(actionTree), searchType);
  }

  @SuppressWarnings("unchecked")
  public <T extends IAction> T findAction(List<? extends IAction> actionTree, Class<T> searchType) {
    for (IAction a : actionTree) {
      if (searchType.isAssignableFrom(a.getClass())) {
        return (T) a;
      }
      if (a instanceof IActionNode<?>) {
        T t = findAction(((IActionNode<? extends IAction>) a).getChildActions(), searchType);
        if (t != null) {
          return t;
        }
      }
    }
    return null;
  }

  public <T extends IAction> List<T> findActions(IAction[] actionTree, Class<T> searchType) {
    return findActions(Arrays.asList(actionTree), searchType);
  }

  public <T extends IAction> List<T> findActions(List<? extends IAction> actionTree, Class<T> searchType) {
    ArrayList<T> list = new ArrayList<T>();
    for (IAction a : actionTree) {
      findActionsRec(a, searchType, list);
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  private <T extends IAction> void findActionsRec(IAction parent, Class<T> searchType, List<T> list) {
    if (searchType.isAssignableFrom(parent.getClass())) {
      list.add((T) parent);
    }
    if (parent instanceof IActionNode<?>) {
      for (IAction a : ((IActionNode<?>) parent).getChildActions()) {
        findActionsRec(a, searchType, list);
      }
    }
  }
}
