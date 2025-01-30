/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryProperties.OpenTelemetryTracingEnabledProperty;
import org.eclipse.scout.rt.platform.util.LazyValue;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import io.opentelemetry.context.Scope;

/**
 * <p>
 * Custom Context Storage implementation such that the OpenTelemetry context is propagated between the Scout's UI and
 * Model Thread. Therefore the RunContext must be kept in sync with the OpenTelemetry context.
 * </p>
 * See also: {@link OpenTelemetryContextProcessor}
 */
public class ScoutOpenTelemetryContextStorage implements ContextStorageProvider {
  private LazyValue<Boolean> m_tracingEnabled = new LazyValue<>(() -> CONFIG.getPropertyValue(OpenTelemetryTracingEnabledProperty.class));

  @Override
  public ContextStorage get() {
    ContextStorage threadLocalStorage = ContextStorage.defaultStorage();

    return new ContextStorage() {
      @Override
      public Scope attach(Context toAttach) {
        if (!m_tracingEnabled.get().booleanValue()) {
          return threadLocalStorage.attach(toAttach);
        }

        Context current = current();

        RunContext runContext = RunContext.CURRENT.get();
        if (runContext != null) {
          runContext.withOpenTelemetryContext(toAttach);
        }
        Scope scope = threadLocalStorage.attach(toAttach);
        return () -> {
          if (runContext != null) {
            runContext.withOpenTelemetryContext(current);
          }
          scope.close();
        };
      }

      @Override
      public Context current() {
        return threadLocalStorage.current();
      }
    };
  }
}
