/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopClosingChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopFormAboutToShowChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailFormChangedChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopPageDetailTableChangedChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;

public class DesktopExtension extends AbstractDesktopExtension<AbstractDesktop> {

  public DesktopExtension(AbstractDesktop owner) {
    super(owner);
  }

  @Override
  public void execInit(DesktopInitChain chain) {
    super.execInit(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformDesktop();
  }

  @Override
  public void execClosing(DesktopClosingChain chain) {
    super.execClosing(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().notifyDesktopClosing();
  }

  @Override
  public void execPageDetailFormChanged(DesktopPageDetailFormChangedChain chain, IForm oldForm, IForm newForm) {
    super.execPageDetailFormChanged(chain, oldForm, newForm);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().notifyPageDetailFormChanged(newForm);
  }

  @Override
  public void execPageDetailTableChanged(DesktopPageDetailTableChangedChain chain, ITable oldTable, ITable newTable) {
    super.execPageDetailTableChanged(chain, oldTable, newTable);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().notifyPageDetailTableChanged(newTable);
  }

  @Override
  public IForm execFormAboutToShow(DesktopFormAboutToShowChain chain, IForm form) {
    form = super.execFormAboutToShow(chain, form);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().notifyFormAboutToShow(form);
    return form;
  }
}
