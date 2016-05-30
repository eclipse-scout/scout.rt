package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.extension.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopClosingChain;
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
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformPageDetailForm(newForm);
  }

  @Override
  public void execPageDetailTableChanged(DesktopPageDetailTableChangedChain chain, ITable oldTable, ITable newTable) {
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformPageDetailTable(newTable);
  }
}
