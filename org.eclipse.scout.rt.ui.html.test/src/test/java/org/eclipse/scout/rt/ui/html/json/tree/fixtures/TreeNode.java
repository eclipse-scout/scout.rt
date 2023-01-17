/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;

public class TreeNode extends AbstractTreeNode {

  public TreeNode() {
    super();
  }

  public TreeNode(String text) {
    super(false);
    getCellForUpdate().setText(text);
    callInitializer();
  }
}
