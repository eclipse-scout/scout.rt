/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action;

@FunctionalInterface
public interface IActionVisitor {

  /**
   * continues visiting the tree (load children if not loaded)
   */
  int CONTINUE = 1;
  /**
   * continues visiting the tree only on this branch (load children if not loaded)
   */
  int CONTINUE_BRANCH = 2;
  /**
   * stop visiting the tree; used when the mission is completed.
   */
  int CANCEL = 0;
  /**
   * continues visiting with siblings of the parent node, aboard subtree.
   */
  int CANCEL_SUBTREE = 3;

  int visit(IAction action);
}
