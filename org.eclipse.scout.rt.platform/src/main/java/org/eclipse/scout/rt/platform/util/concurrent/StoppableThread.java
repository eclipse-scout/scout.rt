/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.concurrent;

//TODO [5.2] jgu: delete?
public class StoppableThread extends Thread {
  private boolean m_stopSignal;

  public StoppableThread() {
    super("StoppableThread");
  }

  public StoppableThread(String name) {
    super(name);
  }

  public StoppableThread(Runnable r) {
    super(r, "StoppableThread");
  }

  public StoppableThread(Runnable r, String s) {
    super(r, s);
  }

  public StoppableThread(ThreadGroup g, Runnable r, String s) {
    super(g, r, s);
  }

  @Override
  public void start() {
    m_stopSignal = false;
    super.start();
  }

  public void setStopSignal() {
    m_stopSignal = true;// cannot be set back again once it is set
    interrupt();
  }

  public boolean isStopSignal() {
    return m_stopSignal;
  }
}
