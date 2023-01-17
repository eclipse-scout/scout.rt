/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

/**
 * search an actions tree to find an action
 */
public class ActionFinder {

  public <T extends IAction> T findAction(List<? extends IAction> actionTree, Class<T> searchType) {
    List<T> filteredActions = findActions(actionTree, searchType, true, true);
    return CollectionUtility.firstElement(filteredActions);
  }

  public <T extends IAction> List<T> findActions(List<? extends IAction> actionTree, Class<T> searchType, boolean recursive) {
    return findActions(actionTree, searchType, recursive, false);
  }

  @SuppressWarnings("unchecked")
  protected <T extends IAction> List<T> findActions(List<? extends IAction> actionTree, Class<T> searchType, boolean recursive, boolean oneMatchSearch) {
    if (CollectionUtility.isEmpty(actionTree) || searchType == null) {
      return CollectionUtility.emptyArrayList();
    }

    List<T> result = new ArrayList<>();
    Function<IAction, TreeVisitResult> collector = action -> {
      if (searchType.isInstance(action)) {
        result.add((T) action);
        if (oneMatchSearch) {
          return TreeVisitResult.TERMINATE;
        }
      }

      return recursive ? TreeVisitResult.CONTINUE : TreeVisitResult.SKIP_SUBTREE;
    };
    for (IAction action : actionTree) {
      if (action == null) {
        continue;
      }
      TreeVisitResult continueSearch = action.visit(collector, IAction.class);
      if (continueSearch == TreeVisitResult.TERMINATE) {
        return result;
      }
    }
    return result;
  }
}
