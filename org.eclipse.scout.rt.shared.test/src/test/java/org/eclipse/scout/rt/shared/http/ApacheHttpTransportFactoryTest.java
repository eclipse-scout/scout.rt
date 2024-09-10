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

import static org.mockito.Mockito.when;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link ApacheHttpTransportFactory}
 */
public class ApacheHttpTransportFactoryTest extends AbstractHttpClientMetricsTest {

  @Test
  public void testInitMetrics() {
    IHttpTransportManager manager = Mockito.mock(IHttpTransportManager.class);
    when(manager.getName()).thenReturn("mock-transport-name");
    PoolingHttpClientConnectionManager connectionManagerMock = Mockito.mock(PoolingHttpClientConnectionManager.class);
    ApacheHttpTransportFactory transportFactory = new ApacheHttpTransportFactory();
    runTestInitMetrics(() -> transportFactory.initMetrics(manager, connectionManagerMock), connectionManagerMock, manager.getName());
  }
}

