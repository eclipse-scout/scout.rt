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
package org.eclipse.scout.rt.client.extension.ui.basic.tree;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeDisposeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeInitTreeNodeChain;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeResolveVirtualChildNodeChain;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.IVirtualTreeNode;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface ITreeNodeExtension<OWNER extends AbstractTreeNode> extends IExtension<OWNER> {

  void execDecorateCell(TreeNodeDecorateCellChain chain, Cell cell);

  void execInitTreeNode(TreeNodeInitTreeNodeChain chain);

  void execDispose(TreeNodeDisposeChain chain);

  ITreeNode execResolveVirtualChildNode(TreeNodeResolveVirtualChildNodeChain chain, IVirtualTreeNode node);

}
