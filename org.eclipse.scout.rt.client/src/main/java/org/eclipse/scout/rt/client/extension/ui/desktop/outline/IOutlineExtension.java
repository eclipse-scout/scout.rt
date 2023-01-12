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
