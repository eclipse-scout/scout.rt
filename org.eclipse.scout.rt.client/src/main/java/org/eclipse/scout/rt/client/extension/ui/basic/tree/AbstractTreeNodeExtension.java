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
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTreeNodeExtension<OWNER extends AbstractTreeNode> extends AbstractExtension<OWNER> implements ITreeNodeExtension<OWNER> {

  public AbstractTreeNodeExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execDecorateCell(TreeNodeDecorateCellChain chain, Cell cell) {
    chain.execDecorateCell(cell);
  }

  @Override
  public void execInitTreeNode(TreeNodeInitTreeNodeChain chain) {
    chain.execInitTreeNode();
  }

  @Override
  public void execDispose(TreeNodeDisposeChain chain) {
    chain.execDispose();
  }

}
