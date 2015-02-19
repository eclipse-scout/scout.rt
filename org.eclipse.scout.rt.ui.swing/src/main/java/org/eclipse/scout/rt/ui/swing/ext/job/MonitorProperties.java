/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.ext.job;

/**
 * Bean to hold properties of a {@link SwingProgressMonitor}
 * 
 * @author awe
 * @since 3.10.0-M3
 */
class MonitorProperties {

  static final MonitorProperties NULL_INSTANCE = new MonitorProperties(0, "", "");

  private final String m_taskName;

  private final String m_subTaskName;

  private final double m_worked;

  MonitorProperties(double worked, String taskName, String subTaskName) {
    m_worked = worked;
    m_taskName = taskName;
    m_subTaskName = subTaskName;
  }

  public String getTaskName() {
    return m_taskName;
  }

  public String getSubTaskName() {
    return m_subTaskName;
  }

  public double getWorked() {
    return m_worked;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_subTaskName == null) ? 0 : m_subTaskName.hashCode());
    result = prime * result + ((m_taskName == null) ? 0 : m_taskName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(m_worked);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MonitorProperties other = (MonitorProperties) obj;
    if (m_subTaskName == null) {
      if (other.m_subTaskName != null) {
        return false;
      }
    }
    else if (!m_subTaskName.equals(other.m_subTaskName)) {
      return false;
    }
    if (m_taskName == null) {
      if (other.m_taskName != null) {
        return false;
      }
    }
    else if (!m_taskName.equals(other.m_taskName)) {
      return false;
    }
    if (Double.doubleToLongBits(m_worked) != Double.doubleToLongBits(other.m_worked)) {
      return false;
    }
    return true;
  }

}
