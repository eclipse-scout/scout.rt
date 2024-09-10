/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.async;

import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.eclipse.scout.rt.shared.http.AbstractHttpClientMetricsTest;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link DefaultAsyncHttpClientManager}
 */
public class DefaultAsyncHttpClientManagerTest extends AbstractHttpClientMetricsTest {

  @Test
  public void testInitMetrics() {
    PoolingAsyncClientConnectionManager poolingConnectionManagerMock = Mockito.mock(PoolingAsyncClientConnectionManager.class);
    DefaultAsyncHttpClientManager asyncHttpClientManager = new DefaultAsyncHttpClientManager();
    runTestInitMetrics(() -> asyncHttpClientManager.initMetrics(poolingConnectionManagerMock), poolingConnectionManagerMock, asyncHttpClientManager.getName());
  }
}
