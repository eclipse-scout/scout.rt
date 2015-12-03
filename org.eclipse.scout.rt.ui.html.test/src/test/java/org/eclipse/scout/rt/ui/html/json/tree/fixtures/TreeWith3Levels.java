/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

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
