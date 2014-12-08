package org.eclipse.scout.rt.client.extension.ui.form.fields.treebox;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxAutoCheckChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxFilterNewNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxLoadChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractTreeBoxExtension<T, OWNER extends AbstractTreeBox<T>> extends AbstractValueFieldExtension<Set<T>, OWNER> implements ITreeBoxExtension<T, OWNER> {

  public AbstractTreeBoxExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execFilterNewNode(TreeBoxFilterNewNodeChain<T> chain, ITreeNode newNode, int treeLevel) throws ProcessingException {
    chain.execFilterNewNode(newNode, treeLevel);
  }

  @Override
  public void execLoadChildNodes(TreeBoxLoadChildNodesChain<T> chain, ITreeNode parentNode) throws ProcessingException {
    chain.execLoadChildNodes(parentNode);
  }

  @Override
  public void execPrepareLookup(TreeBoxPrepareLookupChain<T> chain, ILookupCall<T> call, ITreeNode parent) throws ProcessingException {
    chain.execPrepareLookup(call, parent);
  }

  @Override
  public void execFilterLookupResult(TreeBoxFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) throws ProcessingException {
    chain.execFilterLookupResult(call, result);
  }

  @Override
  public void execAutoCheckChildNodes(TreeBoxAutoCheckChildNodesChain<T> chain, ITreeNode node, boolean value) {
    chain.execAutoCheckChildNodes(node, value);
  }
}
