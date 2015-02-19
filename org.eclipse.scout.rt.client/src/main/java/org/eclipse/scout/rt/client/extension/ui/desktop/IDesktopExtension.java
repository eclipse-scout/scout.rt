/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.desktop;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopAddTrayMenusChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopBeforeClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopGuiAttachedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopGuiDetachedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopOpenedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopOutlineChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailTableChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageSearchFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopTablePageLoadedChain;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 *
 */
public interface IDesktopExtension<DESKTOP extends AbstractDesktop> extends IExtension<DESKTOP> {

  /**
   * @param chain
   */
  void execInit(DesktopInitChain chain) throws ProcessingException;

  void execOpened(DesktopOpenedChain chain) throws ProcessingException;

  void execAddTrayMenus(DesktopAddTrayMenusChain chain, List<IMenu> menus) throws ProcessingException;

  void execBeforeClosing(DesktopBeforeClosingChain chain) throws ProcessingException;

  void execPageDetailFormChanged(DesktopPageDetailFormChangedChain chain, IForm oldForm, IForm newForm) throws ProcessingException;

  void execTablePageLoaded(DesktopTablePageLoadedChain chain, IPageWithTable<?> tablePage) throws ProcessingException;

  void execOutlineChanged(DesktopOutlineChangedChain chain, IOutline oldOutline, IOutline newOutline) throws ProcessingException;

  void execClosing(DesktopClosingChain chain) throws ProcessingException;

  void execPageSearchFormChanged(DesktopPageSearchFormChangedChain chain, IForm oldForm, IForm newForm) throws ProcessingException;

  void execPageDetailTableChanged(DesktopPageDetailTableChangedChain chain, ITable oldTable, ITable newTable) throws ProcessingException;

  void execGuiAttached(DesktopGuiAttachedChain chain) throws ProcessingException;

  void execGuiDetached(DesktopGuiDetachedChain chain) throws ProcessingException;

}
