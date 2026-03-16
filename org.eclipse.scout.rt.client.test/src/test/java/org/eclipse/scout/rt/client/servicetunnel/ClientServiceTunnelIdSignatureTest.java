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

import static org.eclipse.scout.rt.platform.util.Assertions.*;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.ws.rs.core.Response.Status;

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
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.rest.id.IdSignatureClientRequestFilter;
import org.eclipse.scout.rt.rest.jersey.TestJerseyMockConnector;
import org.eclipse.scout.rt.rest.jersey.TestingRestClientConfigFactory;
import org.eclipse.scout.rt.rest.jersey.fixture.SingleIdDo;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.services.lookup.IEchoService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientServiceTunnelIdSignatureTest {

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
    var serviceTunnelContentHandler = BEANS.get(BinaryServiceTunnelContentHandler.class);

    // create service tunnel response
    var serviceTunnelResponseOutputStream = new ByteArrayOutputStream();
    serviceTunnelContentHandler.writeResponse(serviceTunnelResponseOutputStream, new ServiceTunnelResponse(echoResponseRaw));

    // create http response returning service tunnel response
    TestJerseyMockConnector testJerseyMockConnector = BEANS.get(TestJerseyMockConnector.class);
    testJerseyMockConnector.setNextResponseStatus(Status.OK);
    byte[] response = serviceTunnelResponseOutputStream.toByteArray();
    testJerseyMockConnector.setNextResponseEntityStream(new ByteArrayInputStream(response));

    // call echo service
    var echoResponse = ServiceTunnelUtility.createProxy(IEchoService.class, ServiceTunnelOptions.create().withIdSignature(idSignature)).echo(o);

    // get id signature request header
    var idSignatureRequestHeader = testJerseyMockConnector.getLastRequestHeaders().get(IdSignatureClientRequestFilter.ID_SIGNATURE_HTTP_HEADER);

    return ImmutablePair.of(echoResponse, idSignatureRequestHeader);
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
    @Serial
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
