package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;

public class TreeWithOneNode extends AbstractTree {

  @Override
  protected void execInitTree() {
    super.execInitTree();
    addChildNode(getRootNode(), new TreeNode());
  }

}
