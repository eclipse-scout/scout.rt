/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.http;

import java.io.IOException;

import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.junit.Test;

public class TestingHttpServerTest {

  /**
   * Test if the {@link TestingHttpServer} can in principle be started twice in parallel
   */
  @Test
  public void testMultipleServerListeners() throws IOException {
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
