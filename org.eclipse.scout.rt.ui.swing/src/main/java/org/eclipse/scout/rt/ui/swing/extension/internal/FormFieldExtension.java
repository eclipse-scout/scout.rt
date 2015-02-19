/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.extension.internal;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldExtension;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;

public class FormFieldExtension implements IFormFieldExtension {
  private String m_name;
  private boolean m_active;
  private int m_scope = SCOPE_DEFAULT;
  private Class<? extends IFormField> m_modelClass;
  private Class<? extends ISwingScoutFormField> m_uiClass;
  private Class<? extends IFormFieldFactory> m_factoryClass;

  public FormFieldExtension(String name) {
    m_name = name;
  }

  @Override
  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  @Override
  public boolean isActive() {
    return m_active;
  }

  public void setActive(boolean active) {
    m_active = active;
  }

  @Override
  public int getScope() {
    return m_scope;
  }

  public void setScope(int scope) {
    m_scope = scope;
  }

  @Override
  public Class<? extends IFormField> getModelClass() {
    return m_modelClass;
  }

  public void setModelClass(Class<? extends IFormField> modelClass) {
    m_modelClass = modelClass;
  }

  @Override
  public Class<? extends ISwingScoutFormField> getUiClass() {
    return m_uiClass;
  }

  public void setUiClass(Class<? extends ISwingScoutFormField> uiClass) {
    m_uiClass = uiClass;
  }

  @Override
  public Class<? extends IFormFieldFactory> getFactoryClass() {
    return m_factoryClass;
  }

  public void setFactoryClass(Class<? extends IFormFieldFactory> factoryClass) {
    m_factoryClass = factoryClass;
  }
}
