/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.transformation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public abstract class AbstractDeviceTransformer implements IDeviceTransformer {

  private IDesktop m_desktop;
  private final Set<IForm> m_dirtyGridData = new HashSet<>();
  private final DeviceTransformationConfig m_deviceTransformationConfig;

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
  public void transformPageTable(ITable table, IPage<?> page) {
  }

  @Override
  public void notifyPageDetailFormChanged(IForm form) {
  }

  @Override
  public void notifyPageDetailTableChanged(ITable table) {
  }

  @Override
  public void notifyDesktopClosing() {
  }

  @Override
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
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
