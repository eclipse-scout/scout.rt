package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

//SONAR:OFF
public class NullDeviceTransformer implements IDeviceTransformer {

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public void dispose() {
  }

  @Override
  public void setDesktop(IDesktop desktop) {
  }

  @Override
  public void transformDesktop() {
  }

  @Override
  public void transformForm(IForm form) {
  }

  @Override
  public void transformFormField(IFormField field) {
  }

  @Override
  public void transformOutline(IOutline outline) {
  }

  @Override
  public void transformPage(IPage<?> page) {
  }

  @Override
  public void transformPageDetailForm(IForm form) {
  }

  @Override
  public void transformPageDetailTable(ITable table) {
  }

  @Override
  public void notifyFormDisposed(IForm form) {
  }

  @Override
  public void notifyDesktopClosing() {
  }

  @Override
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
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
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return null;
  }

}
