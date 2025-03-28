/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.opentelemetry;

import java.util.Arrays;

import javax.annotation.Nullable;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryProperties.OpenTelemetryTracingEnabledProperty;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;

@ApplicationScoped
public class HttpServiceTunnelInstrumenterFactory {
  public static final String INSTRUMENTATION_NAME = "scout.HttpServiceTunnel";
  private static final String SCOUT_PREFIX = "scout.server.service";

  public static final AttributeKey<String> REQUEST_NAME = AttributeKey.stringKey(SCOUT_PREFIX + ".name");
  public static final AttributeKey<String> OPERATION = AttributeKey.stringKey(SCOUT_PREFIX + ".operation");
  public static final AttributeKey<String> PARAMETER_TYPES = AttributeKey.stringKey(SCOUT_PREFIX + ".parameter.types");

  public Instrumenter<ServiceTunnelRequest, Void> createInstrumenter() {
    SpanNameExtractor<ServiceTunnelRequest> spanNameExtractor = serviceTunnelRequest -> {
      String fullName = serviceTunnelRequest.getServiceInterfaceClassName();
      String serviceName = fullName.substring(fullName.lastIndexOf('.') + 1);
      return "TUNNEL " + serviceName + "." + serviceTunnelRequest.getOperation();
    };
    AttributesExtractor<ServiceTunnelRequest, Void> attributesExtractor = new AttributesExtractor<>() {

      @Override
      public void onStart(AttributesBuilder attributes, Context parentContext, ServiceTunnelRequest serviceTunnelRequest) {
        attributes.put(REQUEST_NAME, serviceTunnelRequest.getServiceInterfaceClassName());
        attributes.put(OPERATION, serviceTunnelRequest.getOperation());
        attributes.put(PARAMETER_TYPES, Arrays.toString(serviceTunnelRequest.getParameterTypes()));
      }

      @Override
      public void onEnd(AttributesBuilder attributes, Context context, ServiceTunnelRequest serviceTunnelRequest, @Nullable Void unused, @Nullable Throwable error) {
        // nop
      }
    };
    return Instrumenter.<ServiceTunnelRequest, Void> builder(GlobalOpenTelemetry.get(),
            INSTRUMENTATION_NAME,
            spanNameExtractor)
        .addAttributesExtractor(attributesExtractor)
        .setEnabled(CONFIG.getPropertyValue(OpenTelemetryTracingEnabledProperty.class))
        .buildInstrumenter(SpanKindExtractor.alwaysClient());
  }
}
