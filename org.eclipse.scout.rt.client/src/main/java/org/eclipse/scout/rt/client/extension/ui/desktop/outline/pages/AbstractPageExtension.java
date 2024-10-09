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
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractPageExtension<OWNER extends AbstractPage> extends AbstractExtension<OWNER> implements IPageExtension<OWNER> {

  public AbstractPageExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execReloadPage(PageReloadPageChain chain, String reloadReason) {
    chain.execReloadPage(reloadReason);
  }

  @Override
  public void execPageDataLoaded(PagePageDataLoadedChain chain) {
    chain.execPageDataLoaded();
  }

  @Override
  public void execPageActivated(PagePageActivatedChain chain) {
    chain.execPageActivated();
  }

  @Override
  public void execDataChanged(PageDataChangedChain chain, Object... dataTypes) {
    chain.execDataChanged(dataTypes);
  }

  @Override
  public void execInitPage(PageInitPageChain chain) {
    chain.execInitPage();
  }

  @Override
  public void execPageDeactivated(PagePageDeactivatedChain chain) {
    chain.execPageDeactivated();
  }

  @Override
  public void execDisposePage(PageDisposePageChain chain) {
    chain.execDisposePage();
  }

  @Override
  public void execInitDetailForm(PageInitDetailFormChain chain) {
    chain.execInitDetailForm();
  }

  @Override
  public void execInitTable(PageInitTableChain chain) {
    chain.execInitTable();
  }

  @Override
  public void execDetailFormActivated(PageDetailFormActivatedChain chain) {
    chain.execDetailFormActivated();
  }

  @Override
  public boolean execCalculateVisible(PageCalculateVisibleChain chain) {
    return chain.execCalculateVisible();
  }

  @Override
  public List<IMenu> execComputeParentTablePageMenus(ComputeParentTablePageMenusChain chain, IPageWithTable<?> parentTablePage) {
    return chain.execComputeParentTablePageMenus(parentTablePage);
  }
}
