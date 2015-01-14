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
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;

/**
 * Processes JSON events from the UI in a Scout client job and waits until all sync jobs have been finished.
 */
public class JsonEventProcessor {

  private static final int SLEEP_TIME = 25;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonEventProcessor.class);

  private final IJsonSession m_jsonSession;

  public JsonEventProcessor(IJsonSession jsonSession) {
    m_jsonSession = jsonSession;
  }

  public void processEvents(JsonRequest request, final JsonResponse response) {
    for (final JsonEvent event : request.getEvents()) {
      // TODO AWE: (jobs) prüfen ob das hier probleme macht: dadurch läuft processEvent immer im richtigen
      // context. JsonAdapter instanzen müssen somit nicht immer einen ClientSyncJob starten wenn sie z.B.
      // einen Scout-service aufrufen wollen. Es wurde bewusst für jedes processEvent ein eigener Job gestartet
      // und nicht für den ganzen Loop.
      new ClientSyncJob("processEvent", m_jsonSession.getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          processEvent(event, response);
        }
      }.schedule();
    }
    waitUntilJobsHaveFinished();
  }

  protected void processEvent(JsonEvent event, JsonResponse response) {
    final IJsonAdapter jsonAdapter = m_jsonSession.getJsonAdapter(event.getTarget());
    if (jsonAdapter == null) {
      throw new JsonException("No adapter found for ID " + event.getTarget());
    }
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Handling event '" + event.getType() + "' for adapter with ID " + event.getTarget());
      }
      jsonAdapter.handleUiEvent(event, response);
      jsonAdapter.cleanUpEventFilters();
    }
    catch (Exception t) {
      LOG.error("Error while handling event '" + event.getType() + "' for adapter with ID " + event.getTarget());
      throw new JsonException(t);
    }
  }

  /**
   * Wait until all sync jobs have been finished or only waitFor sync jobs are left.
   */
  protected void waitUntilJobsHaveFinished() {
    while (true) {
      int numWaitFor = 0;
      int numSync = 0;
      for (Job job : Job.getJobManager().find(ClientJob.class)) {
        ClientJob clientJob = (ClientJob) job;
        if (isSyncJobFromCurrentSession(clientJob)) {
          numSync++;
          if (clientJob.isWaitFor()) {
            numWaitFor++;
          }
        }
      }
      if (numSync == numWaitFor) { // also covers numSync == 0
        LOG.trace("Job list is empty or only 'waitFor' sync jobs left in the queue - finish the request");
        return;
      }
      // TODO AWE: (jobs) prüfen, ob das mit IJobChangeListener schöner geht (sleep/notify)
      LOG.trace("There are still running sync jobs - must wait until they have finished. Jobs (sync)=" + numSync + " jobs (waitFor)=" + numWaitFor);
      LOG.trace("Going to sleep before checking the job queue again...");
      try {
        Thread.sleep(SLEEP_TIME);
      }
      catch (InterruptedException e) {
        // NOP
      }
    }
  }

  private boolean isSyncJobFromCurrentSession(ClientJob job) {
    return job.isSync() &&
        job.getClientSession() == m_jsonSession.getClientSession();
  }

}
