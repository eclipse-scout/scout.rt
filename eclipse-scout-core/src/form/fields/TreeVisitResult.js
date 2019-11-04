/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/**
 * Controls how the tree visiting should continue.
 */
const TreeVisitResult = {
  /**
   * Normally continue visiting. Nothing will be skipped.
   */
  CONTINUE: 'continue',

  /**
   * Abort the whole visiting. May be used if the visitor finishes the operation before all elements have been visited.
   */
  TERMINATE: 'terminate',

  /**
   * Continue without visiting the child elements of the current element.
   */
  SKIP_SUBTREE: 'skip_subtree'
};

export default TreeVisitResult;
