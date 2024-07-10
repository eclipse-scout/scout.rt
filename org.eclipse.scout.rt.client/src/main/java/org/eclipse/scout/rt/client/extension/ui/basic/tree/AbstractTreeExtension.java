/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeAutoCheckChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDisposeTreeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDragNodeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDragNodesChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeDropTargetChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeHyperlinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeInitTreeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodeActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodeClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTreeExtension<OWNER extends AbstractTree> extends AbstractExtension<OWNER> implements ITreeExtension<OWNER> {

  public AbstractTreeExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execDrop(TreeDropChain chain, ITreeNode node, TransferObject t) {
    chain.execDrop(node, t);
  }

  @Override
  public void execInitTree(TreeInitTreeChain chain) {
    chain.execInitTree();
  }

  @Override
  public void execDropTargetChanged(TreeDropTargetChangedChain chain, ITreeNode node) {
    chain.execDropTargetChanged(node);
  }

  @Override
  public TransferObject execDrag(TreeDragNodesChain chain, Collection<ITreeNode> nodes) {
    return chain.execDrag(nodes);
  }

  @Override
  public void execNodeAction(TreeNodeActionChain chain, ITreeNode node) {
    chain.execNodeAction(node);
  }

  @Override
  public void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton) {
    chain.execNodeClick(node, mouseButton);
  }

  @Override
  public void execNodesChecked(TreeNodesCheckedChain chain, List<ITreeNode> nodes) {
    chain.execNodesChecked(nodes);
  }

  @Override
  public void execAppLinkAction(TreeHyperlinkActionChain chain, String ref) {
    chain.execHyperlinkAction(ref);
  }

  @Override
  public void execNodesSelected(TreeNodesSelectedChain chain, TreeEvent e) {
    chain.execNodesSelected(e);
  }

  @Override
  public void execDisposeTree(TreeDisposeTreeChain chain) {
    chain.execDisposeTree();
  }

  @Override
  public void execDecorateCell(TreeDecorateCellChain chain, ITreeNode node, Cell cell) {
    chain.execDecorateCell(node, cell);
  }

  @Override
  public TransferObject execDrag(TreeDragNodeChain chain, ITreeNode node) {
    return chain.execDrag(node);
  }

  @Override
  public void execAutoCheckChildNodes(TreeAutoCheckChildNodesChain chain, List<ITreeNode> nodes, boolean checked, boolean enabledNodesOnly) {
    chain.execAutoCheckChildNodes(nodes, checked, enabledNodesOnly);
  }
}
