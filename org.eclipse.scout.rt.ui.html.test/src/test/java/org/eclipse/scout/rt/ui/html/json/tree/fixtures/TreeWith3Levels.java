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

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("fc9c957c-eac6-400a-b998-b9cf37244b6e")
public class TreeWith3Levels extends AbstractTree {

  @Override
  protected void execInitTree() {
    super.execInitTree();
    ITreeNode node0 = new TreeNode();
    addChildNode(getRootNode(), node0);

    addChildNode(node0, new TreeNode());
    addChildNode(node0, new TreeNode());
    ITreeNode child1 = node0.getChildNodes().get(1);
    addChildNode(child1, new TreeNode());
    addChildNode(child1, new TreeNode());
  }
}
