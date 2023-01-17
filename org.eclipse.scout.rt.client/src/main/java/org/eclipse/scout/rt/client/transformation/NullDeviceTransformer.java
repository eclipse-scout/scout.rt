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
  public void notifyFormAboutToShow(IForm form) {
    // NOP
  }

  @Override
  public void notifyFormDisposed(IForm form) {
    // NOP
  }

  @Override
  public void notifyFieldDisposed(IFormField formField) {
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
  public void excludeTransformation(IDeviceTransformation transformation) {

  }

  @Override
  public void removeTransformationExclusion(IDeviceTransformation transformation) {

  }

  @Override
  public boolean isTransformationExcluded(IDeviceTransformation transformation) {
    return false;
  }

  @Override
  public void excludeForm(IForm form) {
    // NOP
  }

  @Override
  public void excludeFormTransformation(IForm form, IDeviceTransformation transformation) {
    // NOP
  }

  @Override
  public boolean isFormExcluded(IForm form) {
    return false;
  }

  @Override
  public void excludeField(IFormField formField) {
    // NOP
  }

  @Override
  public void excludeFieldTransformation(IFormField formField, IDeviceTransformation transformation) {
    // NOP
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    return false;
  }

  @Override
  public void enableTransformation(IDeviceTransformation transformation) {
    // NOP
  }

  @Override
  public void disableTransformation(IDeviceTransformation transformation) {
    // NOP
  }

  @Override
  public boolean isTransformationEnabled(IDeviceTransformation transformation) {
    return false;
  }

  @Override
  public boolean isTransformationEnabled(IDeviceTransformation transformation, IFormField field) {
    return false;
  }

  @Override
  public boolean isTransformationEnabled(IDeviceTransformation transformation, IForm form) {
    return false;
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return null;
  }
}
