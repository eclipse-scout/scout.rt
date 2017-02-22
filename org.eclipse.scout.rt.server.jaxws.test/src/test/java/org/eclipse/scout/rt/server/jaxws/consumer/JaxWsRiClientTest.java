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

import javax.xml.ws.Endpoint;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Tests the behavior of a JAX-WS client that is based on the JRE's reference implementation (RI).
 *
 * @since 6.0.300
 */
public class JaxWsRiClientTest extends AbstractJaxWsClientTest {

  private static Endpoint s_echoEndpoint;
  private static Endpoint s_pingEndpoint;

  @BeforeClass
  public static void sartupWsProvider() {
    s_echoEndpoint = Endpoint.publish("http://localhost:8085/WS/JaxWsConsumerTestService", new JaxWsConsumerTestServiceProvider());
    s_pingEndpoint = Endpoint.publish("http://localhost:8085/WS/JaxWsPingTestService", new JaxWsPingTestServiceProvider());
  }

  @AfterClass
  public static void stopWsProvider() {
    s_echoEndpoint.stop();
    s_pingEndpoint.stop();
  }
}
