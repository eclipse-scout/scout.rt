package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public abstract class AbstractDeviceTransformer implements IDeviceTransformer {
  private IDesktop m_desktop;
  private Set<IForm> m_dirtyGridData = new HashSet<>();
  private DeviceTransformationConfig m_deviceTransformationConfig;

  public AbstractDeviceTransformer() {
    m_deviceTransformationConfig = createDeviceTransformationConfig();
    initTransformationConfig();
  }

  protected DeviceTransformationConfig createDeviceTransformationConfig() {
    return new DeviceTransformationConfig();
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return m_deviceTransformationConfig;
  }

  protected void initTransformationConfig() {
  }

  @Override
  public void setDesktop(IDesktop desktop) {
    m_desktop = desktop;
  }

  @Override
  public abstract boolean isActive();

  public IDesktop getDesktop() {
    return m_desktop;
  }

  @Override
  public void dispose() {
  }

  @Override
  public void transformDesktop() {
  }

  @Override
  public void transformForm(IForm form) {
  }

  @Override
  public void notifyFormDisposed(IForm form) {
  }

  @Override
  public boolean isFormExcluded(IForm form) {
    return getDeviceTransformationConfig().isFormExcluded(form);
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    return getDeviceTransformationConfig().isFieldExcluded(formField);
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
  public void adaptDesktopActions(Collection<IAction> actions) {
  }

  @Override
  public void notifyDesktopClosing() {
  }

  @Override
  public void notifyTablePageLoaded(IPageWithTable<?> tablePage) {
  }

  @Override
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
  }

  @Override
  public boolean acceptFormAddingToDesktop(IForm form) {
    return true;
  }

  @Override
  public boolean isGridDataDirty(IForm form) {
    return m_dirtyGridData.contains(form);
  }

  @Override
  public void gridDataRebuilt(IForm form) {
    m_dirtyGridData.remove(form);
  }

  protected void markGridDataDirty(IForm form) {
    m_dirtyGridData.add(form);
  }

}
