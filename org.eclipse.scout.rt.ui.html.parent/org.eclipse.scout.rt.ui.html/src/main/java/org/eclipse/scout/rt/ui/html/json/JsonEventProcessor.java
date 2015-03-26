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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.ModelJobInput;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.ui.html.JobUtility;

/**
 * Processes JSON events from the UI in a Scout model job and waits for all model jobs of that session to complete.
 */
public class JsonEventProcessor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonEventProcessor.class);

  private final IJsonSession m_jsonSession;

  public JsonEventProcessor(IJsonSession jsonSession) {
    m_jsonSession = jsonSession;
  }

  public void processEvents(final JsonRequest request, final JsonResponse response) {
    Assertions.assertFalse(ModelJobs.isModelThread(), "Event processing must be called from a thread other than the model thread [thread=%s, request=%s, response=%s]", Thread.currentThread().getName(), request, response);

    // No need to schedule job and wait if there are no requests (e.g. polling for background jobs)
    if (request.getEvents().isEmpty()) {
      return;
    }

    // Process requested events.
    IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        for (final JsonEvent event : request.getEvents()) {
          processEvent(event, response);
        }
      }
    }, ModelJobInput.fillCurrent().session(m_jsonSession.getClientSession()).name("event-processing"));

    // Wait for all events to be processed. It is not sufficient to only wait for the Future to complete, because other jobs might be started as well.
    JobUtility.awaitModelJobs(future);
  }

  protected void processEvent(JsonEvent event, JsonResponse response) {
    final IJsonAdapter jsonAdapter = m_jsonSession.getJsonAdapter(event.getTarget());
    if (jsonAdapter == null) {
      throw new JsonException("No adapter found for ID " + event.getTarget());
    }
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Handling event '{}' for adapter with ID {}", event.getType(), event.getTarget());
      }
      jsonAdapter.handleUiEvent(event);
      jsonAdapter.cleanUpEventFilters();
    }
    catch (Exception e) {
      LOG.error("Error while handling event '" + event.getType() + "' for adapter with ID " + event.getTarget(), e);
      throw new JsonException(e);
    }
  }
}
