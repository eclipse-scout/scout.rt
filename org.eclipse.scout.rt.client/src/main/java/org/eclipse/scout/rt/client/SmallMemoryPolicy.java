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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.job.ClientSessionFutureFilter;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.JobFutureFilter;

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
    //make sure inactive outlines have no selection that "keeps" the pages
    IClientSession session = ClientSessionProvider.currentSession();
    IDesktop desktop = session.getDesktop();
    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o != desktop.getOutline()) {
        o.selectNode(null);
      }
    }
    desktop.releaseUnusedPages();
    System.gc();
    Jobs.getJobManager().cancel(new AndFilter<>(new JobFutureFilter(getClass().getName()), new ClientSessionFutureFilter(session)), true);
    ClientJobs.schedule(new ForceGCJob(), ClientJobInput.defaults().setSession(session).setName("release memory").setId(getClass().getName()));
    if (page.getTable() != null) {
      page.getTable().discardAllRows();
    }
  }

  @Override
  public void pageSearchFormStarted(IPageWithTable<?> p) throws ProcessingException {
    //nop
  }

  @Override
  public void pageCreated(IPage<?> p) throws ProcessingException {
    //nop
  }

  @Override
  public String toString() {
    return "Small";
  }
}
