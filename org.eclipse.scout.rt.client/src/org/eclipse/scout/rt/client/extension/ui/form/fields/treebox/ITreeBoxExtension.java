package org.eclipse.scout.rt.client.extension.ui.form.fields.treebox;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxAutoCheckChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxFilterNewNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxLoadChildNodesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.TreeBoxChains.TreeBoxPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public interface ITreeBoxExtension<T, OWNER extends AbstractTreeBox<T>> extends IValueFieldExtension<Set<T>, OWNER> {

  void execFilterNewNode(TreeBoxFilterNewNodeChain<T> chain, ITreeNode newNode, int treeLevel) throws ProcessingException;

  void execLoadChildNodes(TreeBoxLoadChildNodesChain<T> chain, ITreeNode parentNode) throws ProcessingException;

  void execPrepareLookup(TreeBoxPrepareLookupChain<T> chain, ILookupCall<T> call, ITreeNode parent) throws ProcessingException;

  void execFilterLookupResult(TreeBoxFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) throws ProcessingException;

  void execAutoCheckChildNodes(TreeBoxAutoCheckChildNodesChain<T> chain, ITreeNode node, boolean value);
}
