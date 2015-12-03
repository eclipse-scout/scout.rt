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
package org.eclipse.scout.rt.client.extension.ui.form.fields.treefield;

import java.util.Collection;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldLoadChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveDeletedNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveInsertedNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveUpdatedNodeChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;

public abstract class AbstractTreeFieldExtension<OWNER extends AbstractTreeField> extends AbstractFormFieldExtension<OWNER> implements ITreeFieldExtension<OWNER> {

  public AbstractTreeFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execSave(TreeFieldSaveChain chain, Collection<? extends ITreeNode> insertedNodes, Collection<? extends ITreeNode> updatedNodes, Collection<? extends ITreeNode> deletedNodes) {
    chain.execSave(insertedNodes, updatedNodes, deletedNodes);
  }

  @Override
  public void execSaveDeletedNode(TreeFieldSaveDeletedNodeChain chain, ITreeNode row) {
    chain.execSaveDeletedNode(row);
  }

  @Override
  public void execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain chain, ITreeNode row) {
    chain.execSaveUpdatedNode(row);
  }

  @Override
  public void execLoadChildNodes(TreeFieldLoadChildNodesChain chain, ITreeNode parentNode) {
    chain.execLoadChildNodes(parentNode);
  }

  @Override
  public void execSaveInsertedNode(TreeFieldSaveInsertedNodeChain chain, ITreeNode row) {
    chain.execSaveInsertedNode(row);
  }
}
