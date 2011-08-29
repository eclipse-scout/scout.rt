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
package org.eclipse.scout.rt.ui.swing.concurrency;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

class ConcurrencyQueue {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ConcurrencyQueue.class);
  private static long seqNoProvider;

  private long m_seqNo;
  private Object m_queueChangeLock;
  private boolean m_closed;
  private String m_name;
  private String m_infoCached;
  private Object m_listLock = new Object();
  private LinkedList<Runnable> m_list = new LinkedList<Runnable>();
  private LinkedList<Runnable> m_dispatchingList = new LinkedList<Runnable>();

  public ConcurrencyQueue(String name, Object queueChangeLock) {
    m_seqNo = seqNoProvider++;
    if (queueChangeLock == null) {
      queueChangeLock = new Object();
    }
    m_queueChangeLock = queueChangeLock;
    m_name = name;
    updateInternalInfo();
  }

  public boolean isEmpty() {
    synchronized (m_listLock) {
      return m_list.size() + m_dispatchingList.size() == 0;
    }
  }

  public void close() {
    m_closed = true;
    synchronized (m_listLock) {
      m_listLock.notifyAll();
    }
    synchronized (m_queueChangeLock) {
      m_queueChangeLock.notifyAll();
    }
  }

  public boolean isClosed() {
    return m_closed;
  }

  public void add(Runnable r) {
    if (r != null) {
      synchronized (m_listLock) {
        if (LOG.isInfoEnabled()) {
          LOG.info(m_infoCached + " add: " + r);
        }
        m_list.add(r);
        updateInternalInfo();
        m_listLock.notifyAll();
      }
      synchronized (m_queueChangeLock) {
        m_queueChangeLock.notifyAll();
      }
    }
  }

  /**
   * @return true if element could be removed
   */
  public boolean remove(Runnable r) {
    if (r != null) {
      boolean ok;
      synchronized (m_listLock) {
        ok = m_list.remove(r);
        updateInternalInfo();
        m_listLock.notifyAll();
      }
      synchronized (m_queueChangeLock) {
        m_queueChangeLock.notifyAll();
      }
      return ok;
    }
    return false;
  }

  public boolean isPending(Runnable r) {
    if (r != null) {
      synchronized (m_listLock) {
        return m_dispatchingList.contains(r) || m_list.contains(r);
      }
    }
    return false;
  }

  /**
   * does not block
   */
  public boolean hasNext() {
    synchronized (m_listLock) {
      return !m_list.isEmpty();
    }
  }

  public List<Runnable> removeAllJobs() {
    synchronized (m_listLock) {
      ArrayList<Runnable> a = new ArrayList<Runnable>(m_list);
      m_list.clear();
      return a;
    }
  }

  /**
   * block at most waitTime millis
   */
  public boolean hasNext(long waitTime) {
    long deadline = System.currentTimeMillis() + waitTime;
    synchronized (m_listLock) {
      while (m_list.isEmpty()) {
        long dt = deadline - System.currentTimeMillis();
        if (dt > 0) {
          try {
            m_listLock.wait(dt);
          }
          catch (InterruptedException e) {
          }
        }
        else {
          break;
        }
      }
      return !m_list.isEmpty();
    }
  }

  public void dispatchNext() {
    Runnable r = null;
    synchronized (m_listLock) {
      if (!m_list.isEmpty()) {
        r = m_list.removeFirst();
        m_dispatchingList.add(r);
      }
    }
    if (r != null) {
      try {
        long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
          LOG.info(m_infoCached + " dispatchStart: " + r);
        }
        r.run();
        if (LOG.isInfoEnabled()) {
          LOG.info(m_infoCached + " dispatchEnd " + (System.currentTimeMillis() - startTime) + " ms:   " + r);
        }
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
      synchronized (m_listLock) {
        m_dispatchingList.remove(r);
        updateInternalInfo();
        m_listLock.notifyAll();
      }
      synchronized (m_queueChangeLock) {
        m_queueChangeLock.notifyAll();
      }
    }
  }

  private void updateInternalInfo() {
    int a = m_list.size();
    int b = m_dispatchingList.size();
    m_infoCached = m_name + "#" + m_seqNo + "[" + a + (b > 0 ? "+" + b : "") + " jobs]";
  }

  @Override
  public String toString() {
    return m_infoCached;
  }
}
