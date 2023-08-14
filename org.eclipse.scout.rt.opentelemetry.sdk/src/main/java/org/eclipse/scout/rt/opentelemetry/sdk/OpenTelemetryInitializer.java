/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.opentelemetry.IHistogramViewHintProvider;
import org.eclipse.scout.rt.platform.opentelemetry.IMetricProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;

/**
 * Initialize {@link GlobalOpenTelemetry} instance, "auto" configured by using environment properties, system properties
 * and/or Scout config properties.
 *
 * @see AutoConfiguredOpenTelemetrySdk
 * @see <a href=
 *      "https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md">OpenTelemetry
 *      SDK Autoconfigure</a>
 */
@Order(4_001)
public class OpenTelemetryInitializer implements IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(OpenTelemetryInitializer.class);

  protected OpenTelemetrySdk m_openTelemetry;

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.BeanManagerPrepared) {
      initOpenTelemetry();
    }
  }

  /**
   * Registers an {@link IPlatformListener} that shutdown OpenTelemetry on an {@link IPlatform.State#PlatformStopping}
   * event. It does nothing if this method is invoked when no platform is active (i.e. {@link Platform#peek()} returns
   * {@code null}).
   * <p>
   * <b>Note:</b> We use a separate listener instead to provide OpenTelemetry functionality as long as possible during
   * shutdown.
   */
  protected void registerShutdownListener() {
    final IPlatform platform = Platform.peek();
    if (platform == null) {
      return;
    }
    platform.getBeanManager().registerBean(new BeanMetaData(IPlatformListener.class)
        .withApplicationScoped(true)
        .withOrder(5_999)
        .withInitialInstance((IPlatformListener) event -> {
          if (event.getState() == IPlatform.State.PlatformStopping) {
            shutdownOpenTelemetry();
          }
        }));
  }

  protected void initOpenTelemetry() {
    if (!CONFIG.getPropertyValue(OpenTelemetryInitializerEnabledProperty.class)) {
      LOG.info("Skip Scout OpenTelemetry initialization.");
      return;
    }
    LOG.info("Initialize OpenTelemetry");

    // configuration provided by environment variables and/or system properties
    m_openTelemetry = AutoConfiguredOpenTelemetrySdk.builder()
        .addPropertiesSupplier(this::getDefaultProperties)
        .addMeterProviderCustomizer(this::customizeMeterProvider)
        .disableShutdownHook()
        .setResultAsGlobal()
        .build()
        .getOpenTelemetrySdk();

    registerShutdownListener();

    initMetrics();
  }

  protected Map<String, String> getDefaultProperties() {
    String defaultExporter = CONFIG.getPropertyValue(OpenTelemetryDefaultExporterProperty.class);
    Map<String, String> defaultConfig = new HashMap<>();
    defaultConfig.put("otel.traces.exporter", "none");
    defaultConfig.put("otel.logs.exporter", "none");
    defaultConfig.put("otel.metrics.exporter", defaultExporter);
    defaultConfig.put("otel.exporter.otlp.protocol", "http/protobuf");
    defaultConfig.put("otel.metric.export.interval", "30000"); // 30s

    defaultConfig.put("otel.service.name", CONFIG.getPropertyValue(ApplicationNameProperty.class));
    defaultConfig.put("otel.resource.attributes", "service.instance.id=" + NodeId.current().unwrapAsString());
    return defaultConfig;
  }

  protected SdkMeterProviderBuilder customizeMeterProvider(SdkMeterProviderBuilder builder, ConfigProperties config) {
    for (IHistogramViewHintProvider viewHintProvider : BEANS.all(IHistogramViewHintProvider.class)) {
      LOG.info("Initialize view from {}", viewHintProvider.getClass().getName());
      builder.registerView(
          InstrumentSelector.builder()
              .setName(viewHintProvider.getInstrumentName())
              .setType(InstrumentType.HISTOGRAM)
              .build(),
          View.builder()
              .setAggregation(Aggregation.explicitBucketHistogram(viewHintProvider.getExplicitBuckets()))
              .build());
    }
    return builder;
  }

  protected void initMetrics() {
    for (IMetricProvider metricProvider : BEANS.all(IMetricProvider.class)) {
      LOG.info("Initialize metrics from {}", metricProvider.getClass().getName());
      metricProvider.register(GlobalOpenTelemetry.get());
    }
  }

  protected void shutdownOpenTelemetry() {
    LOG.info("Shutting down OpenTelemetry");
    if (m_openTelemetry == null) {
      return;
    }

    BEANS.all(IMetricProvider.class).forEach(IMetricProvider::close);
    m_openTelemetry.close();
    m_openTelemetry = null;
  }

  public static class OpenTelemetryInitializerEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.otel.initializerEnabled";
    }

    @Override
    public String description() {
      return "Property to specify if the application is using the Scout OpenTelemetry initializer. Default is false. Set to false if you are using the OpenTelemetry Java Agent.";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.FALSE;
    }
  }

  public static class OpenTelemetryDefaultExporterProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.otel.defaultExporter";
    }

    @Override
    public String description() {
      return "List of exporters to be used for traces, metrics and logs, separated by commas. Default is 'otlp' or 'none' in dev mode. Use this property to override the default.";
    }

    @Override
    public String getDefaultValue() {
      if (Platform.get().inDevelopmentMode()) {
        return "none"; // use no autoconfigured exporter
      }
      return "otlp";
    }
  }
}
