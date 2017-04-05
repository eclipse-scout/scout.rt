/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.JaxWsPingTestService;
import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.JaxWsPingTestServicePortType;
import org.eclipse.scout.rt.server.jaxws.consumer.pool.PooledPortProvider;

/**
 * Web service client for the {@link JaxWsPingTestServicePortType}.
 *
 * @since 6.0.300
 */
public class JaxWsPingTestClient extends AbstractWebServiceClient<JaxWsPingTestService, JaxWsPingTestServicePortType> {

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
