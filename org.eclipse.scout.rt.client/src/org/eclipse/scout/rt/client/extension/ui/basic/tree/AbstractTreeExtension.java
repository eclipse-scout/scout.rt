/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import java.net.URL;
import java.util.Collection;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
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
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

/**
 *
 */
public abstract class AbstractTreeExtension<OWNER extends AbstractTree> extends AbstractExtension<OWNER> implements ITreeExtension<OWNER> {

  public AbstractTreeExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execDrop(TreeDropChain chain, ITreeNode node, TransferObject t) throws ProcessingException {
    chain.execDrop(node, t);
  }

  @Override
  public void execInitTree(TreeInitTreeChain chain) throws ProcessingException {
    chain.execInitTree();
  }

  @Override
  public void execDropTargetChanged(TreeDropTargetChangedChain chain, ITreeNode node) throws ProcessingException {
    chain.execDropTargetChanged(node);
  }

  @Override
  public TransferObject execDrag(TreeDragNodesChain chain, Collection<ITreeNode> nodes) throws ProcessingException {
    return chain.execDrag(nodes);
  }

  @Override
  public void execNodeAction(TreeNodeActionChain chain, ITreeNode node) throws ProcessingException {
    chain.execNodeAction(node);
  }

  @Override
  public void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton) throws ProcessingException {
    chain.execNodeClick(node, mouseButton);
  }

  @Override
  public void execHyperlinkAction(TreeHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
    chain.execHyperlinkAction(url, path, local);
  }

  @Override
  public void execNodesSelected(TreeNodesSelectedChain chain, TreeEvent e) throws ProcessingException {
    chain.execNodesSelected(e);
  }

  @Override
  public void execDisposeTree(TreeDisposeTreeChain chain) throws ProcessingException {
    chain.execDisposeTree();
  }

  @Override
  public void execDecorateCell(TreeDecorateCellChain chain, ITreeNode node, Cell cell) throws ProcessingException {
    chain.execDecorateCell(node, cell);
  }

  @Override
  public TransferObject execDrag(TreeDragNodeChain chain, ITreeNode node) throws ProcessingException {
    return chain.execDrag(node);
  }

}
