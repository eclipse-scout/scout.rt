/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.opentelemetry;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.opentelemetry.OpenTelemetryProperties.OpenTelemetryTracingEnabledProperty;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;

@ApplicationScoped
public class OpenTelemetryExtensionInstrumenterFactory {

  public <OWNER> Instrumenter<OpenTelemetryExtensionRequest<OWNER>, Void> createInstrumenter(Class<?> instrumentationClass,
      Function<OpenTelemetryExtensionRequest<OWNER>, String> getTextFunction) {
    SpanNameExtractor<OpenTelemetryExtensionRequest<OWNER>> spanNameExtractor = request -> request.getOwner().getClass().getSimpleName() + "." + request.getEventName();
    ScoutExtensionAttributesExtractor<OWNER> attributesExtractor = new ScoutExtensionAttributesExtractor<>(getTextFunction);
    return Instrumenter.<OpenTelemetryExtensionRequest<OWNER>, Void> builder(GlobalOpenTelemetry.get(),
            "scout." + instrumentationClass.getSimpleName(),
            spanNameExtractor)
        .addAttributesExtractor(attributesExtractor)
        .setEnabled(CONFIG.getPropertyValue(OpenTelemetryTracingEnabledProperty.class).booleanValue())
        .buildInstrumenter();
  }

  public static class ScoutExtensionAttributesExtractor<T> implements AttributesExtractor<OpenTelemetryExtensionRequest<T>, Void> {

    private static final AttributeKey<String> OWNER_CLASS = AttributeKey.stringKey("scout.extension.owner.class");
    private static final AttributeKey<String> OWNER_DISPLAY_TEXT = AttributeKey.stringKey("scout.extension.owner.display_text");

    private final Function<OpenTelemetryExtensionRequest<T>, String> m_getTextFunction;

    public ScoutExtensionAttributesExtractor(Function<OpenTelemetryExtensionRequest<T>, String> getTextFunction) {
      m_getTextFunction = getTextFunction;
    }

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, OpenTelemetryExtensionRequest<T> request) {
      attributes.put(OWNER_CLASS, request.getOwner().getClass().getName());
      attributes.put(OWNER_DISPLAY_TEXT, m_getTextFunction.apply(request));

      request.getAdditionalAttributesProvider().accept(attributes);
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, OpenTelemetryExtensionRequest<T> request, @Nullable Void unused, @Nullable Throwable error) {
      // nop
    }
  }

  public static class OpenTelemetryExtensionRequest<T> {
    private T m_owner;
    private String m_eventName;
    private Consumer<AttributesBuilder> m_additionalAttributesProvider;

    public OpenTelemetryExtensionRequest(T owner, String eventName) {
      m_owner = owner;
      m_eventName = eventName;
      m_additionalAttributesProvider = attributes -> {};
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      OpenTelemetryExtensionRequest<?> that = (OpenTelemetryExtensionRequest<?>) o;
      return Objects.equals(m_owner, that.m_owner) && Objects.equals(m_eventName, that.m_eventName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(m_owner, m_eventName);
    }

    public T getOwner() {
      return m_owner;
    }

    public String getEventName() {
      return m_eventName;
    }

    public Consumer<AttributesBuilder> getAdditionalAttributesProvider() {
      return m_additionalAttributesProvider;
    }

    public OpenTelemetryExtensionRequest<T> withAdditionalAttributesProvider(Consumer<AttributesBuilder> additionalAttributesProvider) {
      m_additionalAttributesProvider = additionalAttributesProvider;
      return this;
    }

    public <R> R wrapCall(Supplier<R> callable, Instrumenter<OpenTelemetryExtensionRequest<T>, Void> m_instrumenter) {
      Context parentContext = Context.current();

      if (!m_instrumenter.shouldStart(parentContext, this)) {
        return callable.get();
      }

      Context context = m_instrumenter.start(parentContext, this);
      R result;
      try (Scope ignored = context.makeCurrent()) {
        result = callable.get();
      }
      catch (Throwable t) {
        m_instrumenter.end(context, this, null, t);
        throw t;
      }
      m_instrumenter.end(context, this, null, null);
      return result;
    }

    public void wrapCall(Runnable callable, Instrumenter<OpenTelemetryExtensionRequest<T>, Void> m_instrumenter) {
      Context parentContext = Context.current();

      if (!m_instrumenter.shouldStart(parentContext, this)) {
        callable.run();
        return;
      }

      Context context = m_instrumenter.start(parentContext, this);
      try (Scope ignored = context.makeCurrent()) {
        callable.run();
      }
      catch (Throwable t) {
        m_instrumenter.end(context, this, null, t);
        throw t;
      }
      m_instrumenter.end(context, this, null, null);
    }
  }
}
