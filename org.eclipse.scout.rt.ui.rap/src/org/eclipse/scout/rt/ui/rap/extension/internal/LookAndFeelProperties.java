/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.extension.internal;

import java.util.HashMap;

import org.eclipse.scout.rt.ui.rap.extension.ILookAndFeelProperties;

public class LookAndFeelProperties implements ILookAndFeelProperties {

  private int m_scope;
  private String m_deviceTypeIdentifier;
  private HashMap<String, Object> m_values = new HashMap<String, Object>();

  @Override
  public int getScope() {
    return m_scope;
  }

  public void setScope(int scope) {
    m_scope = scope;
  }

  @Override
  public String getDeviceTypeIdentifier() {
    return m_deviceTypeIdentifier;
  }

  public void setDeviceTypeIdentifier(String deviceTypeIdentifier) {
    m_deviceTypeIdentifier = deviceTypeIdentifier;
  }

  public void setPropertyInt(String name, int value) {
    m_values.put(name, value);
  }

  @Override
  public int getPropertyInt(String name) {
    Object object = m_values.get(name);
    if (object != null) {
      return ((Integer) object).intValue();
    }
    return 0;
  }

  public void setPropertyString(String name, String value) {
    m_values.put(name, value);
  }

  @Override
  public String getPropertyString(String name) {
    Object object = m_values.get(name);
    if (object != null) {
      return (String) object;
    }
    return null;
  }

  public void setPropertyBool(String name, boolean value) {
    m_values.put(name, value);
  }

  @Override
  public boolean getPropertyBool(String name) {
    Object object = m_values.get(name);
    if (object != null) {
      return ((Boolean) object).booleanValue();
    }
    return false;
  }

  @Override
  public boolean existsProperty(String name) {
    return m_values.containsKey(name);
  }

  public void setProperty(String name, String value) {
    m_values.put(name, value);
  }

}
