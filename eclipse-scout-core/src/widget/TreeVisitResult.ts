/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
/**
 * Controls how the tree visiting should continue.
 */
export enum TreeVisitResult {
  /**
   * Normally continue visiting. Nothing will be skipped.
   */
  CONTINUE = 'continue',

  /**
   * Abort the whole visiting. May be used if the visitor finishes the operation before all elements have been visited.
   */
  TERMINATE = 'terminate',

  /**
   * Continue without visiting the child elements of the current element.
   */
  SKIP_SUBTREE = 'skip_subtree'
}
