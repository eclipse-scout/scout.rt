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
package org.eclipse.scout.rt.ui.swing.basic.table;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.context.RunMonitor;

/**
 *
 */
public abstract class AbstractKeyboardNavigationSupport {
  private final long m_delay;
  private long m_timeoutTimestamp;
  private String m_filterText = "";
  private final Object navigationLock = new Object();
  private IFuture<Void> m_navigationJob;
  private IClientSession m_session;

  public AbstractKeyboardNavigationSupport(IClientSession session) {
    this(1000L, session);
  }

  public AbstractKeyboardNavigationSupport(long delay, IClientSession session) {
    m_delay = delay;
    m_session = session;
    m_navigationJob = null;
  }

  public void addChar(char c) {
    synchronized (navigationLock) {
      if (Character.isWhitespace(c) || Character.isLetterOrDigit(c)) {
        if (System.currentTimeMillis() > m_timeoutTimestamp) {
          m_filterText = "";
        }
        String newText = "" + Character.toLowerCase(c);
        m_filterText += newText;
        if (m_navigationJob != null) {
          m_navigationJob.cancel(true);
        }
        m_navigationJob = ClientJobs.schedule(new P_NavigationJob(), 250, TimeUnit.MILLISECONDS, ClientJobs.newInput(ClientRunContexts.copyCurrent().session(m_session)));
        m_timeoutTimestamp = System.currentTimeMillis() + m_delay;
      }
    }
  }

  abstract void handleSearchPattern(String regex);

  private class P_NavigationJob implements IRunnable {

    @Override
    public void run() throws Exception {
      String pattern;
      synchronized (navigationLock) {
        if (RunMonitor.CURRENT.get().isCancelled() || StringUtility.isNullOrEmpty(m_filterText)) {
          return;
        }
        pattern = StringUtility.toRegExPattern(m_filterText.toLowerCase());
        pattern = pattern + ".*";
      }
      //this call must be outside lock!
      handleSearchPattern(pattern);
      return;
    }
  } // end class P_NavigationJob
}
