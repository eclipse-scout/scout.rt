/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop;

import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopBeforeClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopDefaultViewChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopFormAboutToShowChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopGuiAttachedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopGuiDetachedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopLogoActionChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopOpenedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopOutlineChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailTableChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageSearchFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopTablePageLoadedChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractDesktopExtension<DESKTOP extends AbstractDesktop> extends AbstractExtension<DESKTOP> implements IDesktopExtension<DESKTOP> {

  public AbstractDesktopExtension(DESKTOP owner) {
    super(owner);
  }

  @Override
  public void execInit(DesktopInitChain chain) {
    chain.execInit();
  }

  @Override
  public void execOpened(DesktopOpenedChain chain) {
    chain.execOpened();
  }

  @Override
  public void execBeforeClosing(DesktopBeforeClosingChain chain) {
    chain.execBeforeClosing();
  }

  @Override
  public void execPageDetailFormChanged(DesktopPageDetailFormChangedChain chain, IForm oldForm, IForm newForm) {
    chain.execPageDetailFormChanged(oldForm, newForm);
  }

  @Override
  public void execTablePageLoaded(DesktopTablePageLoadedChain chain, IPageWithTable<?> tablePage) {
    chain.execTablePageLoaded(tablePage);
  }

  @Override
  public void execOutlineChanged(DesktopOutlineChangedChain chain, IOutline oldOutline, IOutline newOutline) {
    chain.execOutlineChanged(oldOutline, newOutline);
  }

  @Override
  public IForm execFormAboutToShow(DesktopFormAboutToShowChain chain, IForm form) {
    return chain.execFormAboutToShow(form);
  }

  @Override
  public void execClosing(DesktopClosingChain chain) {
    chain.execClosing();
  }

  @Override
  public void execPageSearchFormChanged(DesktopPageSearchFormChangedChain chain, IForm oldForm, IForm newForm) {
    chain.execPageSearchFormChanged(oldForm, newForm);
  }

  @Override
  public void execPageDetailTableChanged(DesktopPageDetailTableChangedChain chain, ITable oldTable, ITable newTable) {
    chain.execPageDetailTableChanged(oldTable, newTable);
  }

  @Override
  public void execGuiAttached(DesktopGuiAttachedChain chain) {
    chain.execGuiAttached();
  }

  @Override
  public void execGuiDetached(DesktopGuiDetachedChain chain) {
    chain.execGuiDetached();
  }

  @Override
  public void execDefaultView(DesktopDefaultViewChain chain) {
    chain.execDefaultView();
  }

  @Override
  public void execLogoAction(DesktopLogoActionChain chain) {
    chain.execLogoAction();
  }
}
