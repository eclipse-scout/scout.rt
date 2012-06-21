/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileHomeAction;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformer;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.service.SERVICES;

public class MobileDesktopExtension extends AbstractDesktopExtension {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileDesktopExtension.class);

  private boolean m_active;
  private IDeviceTransformer m_deviceTransformer;

  public MobileDesktopExtension() {
    setActive(UserAgentUtility.isTouchDevice());
  }

  public boolean isActive() {
    return m_active;
  }

  public void setActive(boolean active) {
    m_active = active;
  }

  public IDeviceTransformer getDeviceTransformer() {
    if (m_deviceTransformer == null) {
      m_deviceTransformer = SERVICES.getService(IDeviceTransformationService.class).getDeviceTransformer();
    }

    return m_deviceTransformer;
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    if (!isActive()) {
      return;
    }

    //remove outline buttons, keystrokes and Menus
    //FIXME CGU move to device transformer
    for (Iterator<IAction> iterator = actions.iterator(); iterator.hasNext();) {
      IAction action = iterator.next();
      if (action instanceof IViewButton || action instanceof IKeyStroke || action instanceof IMenu) {
        iterator.remove();
      }
    }
    super.contributeActions(actions);
  }

  @Override
  protected ContributionCommand execInit() throws ProcessingException {
    if (!isActive()) {
      return super.execInit();
    }

    getDeviceTransformer().desktopInit(getCoreDesktop());

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execGuiAttached() throws ProcessingException {
    if (!isActive()) {
      return super.execGuiAttached();
    }

    getDeviceTransformer().desktopGuiAttached();

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execGuiDetached() throws ProcessingException {
    if (!isActive()) {
      return super.execGuiDetached();
    }

    getDeviceTransformer().desktopGuiDetached();

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execOutlineChanged(IOutline oldOutline, IOutline newOutline) throws ProcessingException {
    if (!isActive()) {
      return super.execOutlineChanged(oldOutline, newOutline);
    }

    getDeviceTransformer().transformOutline(newOutline);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) throws ProcessingException {
    if (!isActive()) {
      return super.execPageDetailTableChanged(oldTable, newTable);
    }

    getDeviceTransformer().transformPageDetailTable(newTable);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execCustomFormModification(IHolder<IForm> formHolder) {
    if (!isActive()) {
      return super.execCustomFormModification(formHolder);
    }

    IForm form = formHolder.getValue();
    if (form == null) {
      return ContributionCommand.Stop;
    }

    if (!getDeviceTransformer().acceptForm(form)) {
      formHolder.setValue(null);
      return ContributionCommand.Stop;
    }

    getDeviceTransformer().transformForm(form);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execTablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException {
    if (!isActive()) {
      return super.execTablePageLoaded(tablePage);
    }

    getDeviceTransformer().tablePageLoaded(tablePage);

    return ContributionCommand.Continue;
  }

  @Order(10)
  public class BackViewButton extends AbstractMobileBackAction {

    @Override
    protected void execInitAction() throws ProcessingException {
      init(getCoreDesktop());
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

  }

  @Order(15)
  public class SeparatorMenu extends AbstractMenu {
    @Override
    protected boolean getConfiguredSeparator() {
      return true;
    }
  }

  @Order(20)
  public class HomeViewButton extends AbstractMobileHomeAction {

    @Override
    protected void execInitAction() throws ProcessingException {
      init(getCoreDesktop());
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

  }

  @Order(30.0)
  public class LogoutMenu extends AbstractMenu {

    @Override
    protected boolean getConfiguredSingleSelectionAction() {
      return true;
    }

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Logoff");
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

    @Override
    protected void execAction() throws ProcessingException {
      ClientJob.getCurrentSession().stopSession();
    }
  }

}
