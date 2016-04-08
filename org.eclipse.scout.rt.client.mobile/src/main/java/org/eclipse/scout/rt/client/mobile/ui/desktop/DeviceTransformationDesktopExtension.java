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
package org.eclipse.scout.rt.client.mobile.ui.desktop;

import java.util.Collection;

import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileBackAction;
import org.eclipse.scout.rt.client.mobile.navigation.AbstractMobileHomeAction;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigationService;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformer;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

public class DeviceTransformationDesktopExtension extends AbstractDesktopExtension {
  private boolean m_active;
  private IDeviceTransformer m_deviceTransformer;

  public DeviceTransformationDesktopExtension() {
    setActive(UserAgentUtility.isTouchDevice());
  }

  public boolean isActive() {
    return m_active;
  }

  public void setActive(boolean active) {
    m_active = active;
  }

  @Override
  public void setCoreDesktop(IDesktop desktop) {
    super.setCoreDesktop(desktop);

    if (isActive()) {
      BEANS.get(IBreadCrumbsNavigationService.class).install(getCoreDesktop());
      BEANS.get(IDeviceTransformationService.class).install(getCoreDesktop());
    }
  }

  public IDeviceTransformer getDeviceTransformer() {
    if (m_deviceTransformer == null) {
      m_deviceTransformer = BEANS.get(IDeviceTransformationService.class).getDeviceTransformer();
    }

    return m_deviceTransformer;
  }

  @Override
  protected ContributionCommand execInit() {
    if (!isActive()) {
      return super.execInit();
    }

    getDeviceTransformer().transformDesktop();
    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execClosing() {
    if (!isActive()) {
      return super.execClosing();
    }

    getDeviceTransformer().notifyDesktopClosing();
    return ContributionCommand.Continue;
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    if (!isActive()) {
      return;
    }

    getDeviceTransformer().adaptDesktopActions(actions);
    super.contributeActions(actions);
  }

  @Override
  public void contributeOutlines(OrderedCollection<IOutline> outlines) {
    if (!isActive()) {
      return;
    }

    getDeviceTransformer().adaptDesktopOutlines(outlines);
    super.contributeOutlines(outlines);
  }

  @Override
  protected ContributionCommand execOutlineChanged(IOutline oldOutline, IOutline newOutline) {
    if (!isActive()) {
      return super.execOutlineChanged(oldOutline, newOutline);
    }

    getDeviceTransformer().transformOutline(newOutline);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) {
    if (!isActive()) {
      return super.execPageDetailTableChanged(oldTable, newTable);
    }

    getDeviceTransformer().transformPageDetailTable(newTable);

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execFormAboutToShow(IHolder<IForm> formHolder) {
    if (!isActive()) {
      return super.execFormAboutToShow(formHolder);
    }

    IForm form = formHolder.getValue();
    if (form == null) {
      return ContributionCommand.Stop;
    }

    if (!getDeviceTransformer().acceptFormAddingToDesktop(form)) {
      formHolder.setValue(null);
      return ContributionCommand.Stop;
    }

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execTablePageLoaded(IPageWithTable<?> tablePage) {
    if (!isActive()) {
      return super.execTablePageLoaded(tablePage);
    }

    getDeviceTransformer().notifyTablePageLoaded(tablePage);

    return ContributionCommand.Continue;
  }

  @Order(10)
  public class BackAction extends AbstractMobileBackAction {

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

  }

  @Order(20)
  public class HomeAction extends AbstractMobileHomeAction {

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

  }

}
