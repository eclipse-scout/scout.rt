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
package org.eclipse.scout.rt.ui.rap.testing;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.ui.rap.Activator;

/**
 * To load test a web application, a typical approach is to record the requests and play them back multiple times. This
 * works fine if there are only sync requests. In scout, most of the gui interactions create {@link ClientJob}s and
 * therefore run in the background. The advantage is a responsive gui, the disadvantage is that recording gets more
 * complicated. If the background jobs are very fast at recording time, no ui callback requests will be generated. This
 * means, on playback time, the successive request may be processed while a background job of the previous request is
 * still running. If the successive request contains widget ids of widgets which don't already exist yet, the events
 * cannot be processed and therefore the request and probably every following request can't do what they are supposed
 * to.
 * <p>
 * One possibility to minimize the risk of such wrong tests, is to wait for the background jobs before returning to the
 * client. This can be enabled with the system property {@value #ENABLE_SYNC_REQUESTS}. <br>
 * Another way would be to make sure, every ordinary request is followed by a ui callback request and an associated ui
 * request.
 * <p>
 * Hint: When enabling sync requests you probably want to enable {@link WidgetUtil#ENABLE_UI_TESTS} too, to make sure
 * the widget ids are independent of the order in which the widgets are created.
 * <p>
 * Important: This utility is experimental and may be removed without notice
 */
public class TestingUtility {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(TestingUtility.class);
  private static boolean m_syncRequestsEnabled = false;

  public static final String ENABLE_SYNC_REQUESTS = "org.eclipse.scout.rt.ui.rap.enableSyncRequests";

  static {
    String property = Activator.getDefault().getBundle().getBundleContext().getProperty(ENABLE_SYNC_REQUESTS);
    m_syncRequestsEnabled = Boolean.valueOf(property).booleanValue();

    if (m_syncRequestsEnabled) {
      LOG.info("Sync requests enabled.");
    }
  }

  public static boolean isSyncRequestsEnabled() {
    return m_syncRequestsEnabled;
  }

  /**
   * Waits until every client job has been completed.
   * <p>
   * <b> Important: </b>Use this with care! Make sure there are no continuous jobs running, like
   * ClientNotificationPollingJob. Otherwise it will wait forever.
   */
  public static void waitForClientJobs() {
    while (hasRunningClientJobs()) {
      try {
        Thread.sleep(25);
      }
      catch (InterruptedException e) {
        LOG.warn("Thread interrupted.");
      }
    }
  }

  private static boolean hasRunningClientJobs() {
    Job[] jobs = ClientJob.getJobManager().find(ClientJob.class);
    for (Job job : jobs) {
      ClientJob clientJob = (ClientJob) job;
      if (!clientJob.isWaitFor()) {
        return true;
      }
    }

    return false;
  }
}
