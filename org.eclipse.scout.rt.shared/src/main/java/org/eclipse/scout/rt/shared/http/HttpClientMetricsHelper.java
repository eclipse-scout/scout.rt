/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.shared.http;

import java.util.function.Supplier;

import org.apache.hc.core5.pool.PoolStats;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

/**
 * Helper to provide metrics for Scout's HTTP clients.
 *
 * @see <a href=
 *      "https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-metrics.md#metric-httpclientopen_connections">
 *      OpenTelemetry: Semantic Conventions for HTTP Metrics</a>
 */
@ApplicationScoped
public class HttpClientMetricsHelper {

  private static final Logger LOG = LoggerFactory.getLogger(HttpClientMetricsHelper.class);

  protected static final AttributeKey<String> HTTP_CLIENT_NAME = AttributeKey.stringKey("http.client.name");
  protected static final AttributeKey<String> HTTP_CONNECTION_STATE = AttributeKey.stringKey("state");

  public void initMetrics(Meter meter, String httpClientName, Supplier<PoolStats> poolStatsSupplier) {
    Assertions.assertNotNullOrEmpty(httpClientName, "HTTP client name not specified. A Java process can use multiple HTTP connection providers (pools). To distinguish them in such situations, a unique HTTP client name is required.");
    LOG.info("Init HTTP client connection pool '{}'", httpClientName);

    ObservableLongMeasurement connectionsUsage = meter.upDownCounterBuilder("http.client.open_connections")
        .setDescription("Number of outbound HTTP connections that are currently active or idle on the client.")
        .setUnit("{connection}")
        .buildObserver();
    ObservableLongMeasurement maxConnections = meter.upDownCounterBuilder("http.client.connections.max")
        .setDescription("The maximum number of allowed outbound HTTP connections.")
        .setUnit("{connection}")
        .buildObserver();

    Attributes defaultAttributes = Attributes.of(HTTP_CLIENT_NAME, httpClientName);
    Attributes activeConnectionsAttributes = defaultAttributes.toBuilder().put(HTTP_CONNECTION_STATE, "active").build();
    Attributes idleConnectionsAttributes = defaultAttributes.toBuilder().put(HTTP_CONNECTION_STATE, "idle").build();

    //noinspection resource
    meter.batchCallback(() -> {
      PoolStats stats = poolStatsSupplier.get();
      connectionsUsage.record(stats.getLeased(), activeConnectionsAttributes);
      connectionsUsage.record(stats.getAvailable(), idleConnectionsAttributes);
      maxConnections.record(stats.getMax(), defaultAttributes);
    },
        connectionsUsage,
        maxConnections);
  }
}
