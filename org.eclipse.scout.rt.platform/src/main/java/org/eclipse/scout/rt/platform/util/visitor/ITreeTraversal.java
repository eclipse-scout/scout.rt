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

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Represents a specific tree traversal operation. This includes the type of traverse and the action performed during
 * traversal.
 *
 * @see TreeTraversals
 * @see TreeVisitResult
 * @since 8.0
 */
public interface ITreeTraversal<T> {
  /**
   * Traverses the tree rooted by the specified element. The traversal uses the strategy and visitor as specified during
   * the creation of this {@link ITreeTraversal}.
   *
   * @param root
   *     The root node where to start the traversal. Must not be {@code null}.
   * @return The result from the last call to the visitor that is used by this {@link ITreeTraversal}.
   * @throws AssertionException
   *     if the root is {@code null}.
   * @see TreeVisitResult
   * @see TreeTraversals
   */
  TreeVisitResult traverse(T root);
}
