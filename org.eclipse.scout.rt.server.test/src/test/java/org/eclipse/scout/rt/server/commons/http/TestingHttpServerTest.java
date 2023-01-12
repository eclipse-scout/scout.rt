/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.http;

import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.junit.Test;

public class TestingHttpServerTest {

  /**
   * Test if the {@link TestingHttpServer} can in principle be started twice in parallel
   */
  @Test
  public void testMultipleServerListeners() {
    TestingHttpServer server1 = new TestingHttpServer(TestingHttpPorts.PORT_33002);
    TestingHttpServer server2 = new TestingHttpServer(TestingHttpPorts.PORT_33003);
    try {
      server1.start();
      server2.start();
    }
    finally {
      server1.stop();
      server2.stop();
    }
  }

  /**
   * http://172.0.0.1:33xyz/test
   */
  public static class Servlet extends AbstractHttpServlet {
    private static final long serialVersionUID = 1L;
  }
}
