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

import org.eclipse.core.runtime.IProgressMonitor;

/**
 *
 */
public class ForceGCJob extends ClientAsyncJob {
  boolean m_cancel = false;

  public ForceGCJob() {
    super("Release memory", ClientAsyncJob.getCurrentSession());
    setPriority(DECORATE);
  }

  @Override
  protected void runVoid(IProgressMonitor monitor) throws Throwable {
    int counter = 0;
    Thread.sleep(10000);
    while (counter < 7 && !m_cancel && (Runtime.getRuntime().freeMemory() > 30000000 || counter < 2)) {
      System.gc();
      counter++;
      Thread.sleep(10000);
    }
  }

  @Override
  protected void canceling() {
    m_cancel = true;
  }
}
