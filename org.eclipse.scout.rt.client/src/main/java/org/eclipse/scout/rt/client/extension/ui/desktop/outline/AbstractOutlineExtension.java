/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateChildPagesChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateRootPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineDeactivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineInitDefaultDetailFormChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public abstract class AbstractOutlineExtension<OWNER extends AbstractOutline> extends AbstractTreeExtension<OWNER> implements IOutlineExtension<OWNER> {

  public AbstractOutlineExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execActivated(OutlineActivatedChain chain) {
    chain.execActivated();
  }

  @Override
  public void execDeactivated(OutlineDeactivatedChain chain) {
    chain.execDeactivated();
  }

  @Override
  public void execCreateChildPages(OutlineCreateChildPagesChain chain, List<IPage<?>> pageList) {
    chain.execCreateChildPages(pageList);
  }

  @Override
  public IPage<?> execCreateRootPage(OutlineCreateRootPageChain chain) {
    return chain.execCreateRootPage();
  }

  @Override
  public void execNodesChecked(TreeNodesCheckedChain chain, List<ITreeNode> nodes) {
    chain.execNodesChecked(nodes);
  }

  @Override
  public void execInitDefaultDetailForm(OutlineInitDefaultDetailFormChain chain) {
    chain.execInitDefaultDetailForm();
  }

}
