/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.JaxWsConsumerTestService;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.JaxWsConsumerTestServicePortType;
import org.eclipse.scout.rt.server.jaxws.consumer.pool.PooledPortProvider;

/**
 * Web service client for the {@link JaxWsConsumerTestServiceProvider} testing service.
 *
 * @since 6.0.300
 */
public class JaxWsConsumerTestClient extends AbstractWebServiceClient<JaxWsConsumerTestService, JaxWsConsumerTestServicePortType> {

  public void setEndpointUrl(String endpointUrl) {
    m_endpointUrl = endpointUrl;
  }

  /**
   * Discards all ports and services pooled by the {@link PooledPortProvider}. Does nothing if a different
   * {@link IPortProvider} is used.
   */
  public void discardAllPoolEntries() {
    if (!(m_portProvider instanceof PooledPortProvider<?, ?>)) {
      return;
    }

    ((PooledPortProvider<?, ?>) m_portProvider).discardAllPoolEntries();
  }
}
