/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.servicetunnel;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.eclipse.scout.rt.dataobject.DataObjectHolder;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.fixture.FixtureIntegerId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.jackson.dataobject.JacksonDataObjectMapper;
import org.eclipse.scout.rt.jackson.dataobject.JacksonIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.rest.id.IdSignatureClientRequestFilter;
import org.eclipse.scout.rt.rest.jersey.TestingRestClientConfigFactory;
import org.eclipse.scout.rt.rest.jersey.fixture.SingleIdDo;
import org.eclipse.scout.rt.server.commons.BufferedServletInputStream;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.TestHttpSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.services.lookup.IEchoService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ServerTestRunner.class)
@RunWithSubject("default")
public class ServiceTunnelIdSignatureTest {

  protected static List<IBean<?>> s_beans;

  @BeforeClass
  public static void beforeClass() {
    s_beans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(P_IdCodec.class).withReplace(true)
    );
    BEANS.get(TestingRestClientConfigFactory.class).setupMockConnectorProvider();
  }

  @SuppressWarnings("deprecation")
  @AfterClass
  public static void afterClass() {
    // clear serializer cache. Otherwise, the replaced codec might still be used after the test
    BEANS.get(JacksonDataObjectMapper.class).getObjectMapper().clearCaches();
    BEANS.get(JacksonIdSignatureDataObjectMapper.class).getObjectMapper().clearCaches();
    BeanTestingHelper.get().unregisterBeans(s_beans);
    BEANS.get(TestingRestClientConfigFactory.class).resetConnectorProvider();
  }

  @Test
  public void testServiceTunnelServlet() throws IOException {
    // unsigned request
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDo()), false));
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(false)), false));
    assertNull(callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(true)), false));

    // signed request
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDo()), true));
    assertNull(callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(false)), true));
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(true)), true));
  }

  protected ServiceTunnelResponse callServiceTunnelServletEchoService(Object o, boolean idSignature) throws IOException {
    // create service tunnel request
    var serviceTunnelRequest = new ServiceTunnelRequest(IEchoService.class.getName(), "echo", new Class[]{Object.class}, new Object[]{o});
    serviceTunnelRequest.setUserAgent("UNKNOWN|UNKNOWN|UNKNOWN|UNKNOWN|UNKNOWN");

    var serviceTunnelContentHandler = BEANS.get(BinaryServiceTunnelContentHandler.class);

    // create servlet input stream returning the service tunnel request
    var serviceTunnelRequestOutputStream = new ByteArrayOutputStream();
    RunContexts.copyCurrent()
        .withProperty(ServiceTunnelOptions.ID_SIGNATURE_PROP, idSignature)
        .run(() -> serviceTunnelContentHandler.writeRequest(serviceTunnelRequestOutputStream, serviceTunnelRequest));
    var servletInputStream = new BufferedServletInputStream(new ByteArrayInputStream(serviceTunnelRequestOutputStream.toByteArray()));

    // create request returning the servlet input stream
    var httpSession = new TestHttpSession("42");
    var request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getMethod()).thenReturn("POST");
    Mockito.when(request.getInputStream()).thenReturn(servletInputStream);
    Mockito.when(request.getSession()).thenReturn(httpSession);
    Mockito.when(request.getSession(false)).thenReturn(httpSession);
    Mockito.when(request.getHeader(IdSignatureClientRequestFilter.ID_SIGNATURE_HTTP_HEADER)).thenReturn("" + idSignature);
    Mockito.when(request.getHeader(SessionId.HTTP_HEADER_NAME)).thenReturn("4242");

    // create servlet output stream and response to collect service tunnel response
    var servletOutputStream = new BufferedServletOutputStream();
    var response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(response.getOutputStream()).thenReturn(servletOutputStream);

    // call service tunnel servlet
    try {
      RunContexts.copyCurrent()
          .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, request)
          .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, response)
          .run(() -> {
            Response r = BEANS.get(ServiceTunnelService.class).incomingRequest(request.getInputStream());
            ((StreamingOutput) r.getEntity()).write(response.getOutputStream());
          });
    }
    catch (RuntimeException t) {
      // error occurred -> no response to read
      return null;
    }

    if (servletOutputStream.getContent().length == 0) {
      return null;
    }

    // read service tunnel response
    return RunContexts.copyCurrent()
        .withProperty(ServiceTunnelOptions.ID_SIGNATURE_PROP, idSignature)
        .call(() -> serviceTunnelContentHandler.readResponse(new ByteArrayInputStream(servletOutputStream.getContent())));
  }

  protected static void assertServiceTunnelResponse(IDoEntity expectedDoEntity, ServiceTunnelResponse serviceTunnelResponse) {
    assertNotNull(serviceTunnelResponse);

    assertNotNull(serviceTunnelResponse.getData());
    assertNull(serviceTunnelResponse.getException());

    var echoBean = assertInstance(serviceTunnelResponse.getData(), EchoBean.class);
    assertEchoBean(expectedDoEntity, echoBean);
  }

  protected static void assertEchoBean(IDoEntity expectedDoEntity, EchoBean echoBean) {
    assertEquals(expectedDoEntity, echoBean.getDoEntity());
    assertEquals(expectedDoEntity, echoBean.getDoEntityDoEntityHolder());
    assertEquals(expectedDoEntity, echoBean.getDoEntityDataObjectHolder());
  }

  protected SingleIdDo createSingleIdDo() {
    return BEANS.get(SingleIdDo.class)
        .withId(FixtureIntegerId.of(42));
  }

  protected IDoEntity createSingleIdDoRaw(boolean idSignature) {
    var singleIdDoRaw = BEANS.get(DoEntity.class);
    singleIdDoRaw.put("_type", "scout.SingleId");
    singleIdDoRaw.put("id", BEANS.get(IdCodec.class).toQualified(FixtureIntegerId.of(42), idSignature ? IdCodecFlag.SIGNATURE : null));
    return singleIdDoRaw;
  }

  protected static class EchoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private final IDoEntity m_doEntity;
    private final DoEntityHolder<IDoEntity> m_doEntityDoEntityHolder = new DoEntityHolder<>();
    private final DataObjectHolder<IDoEntity> m_doEntityDataObjectHolder = new DataObjectHolder<>();

    public EchoBean(IDoEntity doEntity) {
      m_doEntity = doEntity;
      m_doEntityDoEntityHolder.setValue(doEntity);
      m_doEntityDataObjectHolder.setValue(doEntity);
    }

    public IDoEntity getDoEntity() {
      return m_doEntity;
    }

    public IDoEntity getDoEntityDoEntityHolder() {
      return m_doEntityDoEntityHolder.getValue();
    }

    public IDoEntity getDoEntityDataObjectHolder() {
      return m_doEntityDataObjectHolder.getValue();
    }
  }

  protected static class P_IdCodec extends IdCodec {

    @Override
    protected byte[] getIdSignaturePassword() {
      return "42".getBytes(StandardCharsets.UTF_8);
    }
  }
}
