/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.visitor;

/**
 * Visitor to traverse an element tree in a <i>Depth-First</i> (DFS) strategy.<br>
 * Using this visitor a pre-order (top-down) or post-order (bottom-op) traversal can be implemented.
 *
 * @since 7.1
 * @see TreeVisitResult
 * @see TreeTraversals
 */
public interface IDepthFirstTreeVisitor<T> {

  /**
   * Callback to implement a pre-order traversal.
   *
   * @param element
   *          The visited element. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(T, int, int)} will not be called
   *         for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(T element, int level, int index);

  /**
   * Callback to implement a post-order traversal.
   * <p>
   * If the previous {@link #preVisit(T, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post visit
   * call will not be executed.
   *
   * @param element
   *          The visited element. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}. {@code false} if the
   *         visit should be cancelled (same as {@link TreeVisitResult#TERMINATE}.
   */
  boolean postVisit(T element, int level, int index);
}
