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
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IDesktopExtension<DESKTOP extends AbstractDesktop> extends IExtension<DESKTOP> {

  void execInit(DesktopInitChain chain);

  void execOpened(DesktopOpenedChain chain);

  void execBeforeClosing(DesktopBeforeClosingChain chain);

  void execPageDetailFormChanged(DesktopPageDetailFormChangedChain chain, IForm oldForm, IForm newForm);

  void execTablePageLoaded(DesktopTablePageLoadedChain chain, IPageWithTable<?> tablePage);

  void execOutlineChanged(DesktopOutlineChangedChain chain, IOutline oldOutline, IOutline newOutline);

  IForm execFormAboutToShow(DesktopFormAboutToShowChain chain, IForm form);

  void execClosing(DesktopClosingChain chain);

  void execPageSearchFormChanged(DesktopPageSearchFormChangedChain chain, IForm oldForm, IForm newForm);

  void execPageDetailTableChanged(DesktopPageDetailTableChangedChain chain, ITable oldTable, ITable newTable);

  void execGuiAttached(DesktopGuiAttachedChain chain);

  void execGuiDetached(DesktopGuiDetachedChain chain);

  void execDefaultView(DesktopDefaultViewChain chain);

  void execLogoAction(DesktopLogoActionChain chain);

}
