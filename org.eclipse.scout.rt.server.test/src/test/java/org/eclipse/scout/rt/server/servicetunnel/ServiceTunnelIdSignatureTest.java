/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.servicetunnel;

import static org.eclipse.scout.rt.platform.util.Assertions.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.dataobject.DataObjectHolder;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityHolder;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.fixture.FixtureIntegerId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.id.IdCodec.IdCodecFlag;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.server.ServiceTunnelServlet;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.commons.BufferedServletInputStream;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.http.AbstractHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnel;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.TestHttpSession;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ServiceTunnelIdSignatureTest {

  protected static List<IBean<?>> s_beans;

  @BeanMock
  private ServiceTunnelTargetUrlProperty mockUrl;

  @BeforeClass
  public static void beforeClass() {
    s_beans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(P_IdCodec.class).withReplace(true),
        new BeanMetaData(P_HttpServiceTunnel.class).withReplace(true).withApplicationScoped(true)
    );
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(s_beans);
  }

  @Test
  public void testServiceTunnel() throws IOException {
    // unsigned request
    var echoResponseIdSignatureRequestHeaderPair = callServiceTunnelEchoService(new EchoBean(createSingleIdDo()), new EchoBean(createSingleIdDoRaw(false)), false);
    var echoBean = assertInstance(echoResponseIdSignatureRequestHeaderPair.getLeft(), EchoBean.class);
    assertEchoBean(createSingleIdDo(), echoBean);
    assertFalse(Boolean.TRUE.toString().equals(echoResponseIdSignatureRequestHeaderPair.getRight()));

    assertThrows(PlatformException.class, () -> callServiceTunnelEchoService(new EchoBean(createSingleIdDo()), new EchoBean(createSingleIdDoRaw(true)), false));

    // signed request
    assertThrows(PlatformException.class, () -> callServiceTunnelEchoService(new EchoBean(createSingleIdDo()), new EchoBean(createSingleIdDoRaw(false)), true));

    echoResponseIdSignatureRequestHeaderPair = callServiceTunnelEchoService(new EchoBean(createSingleIdDo()), new EchoBean(createSingleIdDoRaw(true)), true);
    echoBean = assertInstance(echoResponseIdSignatureRequestHeaderPair.getLeft(), EchoBean.class);
    assertEchoBean(createSingleIdDo(), echoBean);
    assertTrue(Boolean.TRUE.toString().equals(echoResponseIdSignatureRequestHeaderPair.getRight()));
  }

  protected Pair<Object /* echo response */, String /* id signature request header */> callServiceTunnelEchoService(Object o, Object echoResponseRaw, boolean idSignature) throws IOException {
    when(mockUrl.getValue()).thenReturn("http://localhost");

    var serviceTunnelContentHandler = BEANS.get(IServiceTunnelContentHandler.class);
    serviceTunnelContentHandler.initialize();

    // create service tunnel response
    var serviceTunnelResponseOutputStream = new ByteArrayOutputStream();
    serviceTunnelContentHandler.writeResponse(serviceTunnelResponseOutputStream, new ServiceTunnelResponse(echoResponseRaw));

    // create http response returning service tunnel response
    var response = new MockLowLevelHttpResponse().setContent(serviceTunnelResponseOutputStream.toByteArray());
    BEANS.get(P_HttpServiceTunnel.class).setNextLowLevelHttpResponse(response);

    // call echo service
    var echoResponse = ServiceTunnelUtility.createProxy(IEchoService.class, ServiceTunnelOptions.create().withIdSignature(idSignature)).echo(o);

    // get id signature request header
    var request = BEANS.get(P_HttpServiceTunnel.class).getLastHttpResponse().getRequest();
    var idSignatureRequestHeader = (String) request.getHeaders().get(HttpServiceTunnel.ID_SIGNATURE_HTTP_HEADER);

    return ImmutablePair.of(echoResponse, idSignatureRequestHeader);
  }

  @Test
  public void testServiceTunnelServlet() throws IOException, ServletException {
    // unsigned request
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDo()), false));
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(false)), false));
    assertNull(callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(true)), false));

    // signed request
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDo()), true));
    assertNull(callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(false)), true));
    assertServiceTunnelResponse(createSingleIdDo(), callServiceTunnelServletEchoService(new EchoBean(createSingleIdDoRaw(true)), true));
  }

  protected ServiceTunnelResponse callServiceTunnelServletEchoService(Object o, boolean idSignature) throws IOException, ServletException {
    // create service tunnel request
    var serviceTunnelRequest = new ServiceTunnelRequest(IEchoService.class.getName(), "echo", new Class[]{Object.class}, new Object[]{o});
    serviceTunnelRequest.setUserAgent("UNKNOWN|UNKNOWN|UNKNOWN|UNKNOWN|UNKNOWN");
    serviceTunnelRequest.setSessionId("4242");

    var serviceTunnelContentHandler = BEANS.get(IServiceTunnelContentHandler.class);
    serviceTunnelContentHandler.initialize();

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
    Mockito.when(request.getHeader(HttpServiceTunnel.ID_SIGNATURE_HTTP_HEADER)).thenReturn("" + idSignature);

    // create servlet output stream and response to collect service tunnel response
    var servletOutputStream = new BufferedServletOutputStream();
    var response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(response.getOutputStream()).thenReturn(servletOutputStream);

    // call service tunnel servlet
    new ServiceTunnelServlet().service(request, response);

    // error occurred -> no response to read
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

  protected static class P_HttpServiceTunnel extends HttpServiceTunnel {

    private MockHttpTransport m_transport;
    private HttpResponse m_lastResponse;

    public void setNextLowLevelHttpResponse(MockLowLevelHttpResponse response) {
      m_transport = new MockHttpTransport.Builder()
          .setLowLevelHttpResponse(response)
          .build();
    }

    public HttpResponse getLastHttpResponse() {
      return m_lastResponse;
    }

    @Override
    protected HttpResponse executeRequestInternal(ServiceTunnelRequest call, byte[] callData) throws IOException {
      m_lastResponse = null;

      m_lastResponse = super.executeRequestInternal(call, callData);
      m_transport = null;

      return m_lastResponse;
    }

    @Override
    protected IHttpTransportManager getHttpTransportManager() {

      return new AbstractHttpTransportManager() {

        @Override
        public String getName() {
          return "scout.transport.test-service-tunnel-id-signature";
        }

        @Override
        public HttpTransport getHttpTransport() {
          return m_transport;
        }

        @Override
        public HttpRequestFactory getHttpRequestFactory() {
          return m_transport.createRequestFactory(createHttpRequestInitializer());
        }
      };
    }
  }
}
