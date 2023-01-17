/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jaxws.consumer.IPortProvider.IPortInitializer;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

/**
 * Non-blocking, unlimited pool of JAX-WS port instances (i.e. the actual web service client). Pooled entries are
 * discarded 15 minutes after they have been created.
 *
 * @since 6.0.300
 */
public class PortPool<SERVICE extends Service, PORT> extends AbstractNonBlockingPool<PORT> {

  protected final ServicePool<SERVICE> m_servicePool;
  protected final Class<PORT> m_portTypeClazz;
  protected final IPortInitializer m_initializer;

  public PortPool(final ServicePool<SERVICE> servicePool, final Class<PORT> portTypeClazz, final IPortInitializer initializer) {
    super(15, TimeUnit.MINUTES);
    m_servicePool = servicePool;
    m_portTypeClazz = portTypeClazz;
    m_initializer = initializer;
  }

  @Override
  protected PORT createElement() {
    final SERVICE service = m_servicePool.lease();
    try {
      // Install implementor specific webservice features
      final List<WebServiceFeature> webServiceFeatures = new ArrayList<>();
      m_initializer.initWebServiceFeatures(webServiceFeatures);

      // Create the port
      return service.getPort(m_portTypeClazz, CollectionUtility.toArray(webServiceFeatures, WebServiceFeature.class));
    }
    finally {
      m_servicePool.release(service);
    }
  }

  @Override
  protected boolean resetElement(PORT port) {
    final JaxWsImplementorSpecifics implementorSpecifics = BEANS.get(JaxWsImplementorSpecifics.class);
    implementorSpecifics.resetRequestContext(port);
    return implementorSpecifics.isValid(port);
  }
}
