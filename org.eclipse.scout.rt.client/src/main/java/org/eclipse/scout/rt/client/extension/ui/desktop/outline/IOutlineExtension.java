/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateChildPagesChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineCreateRootPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineDeactivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.OutlineChains.OutlineInitDefaultDetailFormChain;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

public interface IOutlineExtension<OWNER extends AbstractOutline> extends ITreeExtension<OWNER> {

  void execCreateChildPages(OutlineCreateChildPagesChain chain, List<IPage<?>> pageList);

  IPage<?> execCreateRootPage(OutlineCreateRootPageChain chain);

  void execInitDefaultDetailForm(OutlineInitDefaultDetailFormChain chain);

  void execActivated(OutlineActivatedChain chain);

  void execDeactivated(OutlineDeactivatedChain chain);
}
