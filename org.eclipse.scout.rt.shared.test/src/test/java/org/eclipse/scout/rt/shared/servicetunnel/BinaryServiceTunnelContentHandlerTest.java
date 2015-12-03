/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.CompressServiceTunnelRequestProperty;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * JUnit test for {@link BinaryServiceTunnelContentHandler}
 */
@RunWith(PlatformTestRunner.class)
public class BinaryServiceTunnelContentHandlerTest {

  private IConfigProperty m_compressProperty;
  private IBean m_serviceReg;
  private List<IBean<IConfigProperty>> m_oldBeans;

  @Before
  public void before() {
    m_oldBeans = BEANS.getBeanManager().getRegisteredBeans(IConfigProperty.class);
    for (IBean<IConfigProperty> iBean : m_oldBeans) {
      BEANS.getBeanManager().unregisterBean(iBean);
    }
    m_compressProperty = Mockito.mock(IConfigProperty.class);
    Mockito.when(m_compressProperty.getValue()).thenReturn(true);
    m_serviceReg = TestingUtility.registerBean(new BeanMetaData(CompressServiceTunnelRequestProperty.class, m_compressProperty));
  }

  @After
  public void after() {
    TestingUtility.unregisterBean(m_serviceReg);

    IBeanManager beanManager = BEANS.getBeanManager();
    // restore
    for (IBean<?> bean : m_oldBeans) {
      beanManager.registerBean(new BeanMetaData(bean));
    }
  }

  @Test
  public void request() throws Throwable {
    BinaryServiceTunnelContentHandler handler = new BinaryServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    handler.writeRequest(bos, new ServiceTunnelRequest("test", null, null, null));
    bos.close();
    ServiceTunnelRequest readRequest = handler.readRequest(new ByteArrayInputStream(bos.toByteArray()));
    assertEquals("test", readRequest.getServiceInterfaceClassName());
  }

  @Test
  public void response() throws Throwable {
    BinaryServiceTunnelContentHandler handler = new BinaryServiceTunnelContentHandler();
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
    BinaryServiceTunnelContentHandler handler = new BinaryServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ServiceTunnelResponse response = new ServiceTunnelResponse(testValue, null);
    handler.writeResponse(bos, response);
    bos.close();
    int sizeCompressed = bos.size();

    Mockito.when(m_compressProperty.getValue()).thenReturn(Boolean.FALSE);

    handler = new BinaryServiceTunnelContentHandler();
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
    BinaryServiceTunnelContentHandler handler = new BinaryServiceTunnelContentHandler();
    handler.initialize();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ServiceTunnelResponse request = new ServiceTunnelResponse(testValue, null);
    handler.writeResponse(bos, request);
    bos.close();
    int sizeCompressed = bos.size();

    Mockito.when(m_compressProperty.getValue()).thenReturn(Boolean.FALSE);

    handler = new BinaryServiceTunnelContentHandler();
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
