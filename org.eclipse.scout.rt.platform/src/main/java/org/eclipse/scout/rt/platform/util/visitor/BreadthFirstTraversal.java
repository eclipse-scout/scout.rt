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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class BreadthFirstTraversal<T> implements ITreeTraversal<T> {

  private final IBreadthFirstTreeVisitor<T> m_visitor;
  private final Function<T, Collection<? extends T>> m_childrenSupplier;

  protected BreadthFirstTraversal(IBreadthFirstTreeVisitor<T> visitor, Function<T, Collection<? extends T>> childrenSupplier) {
    m_visitor = visitor;
    m_childrenSupplier = childrenSupplier;
  }

  @Override
  public TreeVisitResult traverse(T root) {
    assertNotNull(root);
    Deque<P_BreadthFirstNode<T>> dek = new ArrayDeque<>();
    Set<T> alreadyEnqueued = new HashSet<>();
    enqueue(dek, alreadyEnqueued, root, 0, 0);

    while (!dek.isEmpty()) {
      P_BreadthFirstNode<T> node = dek.poll();
      TreeVisitResult nextAction = m_visitor.visit(node.m_element, node.m_level, node.m_index);
      if (nextAction == TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
      if (nextAction == TreeVisitResult.SKIP_SIBLINGS) {
        // remove all heads with same level until first next level
        removeQueuedSiblings(dek, node.m_level);
      }

      if (nextAction != TreeVisitResult.SKIP_SUBTREE) {
        // nextAction can only be 'continue' here
        enqueueChildren(dek, alreadyEnqueued, node);
      }
    }
    return TreeVisitResult.CONTINUE;
  }

  private void enqueueChildren(Deque<P_BreadthFirstNode<T>> dek, Set<T> alreadyEnqueued, P_BreadthFirstNode<T> node) {
    Collection<? extends T> children = m_childrenSupplier.apply(node.m_element);
    if (isEmpty(children)) {
      return;
    }

    int i = 0;
    for (T child : children) { // don't use indexed for loop because children list may not be a random access list.
      if (child == null) {
        continue;
      }
      enqueue(dek, alreadyEnqueued, child, node.m_level + 1, i);
      i++;
    }
  }

  protected void enqueue(Deque<P_BreadthFirstNode<T>> dek, Set<T> alreadyEnqueued, T element, int level, int index) {
    if (alreadyEnqueued.contains(element)) {
      return;
    }

    P_BreadthFirstNode<T> e = new P_BreadthFirstNode<>(element, level, index);
    dek.addLast(e);
    alreadyEnqueued.add(element);
  }

  protected void removeQueuedSiblings(Deque<P_BreadthFirstNode<T>> dek, int level) {
    Iterator<P_BreadthFirstNode<T>> iterator = dek.iterator();
    while (iterator.hasNext()) {
      P_BreadthFirstNode<T> siblingCandidate = iterator.next();
      if (siblingCandidate.m_index > 0) {
        iterator.remove();
      }
      else {
        return;
      }
    }
  }

  protected static class P_BreadthFirstNode<T> {
    private final int m_level;
    private final T m_element;
    private final int m_index;

    protected P_BreadthFirstNode(T element, int level, int index) {
      m_element = element;
      m_level = level;
      m_index = index;
    }
  }
}
