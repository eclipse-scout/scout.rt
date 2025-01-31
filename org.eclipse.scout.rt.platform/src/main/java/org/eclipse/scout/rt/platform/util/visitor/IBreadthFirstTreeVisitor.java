/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.visitor;

/**
 * Visitor to traverse an element tree in a <i>Breadth-First</i> (BFS) strategy.
 *
 * @see TreeVisitResult
 * @see TreeTraversals
 * @since 8.0
 */
@FunctionalInterface
public interface IBreadthFirstTreeVisitor<T> {

  /**
   * Visit callback.
   *
   * @param element
   *     The visited element. Is never {@code null}.
   * @param level
   *     The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *     Their children {@code 1} and so on.
   * @param index
   *     The index of the current element in the list of children of the parent. The root element on which the
   *     visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult visit(T element, int level, int index);
}
