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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeChains.TreeNodesCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateChildPagesChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateRootPageChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public abstract class AbstractOutlineExtension<OWNER extends AbstractOutline> extends AbstractTreeExtension<OWNER> implements IOutlineExtension<OWNER> {

  public AbstractOutlineExtension(OWNER owner) {
    super(owner);
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

}
