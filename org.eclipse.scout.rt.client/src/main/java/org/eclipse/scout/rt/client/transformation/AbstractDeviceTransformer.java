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

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public abstract class AbstractDeviceTransformer implements IDeviceTransformer {

  private IDesktop m_desktop;
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
  public void excludeTransformation(IDeviceTransformation transformation) {
    getDeviceTransformationConfig().excludeTransformation(transformation);
  }

  @Override
  public void removeTransformationExclusion(IDeviceTransformation transformation) {
    getDeviceTransformationConfig().removeTransformationExclusion(transformation);
  }

  @Override
  public boolean isTransformationExcluded(IDeviceTransformation transformation) {
    return getDeviceTransformationConfig().isTransformationExcluded(transformation);
  }

  @Override
  public void excludeForm(IForm form) {
    getDeviceTransformationConfig().excludeForm(form);
  }

  @Override
  public void excludeFormTransformation(IForm form, IDeviceTransformation transformation) {
    getDeviceTransformationConfig().excludeFormTransformation(form, transformation);
  }

  @Override
  public boolean isFormExcluded(IForm form) {
    return getDeviceTransformationConfig().isFormExcluded(form);
  }

  @Override
  public void excludeField(IFormField formField) {
    getDeviceTransformationConfig().excludeField(formField);
  }

  @Override
  public void excludeFieldTransformation(IFormField formField, IDeviceTransformation transformation) {
    getDeviceTransformationConfig().excludeFieldTransformation(formField, transformation);
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    return getDeviceTransformationConfig().isFieldExcluded(formField);
  }

  @Override
  public void enableTransformation(IDeviceTransformation transformation) {
    getDeviceTransformationConfig().enableTransformation(transformation);
  }

  @Override
  public void disableTransformation(IDeviceTransformation transformation) {
    getDeviceTransformationConfig().disableTransformation(transformation);
  }

  @Override
  public boolean isTransformationEnabled(IDeviceTransformation transformation) {
    return getDeviceTransformationConfig().isTransformationEnabled(transformation);
  }

  @Override
  public boolean isTransformationEnabled(IDeviceTransformation transformation, IForm form) {
    return getDeviceTransformationConfig().isTransformationEnabled(transformation, form);
  }

  @Override
  public boolean isTransformationEnabled(IDeviceTransformation transformation, IFormField field) {
    return getDeviceTransformationConfig().isTransformationEnabled(transformation, field);
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
  public void notifyFormAboutToShow(IForm form) {
  }

  @Override
  public void notifyFormDisposed(IForm form) {
    getDeviceTransformationConfig().removeFormExclusion(form);
  }

  @Override
  public void notifyFieldDisposed(IFormField formField) {
    getDeviceTransformationConfig().removeFieldExclusion(formField);
  }

  @Override
  public void notifyDesktopClosing() {
  }

  @Override
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
  }

  protected void rebuildParentGrid(IFormField field) {
    ICompositeField parentField = field.getParentField();
    if (parentField != null) {
      parentField.rebuildFieldGrid();
    }
  }
}
