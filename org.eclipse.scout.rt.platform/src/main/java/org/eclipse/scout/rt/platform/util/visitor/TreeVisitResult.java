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

import java.util.function.Function;

/**
 * Controls how the tree visiting should continue.
 *
 * @since 8.0
 * @see IBreadthFirstTreeVisitor
 * @see IDepthFirstTreeVisitor
 * @see ITreeTraversal
 * @see TreeTraversals
 */
public enum TreeVisitResult {
  /**
   * Normally continue visiting. Nothing will be skipped.
   */
  CONTINUE,

  /**
   * Abort the whole visiting. May be used if the visitor finishes the operation before all elements have been visited.
   */
  TERMINATE,

  /**
   * Continue without visiting the child elements of the current element.
   */
  SKIP_SUBTREE,

  /**
   * All siblings of the current element are skipped. The current element is visited completely (including children).
   */
  SKIP_SIBLINGS;

  /**
   * @return Converts this {@link TreeVisitResult} value to a function that takes any input and always returns the
   *         current {@link TreeVisitResult}.
   */
  public <S> Function<S, TreeVisitResult> asFunction() {
    return o -> this;
  }
}
