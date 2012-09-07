/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.extension.client.ui.action.menu.MenuExtensionUtility;

/**
 * {@link IDesktopExtension} contributing, modifying and removing root-level desktop menus which are provided by the
 * <code>menus</code> extension point.
 * <p/>
 * <b>Note</b>: Menus provided by this extension are wrapped by an {@link OrderedMenuWrapper}. Hence any instance and
 * class comparisons must be performed on the unwrapped menu, that is retrieved by
 * {@link OrderedMenuWrapper#getWrappedObject()}.
 * 
 * @since 3.9.0
 */
public class DesktopMenuInjectingDesktopExtension implements IDesktopExtension {

  private IDesktop m_coreDesktop;

  @Override
  public IDesktop getCoreDesktop() {
    return m_coreDesktop;
  }

  @Override
  public void setCoreDesktop(IDesktop desktop) {
    m_coreDesktop = desktop;
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    // get root menus and remove them from the action collection
    List<IMenu> menuList = new ActionFinder().findActions(new ArrayList<IAction>(actions), IMenu.class, false);
    actions.removeAll(menuList);
    // contribute menus to menuList and add them to the original actions collection
    MenuExtensionUtility.adaptMenus(getCoreDesktop(), getCoreDesktop(), menuList, true);
    actions.addAll(menuList);
  }

  @Override
  public ContributionCommand initDelegate() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand desktopOpenedDelegate() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand desktopClosingDelegate() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand guiAttachedDelegate() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand guiDetachedDelegate() throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand outlineChangedDelegate(IOutline oldOutline, IOutline newOutline) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand customFormModificationDelegate(IHolder<IForm> formHolder) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand pageSearchFormChangedDelegate(IForm oldForm, IForm newForm) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand pageDetailFormChangedDelegate(IForm oldForm, IForm newForm) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand pageDetailTableChangedDelegate(ITable oldTable, ITable newTable) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand tablePageLoadedDelegate(IPageWithTable<?> tablePage) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public ContributionCommand addTrayMenusDelegate(List<IMenu> menus) throws ProcessingException {
    return ContributionCommand.Continue;
  }

  @Override
  public void contributeOutlines(Collection<IOutline> outlines) {
  }
}
