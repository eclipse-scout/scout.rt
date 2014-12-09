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
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 *
 */
public interface ITreeExtension<OWNER extends AbstractTree> extends IExtension<OWNER> {

  void execDrop(TreeDropChain chain, ITreeNode node, TransferObject t) throws ProcessingException;

  void execInitTree(TreeInitTreeChain chain) throws ProcessingException;

  void execDropTargetChanged(TreeDropTargetChangedChain chain, ITreeNode node) throws ProcessingException;

  TransferObject execDrag(TreeDragNodesChain chain, Collection<ITreeNode> nodes) throws ProcessingException;

  void execNodeAction(TreeNodeActionChain chain, ITreeNode node) throws ProcessingException;

  void execNodeClick(TreeNodeClickChain chain, ITreeNode node, MouseButton mouseButton) throws ProcessingException;

  void execHyperlinkAction(TreeHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException;

  void execNodesSelected(TreeNodesSelectedChain chain, TreeEvent e) throws ProcessingException;

  void execDisposeTree(TreeDisposeTreeChain chain) throws ProcessingException;

  void execDecorateCell(TreeDecorateCellChain chain, ITreeNode node, Cell cell) throws ProcessingException;

  TransferObject execDrag(TreeDragNodeChain chain, ITreeNode node) throws ProcessingException;

}
