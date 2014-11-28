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

import java.util.ArrayList;
import java.util.List;

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

  private final JsonClientSession m_jsonSession;

  public JsonEventProcessor(JsonClientSession jsonSession) {
    m_jsonSession = jsonSession;
  }

  public void processEvents(JsonRequest request, final JsonResponse response) {
    for (final JsonEvent event : request.getEvents()) {
      // TODO AWE: (jobs) prüfen ob das hier probleme macht: dadurch läuft processEvent immer im richtigen
      // context. JsonAdapter instanzen müssen somit nicht immer einen ClientSyncJob starten wenn sie z.B.
      // einen Scout-service aufrufen wollen. Es wurde bewusst für jedes processEvent ein eigener Job gestartet
      // und nicht für den ganzen Loop.
      new ClientSyncJob("processEvent", m_jsonSession.getJsonSession().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          processEvent(event, response);
        }
      }.schedule();
    }
    waitUntilJobsHaveFinished();
  }

  private void processEvent(JsonEvent event, JsonResponse response) {
    final String id = event.getId();
    final IJsonAdapter jsonAdapter = m_jsonSession.getJsonSession().getJsonAdapter(id);
    if (jsonAdapter == null) {
      throw new JsonException("No adapter found for id " + id);
    }
    try {
      LOG.info("Handling event. Type: " + event.getType() + ", Id: " + id);
      jsonAdapter.handleUiEvent(event, response);
      jsonAdapter.cleanUpEventFilters();
    }
    catch (Exception t) {
      LOG.error("Handling event. Type: " + event.getType() + ", Id: " + id, t);
      throw new JsonException(t);
    }
  }

  /**
   * Wait until all sync jobs have been finished or only waitFor jobs are left.
   */
  private void waitUntilJobsHaveFinished() {
    while (true) {
      List<ClientJob> jobList = getJobsForClientSession();
      if (jobList.isEmpty()) {
        LOG.info("Job list is empty. Finish request");
        return;
      }

      int numJobs = jobList.size();
      int numSync = 0;
      int numWaitFor = 0;
      for (ClientJob job : jobList) {
        if (job.isWaitFor()) {
          numWaitFor++;
        }
        else if (job.isSync()) {
          numSync++;
        }
      }
      LOG.trace("Jobs: " + numJobs + ", sync (running): " + numSync + ", waitFor (blocking): " + numWaitFor);
      if (numSync > 0) {
        LOG.trace("There are still running sync jobs - must wait until they have finished");
      }
      else if (numJobs == numWaitFor) {
        LOG.trace("Only 'waitFor' jobs left in the queue - it's allowed to finish the request");
        return;
      }

      // TODO AWE: (jobs) prüfen, ob das mit IJobChangeListener schöner geht (sleep/notify)
      LOG.trace("Going to sleep before checking the job queue again...");
      try {
        Thread.sleep(SLEEP_TIME);
      }
      catch (InterruptedException e) {
        // NOP
      }
    }
  }

  /**
   * Returns only jobs which belong to the current client session.
   */
  private List<ClientJob> getJobsForClientSession() {
    List<ClientJob> jobList = new ArrayList<>();
    for (Job job : Job.getJobManager().find(ClientJob.class)) {
      if (job instanceof ClientJob) {
        ClientJob clientJob = (ClientJob) job;
        if (clientJob.getClientSession() == m_jsonSession.getJsonSession().getClientSession()) {
          jobList.add(clientJob);
        }
      }
    }
    return jobList;
  }

}
