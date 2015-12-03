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

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldLoadChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveDeletedNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveInsertedNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.TreeFieldChains.TreeFieldSaveUpdatedNodeChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;

public interface ITreeFieldExtension<OWNER extends AbstractTreeField> extends IFormFieldExtension<OWNER> {

  void execSave(TreeFieldSaveChain chain, Collection<? extends ITreeNode> insertedNodes, Collection<? extends ITreeNode> updatedNodes, Collection<? extends ITreeNode> deletedNodes);

  void execSaveDeletedNode(TreeFieldSaveDeletedNodeChain chain, ITreeNode row);

  void execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain chain, ITreeNode row);

  void execLoadChildNodes(TreeFieldLoadChildNodesChain chain, ITreeNode parentNode);

  void execSaveInsertedNode(TreeFieldSaveInsertedNodeChain chain, ITreeNode row);
}
