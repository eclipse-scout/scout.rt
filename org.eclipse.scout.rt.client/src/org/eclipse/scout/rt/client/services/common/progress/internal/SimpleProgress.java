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
package org.eclipse.scout.rt.client.services.common.progress.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.services.common.progress.ISimpleProgress;

public class SimpleProgress extends ProgressMonitorWrapper implements ISimpleProgress {
  private Job m_job;
  private String m_name;
  private int m_worked = -1;

  protected SimpleProgress(Job job, IProgressMonitor monitor) {
    super(monitor);
    m_job = job;
  }

  @Override
  public void beginTask(String name, int totalWork) {
    super.beginTask(name, totalWork);
    m_name = name;
    m_worked = 0;
  }

  @Override
  public void setTaskName(String name) {
    super.setTaskName(name);
    m_name = name;
  }

  @Override
  public void worked(int work) {
    super.worked(work);
    m_worked += work;
  }

  public void setProgress(String s) {
    setTaskName(s);
  }

  public void setProgress(float f) {
    if (m_worked < 0) {
      beginTask(m_name, 100);
    }
    int i = (int) (100 * f);
    if (i >= 0 && i < m_worked) {
      beginTask(m_name, 100);
      worked(i);
    }
    int delta = i - m_worked;
    if (delta > 0) {
      worked(delta);
    }
  }

  public void setProgress(float f, String s) {
    setTaskName(s);
    //
    if (m_worked < 0) {
      beginTask(m_name, 100);
    }
    int i = (int) (100 * f);
    if (i >= 0 && i < m_worked) {
      beginTask(m_name, 100);
      worked(i);
    }
    int delta = i - m_worked;
    if (delta > 0) {
      worked(delta);
    }
  }

}
