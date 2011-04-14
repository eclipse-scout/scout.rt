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
package org.eclipse.scout.rt.ui.swing.window.desktop.status;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;

public class SwingProgressMonitor extends AbstractPropertyObserver implements IProgressMonitorWithBlocking {
  /**
   * {@link String}
   */
  public static final String PROP_TASK_NAME = "taskName";
  /**
   * {@link String}
   */
  public static final String PROP_SUB_TASK_NAME = "subTaskName";
  /**
   * {@link Double} 0..1
   */
  public static final String PROP_WORKED = "worked";
  /**
   * {@link Boolean}
   */
  public static final String PROP_CANCELLED = "cancelled";

  private double m_internalTotalWork;
  private double m_internalWorked;

  @Override
  public void beginTask(String name, int totalWork) {
    propertySupport.setPropertyString(PROP_SUB_TASK_NAME, null);
    propertySupport.setPropertyString(PROP_TASK_NAME, name);
    m_internalTotalWork = totalWork;
    m_internalWorked = 0;
    setWorked(0);
  }

  @Override
  public void done() {
    m_internalWorked = m_internalTotalWork;
    setWorked(1);
  }

  public void setWorked(double worked) {
    propertySupport.setPropertyDouble(PROP_WORKED, worked);
  }

  public double getWorked() {
    return propertySupport.getPropertyDouble(PROP_WORKED);
  }

  @Override
  public void internalWorked(double work) {
    m_internalWorked += work;
    if (m_internalTotalWork > 0) {
      setWorked(m_internalWorked / m_internalTotalWork);
    }
    else {
      setWorked(0);
    }
  }

  @Override
  public boolean isCanceled() {
    return propertySupport.getPropertyBool(PROP_CANCELLED);
  }

  @Override
  public void setCanceled(boolean cancelled) {
    propertySupport.setPropertyBool(PROP_CANCELLED, cancelled);
  }

  @Override
  public void setTaskName(String name) {
    propertySupport.setPropertyString(PROP_TASK_NAME, name);
  }

  public String getTaskName() {
    return propertySupport.getPropertyString(PROP_TASK_NAME);
  }

  @Override
  public void subTask(String name) {
    propertySupport.setPropertyString(PROP_SUB_TASK_NAME, name);
  }

  public String getSubTaskName() {
    return propertySupport.getPropertyString(PROP_SUB_TASK_NAME);
  }

  @Override
  public void worked(int work) {
    internalWorked(work);
  }

  @Override
  public void clearBlocked() {
  }

  @Override
  public void setBlocked(IStatus reason) {
  }
}
