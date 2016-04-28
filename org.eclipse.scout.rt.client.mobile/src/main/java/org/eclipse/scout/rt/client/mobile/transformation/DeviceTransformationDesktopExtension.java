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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.IHolder;

public class DeviceTransformationDesktopExtension extends AbstractDesktopExtension {
  private IDeviceTransformer m_deviceTransformer;

  public DeviceTransformationDesktopExtension() {
  }

  public IDeviceTransformer getDeviceTransformer() {
    return m_deviceTransformer;
  }

  @Override
  public void setCoreDesktop(IDesktop desktop) {
    super.setCoreDesktop(desktop);

    IDeviceTransformationService transformationService = BEANS.get(IDeviceTransformationService.class);
    transformationService.install(getCoreDesktop());
    m_deviceTransformer = transformationService.getDeviceTransformer();
  }

  @Override
  protected ContributionCommand execInit() {
    getDeviceTransformer().transformDesktop();
    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execClosing() {
    getDeviceTransformer().notifyDesktopClosing();
    return ContributionCommand.Continue;
  }

  @Override
  public void contributeActions(Collection<IAction> actions) {
    getDeviceTransformer().adaptDesktopActions(actions);
    super.contributeActions(actions);
  }

  @Override
  protected ContributionCommand execPageDetailTableChanged(ITable oldTable, ITable newTable) {
    getDeviceTransformer().transformPageDetailTable(newTable);
    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execFormAboutToShow(IHolder<IForm> formHolder) {
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
    getDeviceTransformer().notifyTablePageLoaded(tablePage);
    return ContributionCommand.Continue;
  }

}
