/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.session;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNullOrEmpty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;

/**
 * Helper to provide metrics for Scout's sessions:
 * <ul>
 * <li>number of active sessions</li>
 * <li>number of created sessions</li>
 * </ul>
 * <p>
 * These metrics are differentiated by session type (e.g. client, ui, ...)
 * </p>
 */
@ApplicationScoped
public class SessionMetricsHelper {

  private static final Logger LOG = LoggerFactory.getLogger(SessionMetricsHelper.class);

  protected final Map<String, SessionMetrics> m_metrics = new ConcurrentHashMap<>();

  public void sessionCreated(String type) {
    assertNotNullOrEmpty(type);
    m_metrics.computeIfAbsent(type, this::initMetrics).sessionCreated();
  }

  public void sessionDestroyed(String type) {
    assertNotNullOrEmpty(type);
    m_metrics.computeIfAbsent(type, this::initMetrics).sessionDestroyed();
  }

  protected SessionMetrics initMetrics(String type) {
    LOG.info("Init session metrics of type '{}'", type);
    Meter meter = GlobalOpenTelemetry.get().getMeter(getClass().getName());
    return new SessionMetrics(meter, type);
  }

  protected static class SessionMetrics {

    protected static final AttributeKey<String> TYPE = AttributeKey.stringKey("type");

    protected final Attributes m_defaultAttributes;
    protected final LongUpDownCounter m_activeSessions;
    protected final LongCounter m_createdSessions;

    public SessionMetrics(Meter meter, String type) {
      m_activeSessions = meter.upDownCounterBuilder("scout.sessions.active")
          .setDescription("The number of active sessions.")
          .setUnit("{session}")
          .build();
      m_createdSessions = meter.counterBuilder("scout.sessions.created")
          .setDescription("The number of sessions that has been created since server start.")
          .setUnit("{session}")
          .build();

      m_defaultAttributes = Attributes.of(TYPE, type);
    }

    public void sessionCreated() {
      m_activeSessions.add(1, m_defaultAttributes);
      m_createdSessions.add(1, m_defaultAttributes);
    }

    public void sessionDestroyed() {
      m_activeSessions.add(-1, m_defaultAttributes);
    }
  }
}
