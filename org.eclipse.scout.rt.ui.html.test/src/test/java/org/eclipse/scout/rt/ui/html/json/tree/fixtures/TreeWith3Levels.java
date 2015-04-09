package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

public class TreeWith3Levels extends AbstractTree {

  @Override
  protected void execInitTree() throws ProcessingException {
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
