/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client.multipart;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.uuid.IUuidProvider;
import org.eclipse.scout.rt.rest.client.multipart.MultipartMessage;
import org.eclipse.scout.rt.rest.client.multipart.MultipartPart;
import org.eclipse.scout.rt.rest.jersey.EchoServletParameters;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.RestClientTestEchoResponse;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.uuid.FixedUuidProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link MultipartMessageBodyWriter} via {@link JerseyTestApplication}.
 */
@RunWith(PlatformTestRunner.class)
public class MultipartRestClientTest {

  private static IBean<?> s_uuidProvider;

  /**
   * Used in .txt resource serving as expected echo response.
   */
  private static final String FIXED_UUID = "3372ccc6-dada-4847-b189-3ff57a81e553";

  @BeforeClass
  public static void beforeClass() {
    // Use a fixed uuid provider to create a known boundary for the multipart message.
    s_uuidProvider = BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(IUuidProvider.class, new FixedUuidProvider(UUID.fromString(FIXED_UUID))));

    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(BeanTestingHelper.class).unregisterBean(s_uuidProvider);
  }

  private WebTarget m_target;

  @Before
  public void before() {
    m_target = BEANS.get(JerseyTestRestClientHelper.class).target("echo");
  }

  @Test
  public void testMultipart() {
    byte[] plainTextBytes = "lorem ipsum dolor\nsit amet".getBytes(StandardCharsets.UTF_8);
    byte[] jsonBytes = "{ \"lorem\": \"ipsum\"}".getBytes(StandardCharsets.UTF_8);
    byte[] umlauteBytes = "Äpfel".getBytes(StandardCharsets.UTF_8);

    MultipartMessage multiPartMessage = BEANS.get(MultipartMessage.class)
        .addPart(MultipartPart.ofFile("plaintext", "text.txt", new ByteArrayInputStream(plainTextBytes)))
        .addPart(MultipartPart.ofFile("json", "json.json", new ByteArrayInputStream(jsonBytes)))
        .addPart(MultipartPart.ofFile("umlaute-öäü", "äpfel\uD83D\uDE00.txt", new ByteArrayInputStream(umlauteBytes)))
        .addPart(MultipartPart.ofField("lorem", "lorem value"))
        .addPart(MultipartPart.ofField("ipsum", "ipsum välüe"))
        .addPart(MultipartPart.ofField("dol\"or", "dolor\"value"));

    RestClientTestEchoResponse response = m_target
        .queryParam(EchoServletParameters.STATUS, Response.Status.OK.getStatusCode())
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .post(multiPartMessage.toEntity(), RestClientTestEchoResponse.class);

    String contentTypeHeader = response.getReceivedHeaders().get("Content-Type");
    assertEquals("multipart/form-data;boundary=" + FIXED_UUID.replace("-", ""), contentTypeHeader);

    // file contains \n only, replace by \r\n as the HTTP line delimiter is using \r\n too (except the plain text bytes)
    String expectedEchoResponse = IOUtility.readStringUTF8(MultipartRestClientTest.class.getResourceAsStream("MultipartEchoResponse.txt"))
        .replaceAll("\n", "\r\n")
        .replaceFirst("lorem ipsum dolor\r\nsit amet", "lorem ipsum dolor\nsit amet");

    assertEquals(expectedEchoResponse, response.getEcho().getBody());
  }
}
