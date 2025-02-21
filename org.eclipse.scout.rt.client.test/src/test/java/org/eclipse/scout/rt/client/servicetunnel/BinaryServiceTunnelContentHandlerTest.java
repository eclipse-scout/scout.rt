/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link BinaryServiceTunnelContentHandler}
 */
@RunWith(PlatformTestRunner.class)
public class BinaryServiceTunnelContentHandlerTest {

  @Test
  public void request() throws Throwable {
    BinaryServiceTunnelContentHandler handler = BEANS.get(BinaryServiceTunnelContentHandler.class);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    handler.writeRequest(bos, new ServiceTunnelRequest("test", null, null, null));
    bos.close();
    ServiceTunnelRequest readRequest = handler.readRequest(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals("test", readRequest.getServiceInterfaceClassName());
  }

  @Test
  public void response() throws Throwable {
    BinaryServiceTunnelContentHandler handler = BEANS.get(BinaryServiceTunnelContentHandler.class);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    handler.writeResponse(bos, new ServiceTunnelResponse("test", null));
    bos.close();
    ServiceTunnelResponse readResponse = handler.readResponse(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals("test", readResponse.getData());
  }
}
