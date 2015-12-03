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
package org.eclipse.scout.rt.client.extension.ui.form.fields.treebox;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
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
  public void execFilterNewNode(TreeBoxFilterNewNodeChain<T> chain, ITreeNode newNode, int treeLevel) {
    chain.execFilterNewNode(newNode, treeLevel);
  }

  @Override
  public void execLoadChildNodes(TreeBoxLoadChildNodesChain<T> chain, ITreeNode parentNode) {
    chain.execLoadChildNodes(parentNode);
  }

  @Override
  public void execPrepareLookup(TreeBoxPrepareLookupChain<T> chain, ILookupCall<T> call, ITreeNode parent) {
    chain.execPrepareLookup(call, parent);
  }

  @Override
  public void execFilterLookupResult(TreeBoxFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) {
    chain.execFilterLookupResult(call, result);
  }
}
