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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swt.extension.ILookAndFeelProperties;

public class LookAndFeelProperties implements ILookAndFeelProperties {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(LookAndFeelProperties.class);

  private String m_contributor;
  private int m_scope;
  private HashMap<String, String> m_values = new HashMap<String, String>();

  @Override
  public int getScope() {
    return m_scope;
  }

  public void setScope(int scope) {
    m_scope = scope;
  }

  @Override
  public int getPropertyInt(String name) {
    String value = m_values.get(name);
    if (value == null) {
      return 0;
    }

    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      LOG.error("Could not parse extension look and feel of contributor '" + m_contributor + "' property name='" + name + "' value='" + value + "'");
      return 0;
    }
  }

  public void setPropertyString(String name, String value) {
    m_values.put(name, value);
  }

  public void setContributor(String contributor) {
    m_contributor = contributor;
  }

  public String getContributor() {
    return m_contributor;
  }

  @Override
  public boolean getPropertyBool(String name) {
    String value = m_values.get(name);
    if (value == null) {
      return false;
    }

    return Boolean.parseBoolean(value);
  }

  public void setProperty(String name, String value) {
    m_values.put(name, value);
  }

  @Override
  public String getPropertyString(String name) {
    return m_values.get(name);
  }

}
