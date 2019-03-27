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

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainDeviceTransformer implements IDeviceTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(MainDeviceTransformer.class);

  private List<IDeviceTransformer> m_transformers;

  public List<IDeviceTransformer> getTransformers() {
    if (m_transformers == null) {
      m_transformers = createTransformers();
      LOG.debug("Using following device transformers{}", m_transformers);
    }
    return m_transformers;
  }

  protected List<IDeviceTransformer> createTransformers() {
    return BEANS.all(IDeviceTransformer.class, transformer -> !(transformer instanceof MainDeviceTransformer) && transformer.isActive());
  }

  @Override
  public boolean isActive() {
    return !getTransformers().isEmpty();
  }

  @Override
  public void dispose() {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.dispose();
    }
  }

  @Override
  public void setDesktop(IDesktop desktop) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.setDesktop(desktop);
    }
  }

  @Override
  public void transformDesktop() {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformDesktop();
    }
  }

  @Override
  public void transformForm(IForm form) {
    if (isFormExcluded(form)) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformForm(form);
    }
  }

  @Override
  public void notifyFormDisposed(IForm form) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyFormDisposed(form);
    }
  }

  @Override
  public void notifyFieldDisposed(IFormField formField) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyFieldDisposed(formField);
    }
  }

  @Override
  public void excludeForm(IForm form) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.excludeForm(form);
    }
  }

  @Override
  public void excludeFormTransformation(IForm form, IDeviceTransformation transformation) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.excludeFormTransformation(form, transformation);
    }
  }

  @Override
  public boolean isFormExcluded(IForm form) {
    for (IDeviceTransformer transformer : getTransformers()) {
      if (transformer.isFormExcluded(form)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void excludeField(IFormField formField) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.excludeField(formField);
    }
  }

  @Override
  public void excludeFieldTransformation(IFormField formField, IDeviceTransformation transformation) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.excludeFieldTransformation(formField, transformation);
    }
  }

  @Override
  public boolean isFormFieldExcluded(IFormField formField) {
    for (IDeviceTransformer transformer : getTransformers()) {
      if (transformer.isFormFieldExcluded(formField)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void transformFormField(IFormField field) {
    if (isFormExcluded(field.getForm()) || isFormFieldExcluded(field)) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformFormField(field);
    }
  }

  @Override
  public void transformOutline(IOutline outline) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformOutline(outline);
    }
  }

  @Override
  public void transformPage(IPage<?> page) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformPage(page);
    }
  }

  @Override
  public void transformPageTable(ITable table, IPage<?> page) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.transformPageTable(table, page);
    }
  }

  @Override
  public void notifyPageDetailFormChanged(IForm form) {
    if (form == null || isFormExcluded(form)) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyPageDetailFormChanged(form);
    }
  }

  @Override
  public void notifyPageDetailTableChanged(ITable table) {
    if (table == null) {
      return;
    }

    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyPageDetailTableChanged(table);
    }
  }

  @Override
  public void notifyDesktopClosing() {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyDesktopClosing();
    }
  }

  @Override
  public void notifyPageSearchFormInit(IPageWithTable<ITable> page) {
    for (IDeviceTransformer transformer : getTransformers()) {
      transformer.notifyPageSearchFormInit(page);
    }
  }

  @Override
  public DeviceTransformationConfig getDeviceTransformationConfig() {
    return null;
  }
}
