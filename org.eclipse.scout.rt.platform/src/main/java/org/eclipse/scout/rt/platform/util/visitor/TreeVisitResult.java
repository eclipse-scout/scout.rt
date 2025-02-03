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

import java.util.function.Function;

/**
 * Controls how the tree visiting should continue.
 *
 * @see IBreadthFirstTreeVisitor
 * @see IDepthFirstTreeVisitor
 * @see ITreeTraversal
 * @see TreeTraversals
 * @since 8.0
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
   * current {@link TreeVisitResult}.
   */
  public <S> Function<S, TreeVisitResult> asFunction() {
    return o -> this;
  }
}
