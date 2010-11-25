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
package org.eclipse.scout.rt.client;

import java.util.ArrayList;

import org.eclipse.core.runtime.jobs.Job;

/**
 * Use this object to put the current job into waiting mode until the blocking
 * condition falls. see {@link ClientSyncJob}
 */
public class BlockingCondition {
  private boolean m_blocking = true;
  private ArrayList<ClientJob> m_blockingJobs;

  public BlockingCondition(boolean b) {
    m_blocking = b;
    m_blockingJobs = new ArrayList<ClientJob>();
  }

  public boolean isBlocking() {
    return m_blocking;
  }

  public void setBlocking(boolean b) {
    if (m_blocking != b) {
      if (b) {
        synchronized (this) {
          m_blocking = true;
        }
      }
      else {
        release();
      }
    }
  }

  public void waitFor() throws InterruptedException {
    ClientJob c = null;
    synchronized (this) {
      if (m_blocking) {
        Job j = ClientJob.getJobManager().currentJob();
        if (j instanceof ClientJob) {
          c = (ClientJob) j;
          m_blockingJobs.add(c);
        }
        else {
          while (m_blocking) {
            wait();
          }
          return;
        }
      }
    }
    // outside sync
    if (c != null) {
      c.waitFor();
    }
  }

  public void release() {
    synchronized (this) {
      if (m_blocking) {
        m_blocking = false;
        notifyAll();
        for (ClientJob c : m_blockingJobs) {
          try {
            c.releaseWaitFor();
          }
          catch (Throwable t) {
            t.printStackTrace();
          }
        }
        m_blockingJobs.clear();
      }
    }
  }
}
