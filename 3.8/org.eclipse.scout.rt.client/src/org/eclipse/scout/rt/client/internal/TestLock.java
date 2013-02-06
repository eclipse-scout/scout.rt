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
package org.eclipse.scout.rt.client.internal;

import java.util.concurrent.locks.ReentrantLock;

public class TestLock {

  public static void main(String[] args) throws Exception {
    new TestLock().run();
  }

  private ReentrantLock m_lock = new ReentrantLock();
  private int expectedId;

  public void run() throws Exception {
    T[] a = new T[100];
    for (int n = 0; n < 10; n++) {
      System.out.println("Go " + n);
      for (int i = 0; i < a.length; i++) {
        a[i] = new T(i, "" + Math.random());
      }
      m_lock.lock();
      for (int i = 0; i < a.length; i++) {
        a[i].start();
        Thread.sleep(0, 1);
      }
      expectedId = 0;
      m_lock.unlock();
      for (int i = 0; i < a.length; i++) {
        a[i].join();
      }
    }
  }

  private class T extends Thread {
    private int m_id;

    public T(int id, String name) {
      super(name);
      m_id = id;
    }

    @Override
    public void run() {
      m_lock.lock();
      try {
        // System.out.println(m_id+" got lock, expected "+expectedId);
        if (m_id == expectedId) {
          expectedId++;
        }
        else {
          System.out.println("ERROR " + m_id + " expected " + expectedId);
        }
      }
      finally {
        m_lock.unlock();
      }
    }
  }

}
