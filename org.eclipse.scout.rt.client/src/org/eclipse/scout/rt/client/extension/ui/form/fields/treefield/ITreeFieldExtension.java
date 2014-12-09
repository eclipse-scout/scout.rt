package org.eclipse.scout.rt.client.extension.ui.form.fields.treefield;

import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
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

  void execSaveDeletedNode(TreeFieldSaveDeletedNodeChain chain, ITreeNode row) throws ProcessingException;

  void execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain chain, ITreeNode row) throws ProcessingException;

  void execLoadChildNodes(TreeFieldLoadChildNodesChain chain, ITreeNode parentNode) throws ProcessingException;

  void execSaveInsertedNode(TreeFieldSaveInsertedNodeChain chain, ITreeNode row) throws ProcessingException;
}
