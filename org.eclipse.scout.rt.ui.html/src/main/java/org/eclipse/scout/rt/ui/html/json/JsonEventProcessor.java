/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;
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
    Assertions.assertTrue(ModelJobs.isModelThread(), "Event processing must be called from the model thread  [currentThread={}, request={}, response={}]",
        Thread.currentThread().getName(), request, response);
    for (final JsonEvent event : request.getEvents()) {
      BEANS.get(ITracingHelper.class).wrapInSpan(BEANS.get(ITracingHelper.class).createTracer(JsonEventProcessor.class), "processJsonEvent", span -> {
        BEANS.get(ITracingHelper.class).appendAttributes(span, event);
        processEvent(event, response);
      });
    }
  }

  protected void processEvent(JsonEvent event, JsonResponse response) {
    final IJsonAdapter<?> jsonAdapter = m_uiSession.getJsonAdapter(event.getTarget());
    if (jsonAdapter == null) {
      LOG.info("No adapter found for event. {}", event.toSafeString());
      return;
    }
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Handling event '{}' for adapter with ID {}", event.getType(), event.getTarget());
      }

      jsonAdapter.handleUiEvent(event);
      jsonAdapter.cleanUpEventFilters();
    }
    catch (PlatformException e) { // NOSONAR
      throw e
          .withContextInfo("ui.event", event.getType())
          .withContextInfo("ui.adapter", jsonAdapter);
    }
  }
}
