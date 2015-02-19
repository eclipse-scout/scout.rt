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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.ui.html.ClientJobUtility;

/**
 * Processes JSON events from the UI in a Scout client job and waits until all sync jobs have been finished.
 */
public class JsonEventProcessor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonEventProcessor.class);

  private final IJsonSession m_jsonSession;

  public JsonEventProcessor(IJsonSession jsonSession) {
    m_jsonSession = jsonSession;
  }

  public void processEvents(final JsonRequest request, final JsonResponse response) {
    ClientSyncJob job = new ClientSyncJob("processEvents", m_jsonSession.getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        for (final JsonEvent event : request.getEvents()) {
          processEvent(event, response);
        }
      }
    };
    job.schedule();
    ClientJobUtility.waitUntilJobsHaveFinished(m_jsonSession.getClientSession());
    try {
      job.throwOnError();
    }
    catch (ProcessingException e) {
      throw new JsonException(e); // TODO BSH Exception | Try to eliminate this pattern (5 others in html bundle)
    }
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
}
