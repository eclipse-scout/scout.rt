/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.Supplier;

import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.PoolStats;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.opentelemetry.api.metrics.Meter;

/**
 * Abstract test case for correct metrics initialization of HTTP connection pool.
 */
public abstract class AbstractHttpClientMetricsTest {

  protected void runTestInitMetrics(Runnable metricsInitializer, ConnPoolStats<?> connPoolStatsMock, String httpClientName) {
    IBean<?> bean = null;
    try {
      HttpClientMetricsHelper mock = Mockito.mock(HttpClientMetricsHelper.class);
      bean = BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(HttpClientMetricsHelper.class, mock));
      @SuppressWarnings("unchecked")
      ArgumentCaptor<Supplier<Integer>> idleCaptor = ArgumentCaptor.forClass(Supplier.class);
      @SuppressWarnings("unchecked")
      ArgumentCaptor<Supplier<Integer>> activeCaptor = ArgumentCaptor.forClass(Supplier.class);
      @SuppressWarnings("unchecked")
      ArgumentCaptor<Supplier<Integer>> maxCaptor = ArgumentCaptor.forClass(Supplier.class);

      when(connPoolStatsMock.getTotalStats()).thenReturn(new PoolStats(10, 0, 20, 40));
      metricsInitializer.run();

      // verify init once and pool stats values
      verify(mock, only()).initMetrics(any(Meter.class), eq(httpClientName), idleCaptor.capture(), activeCaptor.capture(), maxCaptor.capture());
      assertEquals(Integer.valueOf(20), idleCaptor.getValue().get());
      assertEquals(Integer.valueOf(10), activeCaptor.getValue().get());
      assertEquals(Integer.valueOf(40), maxCaptor.getValue().get());

      // verify changed pool stats values
      when(connPoolStatsMock.getTotalStats()).thenReturn(new PoolStats(20, 0, 10, 50));
      assertEquals(Integer.valueOf(10), idleCaptor.getValue().get());
      assertEquals(Integer.valueOf(20), activeCaptor.getValue().get());
      assertEquals(Integer.valueOf(50), maxCaptor.getValue().get());
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }
}
