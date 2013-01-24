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
package org.eclipse.scout.rt.ui.swt.basic.table;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 *
 */
public abstract class AbstractKeyboardNavigationSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractKeyboardNavigationSupport.class);
  private final long m_delay;
  private long m_timeoutTimestamp;
  private String m_filterText = "";
  private Object navigationLock = new Object();
  private P_NavigationJob m_navigationJob;

  public AbstractKeyboardNavigationSupport() {
    this(1000L);
  }

  public AbstractKeyboardNavigationSupport(long delay) {
    m_delay = delay;
    m_navigationJob = new P_NavigationJob();
  }

  public void addChar(char c) {
    synchronized (navigationLock) {
      if (Character.isLetter(c)) {
        if (System.currentTimeMillis() > m_timeoutTimestamp) {
          m_filterText = "";
        }
        String newText = "" + Character.toLowerCase(c);
        m_filterText += newText;
        if (m_navigationJob != null) {
          m_navigationJob.cancel();
        }
        else {
          m_navigationJob = new P_NavigationJob();
        }
        m_navigationJob.schedule(300L);
        m_timeoutTimestamp = System.currentTimeMillis() + m_delay;
      }
    }
  }

  abstract void handleSearchPattern(String regex);

  private class P_NavigationJob extends Job {

    public P_NavigationJob() {
      super("");
      setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      String pattern;
      synchronized (navigationLock) {
        if (monitor.isCanceled() || StringUtility.isNullOrEmpty(m_filterText)) {
          return Status.CANCEL_STATUS;
        }
        pattern = StringUtility.toRegExPattern(m_filterText.toLowerCase());
        pattern = pattern + ".*";
      }
      //this call must be outside lock!
      handleSearchPattern(pattern);
      return Status.OK_STATUS;
    }
  } // end class P_NavigationJob
}
