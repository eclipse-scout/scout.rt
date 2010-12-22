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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

/**
 * dont cache table page search form contents, releaseUnusedPages before every page reload and force gc to free
 * memory
 */
public class SmallMemoryPolicy extends AbstractMemoryPolicy {

  /**
   * clear table before loading new data, thus disabling "replaceRow" mechanism but saving memory
   */
  @Override
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

  @Override
  public void pageCreated(IPage p) throws ProcessingException {
    //nop
  }

  @Override
  public String toString() {
    return "Small";
  }
}
