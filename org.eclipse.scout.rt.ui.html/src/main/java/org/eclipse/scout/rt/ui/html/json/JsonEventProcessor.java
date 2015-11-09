/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes JSON events from the UI in a Scout model job and waits for all model jobs of that session to complete.
 */
public class JsonEventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(JsonEventProcessor.class);

  private final IUiSession m_uiSession;

  public JsonEventProcessor(IUiSession uiSession) {
    m_uiSession = uiSession;
  }

  public void processEvents(final JsonRequest request, final JsonResponse response) {
    Assertions.assertTrue(ModelJobs.isModelThread(), "Event processing must be called from the model thread  [currentThread=%s, request=%s, response=%s]",
        Thread.currentThread().getName(), request, response);
    for (final JsonEvent event : request.getEvents()) {
      processEvent(event, response);
    }
  }

  protected void processEvent(JsonEvent event, JsonResponse response) {
    final IJsonAdapter jsonAdapter = m_uiSession.getJsonAdapter(event.getTarget());
    if (jsonAdapter == null) {
      // FIXME AWE: (json-layer) schauen ob wir eine warning ans UI zurückgeben sollen
      LOG.warn("No adapter found for ID {}", event.getTarget());
      return;
    }
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Handling event '{}' for adapter with ID {}", event.getType(), event.getTarget());
      }
      jsonAdapter.handleUiEvent(event);
      jsonAdapter.cleanUpEventFilters();
    }
    catch (RuntimeException e) {
      LOG.error("Error while handling event '{}' for adapter {}", event.getType(), jsonAdapter, e);
      throw e;
    }
  }
}
