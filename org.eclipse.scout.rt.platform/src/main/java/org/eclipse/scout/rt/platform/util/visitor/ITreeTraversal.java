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

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Represents a specific tree traversal operation. This includes the type of traverse and the action performed during
 * traversal.
 *
 * @since 8.0
 * @see TreeTraversals
 * @see TreeVisitResult
 */
public interface ITreeTraversal<T> {
  /**
   * Creates a new {@link ITreeTraversal} using a <i>Breadth-First</i> traversal strategy.
   *
   * @param root
   *          The root node where to start the traversal. Must not be {@code null}.
   * @return The result from the last call to the visitor that is used by this {@link ITreeTraversal}.
   * @throws AssertionException
   *           if the root is {@code null}.
   * @see TreeVisitResult
   * @see TreeTraversals
   */
  TreeVisitResult traverse(T root);
}
