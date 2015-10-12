package org.eclipse.scout.rt.client.extension.ui.form.fields.treefield;

import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
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
  public void execSaveDeletedNode(TreeFieldSaveDeletedNodeChain chain, ITreeNode row) throws ProcessingException {
    chain.execSaveDeletedNode(row);
  }

  @Override
  public void execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain chain, ITreeNode row) throws ProcessingException {
    chain.execSaveUpdatedNode(row);
  }

  @Override
  public void execLoadChildNodes(TreeFieldLoadChildNodesChain chain, ITreeNode parentNode) throws ProcessingException {
    chain.execLoadChildNodes(parentNode);
  }

  @Override
  public void execSaveInsertedNode(TreeFieldSaveInsertedNodeChain chain, ITreeNode row) throws ProcessingException {
    chain.execSaveInsertedNode(row);
  }
}
