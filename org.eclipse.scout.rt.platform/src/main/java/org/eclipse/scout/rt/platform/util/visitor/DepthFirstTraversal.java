/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.visitor;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.isEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DepthFirstTraversal<T> implements ITreeTraversal<T> {

  private final IDepthFirstTreeVisitor<T> m_visitor;
  private final Function<T, Collection<? extends T>> m_childrenSupplier;

  protected DepthFirstTraversal(IDepthFirstTreeVisitor<T> visitor, Function<T, Collection<? extends T>> childrenSupplier) {
    m_visitor = visitor;
    m_childrenSupplier = childrenSupplier;
  }

  @Override
  public TreeVisitResult traverse(T root) {
    return doVisitInternal(assertNotNull(root), new HashSet<>(), 0, 0);
  }

  protected TreeVisitResult doVisitInternal(T toVisit, Set<T> alreadyVisited, int level, int index) {
    if (alreadyVisited.contains(toVisit)) {
      return TreeVisitResult.CONTINUE; // skip this branch as it is already visited
    }

    TreeVisitResult nextAction = m_visitor.preVisit(toVisit, level, index);
    alreadyVisited.add(toVisit); // remember already visited

    if (nextAction == TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }

    if (nextAction != TreeVisitResult.SKIP_SUBTREE) {
      TreeVisitResult childResult = visitChildren(toVisit, alreadyVisited, level + 1);
      if (childResult == TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
    }

    boolean continueVisit = m_visitor.postVisit(toVisit, level, index);
    if (!continueVisit) {
      return TreeVisitResult.TERMINATE;
    }

    return nextAction;
  }

  protected TreeVisitResult visitChildren(T parent, Set<T> alreadyVisited, int level) {
    Collection<? extends T> children = m_childrenSupplier.apply(parent);
    if (isEmpty(children)) {
      return TreeVisitResult.CONTINUE;
    }

    int i = 0;
    for (T child : children) {
      if (child == null) {
        continue;
      }

      TreeVisitResult nextAction = doVisitInternal(child, alreadyVisited, level, i++);
      if (nextAction == TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (nextAction == TreeVisitResult.SKIP_SIBLINGS) {
        return TreeVisitResult.CONTINUE; // skip this loop only
      }
    }
    return TreeVisitResult.CONTINUE;
  }
}
