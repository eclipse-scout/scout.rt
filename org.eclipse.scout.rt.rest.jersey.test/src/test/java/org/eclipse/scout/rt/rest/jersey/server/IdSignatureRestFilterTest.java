/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.dataobject.IIdSignatureDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.dataobject.fixture.FixtureIntegerId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.fixture.SerializationRequest;
import org.eclipse.scout.rt.rest.jersey.fixture.SerializationResponse;
import org.eclipse.scout.rt.rest.jersey.fixture.SingleIdDo;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class IdSignatureRestFilterTest {

  protected static IBean<?> s_bean;

  private WebTarget m_target;

  @BeforeClass
  public static void beforeClass() {
    s_bean = BeanTestingHelper.get().registerBean(new BeanMetaData(P_IdCodec.class).withReplace(true));
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBean(s_bean);
  }

  @Before
  public void before() {
    m_target = BEANS.get(JerseyTestRestClientHelper.class).target("api/serialization");
  }

  protected Builder request() {
    return m_target
        .request()
        .accept(MediaType.APPLICATION_JSON);
  }

  @Test
  public void testSigned() {
    var singleIdDo = BEANS.get(SingleIdDo.class)
        .withId(FixtureIntegerId.of(42));
    var request = BEANS.get(SerializationRequest.class)
        .withBody(singleIdDo);

    // ids need to be signed, but are not signed -> exception
    assertThrows(VetoException.class, () -> request()
        .header(IdSignatureRestContainerFilter.ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString())
        .post(Entity.json(BEANS.get(IPrettyPrintDataObjectMapper.class).writeValue(request)), SerializationResponse.class));

    assertEquals(
        BEANS.get(SerializationResponse.class)
            .withOriginal(singleIdDo)
            .withSerialized("{\"_type\":\"scout.SingleId\",\"id\":\"scout.FixtureIntegerId:42\"}"),
        request()
            .header(IdSignatureRestContainerFilter.ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString())
            .post(Entity.json(BEANS.get(IIdSignatureDataObjectMapper.class).writeValue(request)), SerializationResponse.class));

    assertEquals(
        BEANS.get(SerializationResponse.class)
            .withOriginal(singleIdDo)
            .withSerialized("{\"_type\":\"scout.SingleId\",\"id\":\"scout.FixtureIntegerId:42\"}"),
        request()
            .header(IdSignatureRestContainerFilter.ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString())
            .post(Entity.json(request), SerializationResponse.class));
  }

  @Test
  public void testUnsigned() {
    var singleIdDo = BEANS.get(SingleIdDo.class)
        .withId(FixtureIntegerId.of(42));
    var request = BEANS.get(SerializationRequest.class)
        .withBody(singleIdDo);

    assertEquals(
        BEANS.get(SerializationResponse.class)
            .withOriginal(singleIdDo)
            .withSerialized("{\"_type\":\"scout.SingleId\",\"id\":\"scout.FixtureIntegerId:42\"}"),
        request()
            .post(Entity.json(BEANS.get(IPrettyPrintDataObjectMapper.class).writeValue(request)), SerializationResponse.class));

    // ids need to be not signed, but are signed -> exception
    assertThrows(VetoException.class, () -> request()
        .post(Entity.json(BEANS.get(IIdSignatureDataObjectMapper.class).writeValue(request)), SerializationResponse.class));

    assertEquals(
        BEANS.get(SerializationResponse.class)
            .withOriginal(singleIdDo)
            .withSerialized("{\"_type\":\"scout.SingleId\",\"id\":\"scout.FixtureIntegerId:42\"}"),
        request()
            .post(Entity.json(request), SerializationResponse.class));
  }

  protected static class P_IdCodec extends IdCodec {

    @Override
    protected byte[] getIdSignaturePassword() {
      return "42".getBytes(StandardCharsets.UTF_8);
    }
  }
}
