/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.SharedConfigProperties.CompressServiceTunnelRequestProperty;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link SoapServiceTunnelContentHandler}
 */
@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class SoapServiceTunnelContentHandlerTest {

  @Before
  public void before() {
    CompressServiceTunnelRequestProperty prop = BEANS.get(CompressServiceTunnelRequestProperty.class);
    prop.invalidate();
  }

  @Test
  public void request() throws Throwable {
    SoapServiceTunnelContentHandler handler = new SoapServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    handler.writeRequest(bos, new ServiceTunnelRequest("test", "testop", null, null));
    bos.close();
    ServiceTunnelRequest readRequest = handler.readRequest(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals("test", readRequest.getServiceInterfaceClassName());
  }

  @Test
  public void response() throws Throwable {
    SoapServiceTunnelContentHandler handler = new SoapServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    handler.writeResponse(bos, new ServiceTunnelResponse("test", null));
    bos.close();
    ServiceTunnelResponse readResponse = handler.readResponse(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals("test", readResponse.getData());
  }

  @Test
  public void requestUncompressed() throws Throwable {
    String testValue = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    SoapServiceTunnelContentHandler handler = new SoapServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ServiceTunnelResponse response = new ServiceTunnelResponse(testValue, null);
    handler.writeResponse(bos, response);
    bos.close();
    int sizeCompressed = bos.size();

    CompressServiceTunnelRequestProperty prop = BEANS.get(CompressServiceTunnelRequestProperty.class);
    prop.invalidate();
    prop.setValue(Boolean.FALSE);

    handler = new SoapServiceTunnelContentHandler();
    handler.initialize();
    bos = new ByteArrayOutputStream();
    handler.writeResponse(bos, response);
    bos.close();
    int sizeUncompressed = bos.size();
    ServiceTunnelResponse readRequest = handler.readResponse(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals(testValue, readRequest.getData());
    assertTrue(String.format("sizeUncompressed: %s, sizeCompressed: %s", sizeUncompressed, sizeCompressed), sizeUncompressed > sizeCompressed);
  }

  @Test
  public void responseUncompressed() throws Throwable {
    String testValue = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    SoapServiceTunnelContentHandler handler = new SoapServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ServiceTunnelResponse request = new ServiceTunnelResponse(testValue, null);
    handler.writeResponse(bos, request);
    bos.close();
    int sizeCompressed = bos.size();

    CompressServiceTunnelRequestProperty prop = BEANS.get(CompressServiceTunnelRequestProperty.class);
    prop.invalidate();
    prop.setValue(Boolean.FALSE);

    handler = new SoapServiceTunnelContentHandler();
    handler.initialize();
    bos = new ByteArrayOutputStream();
    handler.writeResponse(bos, request);
    bos.close();
    int sizeUncompressed = bos.size();
    ServiceTunnelResponse readResponse = handler.readResponse(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals(testValue, readResponse.getData());
    assertTrue(String.format("sizeUncompressed: %s, sizeCompressed: %s", sizeUncompressed, sizeCompressed), sizeUncompressed > sizeCompressed);
  }
}
