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
package org.eclipse.scout.rt.ui.swt.extension.internal;

import org.eclipse.scout.rt.ui.swt.extension.IFormFieldExtension;

public class FormFieldExtension implements IFormFieldExtension {

  private String m_name;
  private boolean m_active;
  private int m_scope = SCOPE_DEFAULT;
  private String /* fully qualified name of a Class<? extends IFormField> */m_modelClassName;
  private String /*
                  * fully qualified name of a Class<? extends
                  * ISwtScoutFormField>
                  */m_uiClassName;
  private String /*
                  * fully qualified name of a Class<? extends
                  * IFormFieldFactory>
                  */m_factoryClassName;
  private String m_contibuterBundleId;

  public FormFieldExtension(String name) {
    m_name = name;
  }

  @Override
  public String getContibuterBundleId() {
    return m_contibuterBundleId;
  }

  public void setContibuterBundleId(String contibuterBundleId) {
    m_contibuterBundleId = contibuterBundleId;
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
  public String getModelClassName() {
    return m_modelClassName;
  }

  public void setModelClassName(String modelClassName) {
    m_modelClassName = modelClassName;
  }

  @Override
  public String getUiClassName() {
    return m_uiClassName;
  }

  public void setUiClassName(String uiClassName) {
    m_uiClassName = uiClassName;
  }

  @Override
  public String getFactoryClassName() {
    return m_factoryClassName;
  }

  public void setFactoryClassName(String factoryClassName) {
    m_factoryClassName = factoryClassName;
  }

}
