/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface ITreeExtension<OWNER extends AbstractTree> extends IExtension<OWNER> {

  void execDrop(TreeDropChain chain, ITreeNode node, TransferObject t);

  void execInitTree(TreeInitTreeChain chain);

  void execDropTargetChanged(TreeDropTargetChangedChain chain, ITreeNode node);

  TransferObject execDrag(TreeDragNodesChain chain, Collection<ITreeNode> nodes);

  void execNodeAction(TreeNodeActionChain chain, ITreeNode node);

  void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton);

  void execNodesChecked(TreeNodesCheckedChain chain, List<ITreeNode> nodes);

  void execAppLinkAction(TreeHyperlinkActionChain chain, String ref);

  void execNodesSelected(TreeNodesSelectedChain chain, TreeEvent e);

  void execDisposeTree(TreeDisposeTreeChain chain);

  void execDecorateCell(TreeDecorateCellChain chain, ITreeNode node, Cell cell);

  TransferObject execDrag(TreeDragNodeChain chain, ITreeNode node);

}
