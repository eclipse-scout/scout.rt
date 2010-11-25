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
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

public class LargeMemoryPolicy implements IMemoryPolicy {

  public void afterOutlineSelectionChanged(final IDesktop desktop) {
    long memTotal = Runtime.getRuntime().totalMemory();
    long memUsed = (memTotal - Runtime.getRuntime().freeMemory());
    long memMax = Runtime.getRuntime().maxMemory();
    if (memUsed > memMax * 80L / 100L) {
      new ClientSyncJob("Check memory", ClientSyncJob.getCurrentSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          desktop.releaseUnusedPages();
          System.gc();
        }
      }.schedule();
    }
  }

  public void beforeTablePageLoadData(IPageWithTable<?> page) {
  }

  public void afterTablePageLoadData(IPageWithTable<?> page) {
  }

  @Override
  public String toString() {
    return "Large";
  }
}
