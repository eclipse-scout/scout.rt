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

import java.util.HashMap;

import org.eclipse.scout.rt.ui.swt.extension.ILookAndFeelProperties;

public class LookAndFeelProperties implements ILookAndFeelProperties {

  private int m_scope;

  HashMap<String, Object> m_values = new HashMap<String, Object>();

  @Override
  public int getScope() {
    return m_scope;
  }

  public void setScope(int scope) {
    m_scope = scope;
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

  public void setProperty(String name, String value) {
    m_values.put(name, value);
    // if(name.equals(PROP_COLOR_FOREGROUND_DISABLED)){
    // setPropertyString(name, value);
    // }else{
    // setPropertyInt(name, Integer.parseInt(value));
    // }

  }

}
