/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.ComputeParentTablePageMenusChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageCalculateVisibleChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDetailFormActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageDisposePageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitDetailFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitPageChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDataLoadedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PagePageDeactivatedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageReloadPageChain;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IReloadReason;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IPageExtension<OWNER extends AbstractPage> extends IExtension<OWNER> {

  /**
   * @param reloadReason
   *          {@link IReloadReason}
   * @since 16.1
   */
  void execReloadPage(PageReloadPageChain chain, String reloadReason);

  void execPageDataLoaded(PagePageDataLoadedChain chain);

  void execPageActivated(PagePageActivatedChain chain);

  void execDataChanged(PageDataChangedChain chain, Object... dataTypes);

  void execInitPage(PageInitPageChain chain);

  void execInitDetailForm(PageInitDetailFormChain chain);

  void execPageDeactivated(PagePageDeactivatedChain chain);

  void execDisposePage(PageDisposePageChain chain);

  void execInitTable(PageInitTableChain chain);

  void execDetailFormActivated(PageDetailFormActivatedChain chain);

  boolean execCalculateVisible(PageCalculateVisibleChain chain);

  List<IMenu> execComputeParentTablePageMenus(ComputeParentTablePageMenusChain chain, IPageWithTable<?> parentTablePage);
}
