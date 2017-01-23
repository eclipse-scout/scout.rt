package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class NullDeviceTransformer implements IDeviceTransformer {

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public void dispose() {
    // NOP
  }

  @Override
  public void setDesktop(IDesktop desktop) {
    // NOP
  }

  @Override
  public void transformDesktop() {
    // NOP
  }

  @Override
  public void transformForm(IForm form) {
    // NOP
  }

  @Override
  public void transformFormField(IFormField field) {
    // NOP
  }

  @Override
  public void transformOutline(IOutline outline) {
    // NOP
  }

  @Override
  public void transformPage(IPage<?> page) {
    // NOP
  }

  @Override
  public void transformPageTable(ITable table, IPage<?> page) {
    // NOP
  }

  @Override
  public void notifyPageDetailFormChanged(IForm form) {
    // NOP
  }

  @Override
  public void notifyPageDetailTableChanged(ITable table) {
    // NOP
  }

  @Override
  public void notifyFormDisposed(IForm form) {
    // NOP
  }

  @Override
  public void notifyDesktopClosing() {
    // NOP
  }

  @Override
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
    // NOP
  }

  @Override
  public boolean isFormExcluded(IForm form) {
    return false;
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    return false;
  }

  @Override
  public boolean isGridDataDirty(IForm form) {
    return false;
  }

  @Override
  public void gridDataRebuilt(IForm form) {
    // NOP
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return null;
  }
}
