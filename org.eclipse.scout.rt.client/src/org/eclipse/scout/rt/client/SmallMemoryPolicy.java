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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

public class SmallMemoryPolicy implements IMemoryPolicy {

  public void afterOutlineSelectionChanged(final IDesktop desktop) {
  }

  /**
   * clear table before loading new data, thus disabling "replaceRow" mechanism but saving memory
   */
  public void beforeTablePageLoadData(IPageWithTable<?> page) {
    ClientJob.getCurrentSession().getDesktop().releaseUnusedPages();
    System.gc();
    for (Job j : Job.getJobManager().find(ClientJob.class)) {
      if (j instanceof ForceGCJob) {
        j.cancel();
      }
    }
    new ForceGCJob().schedule();
    if (page.getTable() != null) {
      page.getTable().discardAllRows();
    }
  }

  public void afterTablePageLoadData(IPageWithTable<?> page) {
  }

  @Override
  public String toString() {
    return "Small";
  }
}
