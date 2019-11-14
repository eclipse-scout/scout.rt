/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.jersey.client;

import static org.eclipse.scout.rt.rest.jersey.EchoServletParameters.STATUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.UnaryOperator;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.RestClientTestEchoResponse;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.glassfish.jersey.apache.connector.RestEnsureHttpHeaderConnectionCloseProperty;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ApacheHttpClientConnectionHeaderTest {

  private static final String CON_DIRECTIVE = "Connection";
  private static final String CON_CLOSE = "close";
  private static final String CON_KEEP_ALIVE = "keep-alive";

  private WebTarget m_target;
  private JerseyTestRestClientHelper m_helper;

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
    BEANS.get(RestEnsureHttpHeaderConnectionCloseProperty.class).setValue(Boolean.TRUE);
  }

  @AfterClass
  public static void afterClass() {
    BEANS.get(RestEnsureHttpHeaderConnectionCloseProperty.class).invalidate();
  }

  @Before
  public void before() {
    m_helper = BEANS.get(JerseyTestRestClientHelper.class);
    m_target = m_helper.target("echo");
  }

  @Test
  public void testDefaultRequest() throws Exception {
    assertTrue(CONFIG.getPropertyValue(RestEnsureHttpHeaderConnectionCloseProperty.class).booleanValue());
    assertSyncAsyncHttpConnectionHeader(b -> b, CON_CLOSE);
  }

  @Test
  public void testDefaultWithDisabledConfigPropertyRequest() throws Exception {
    RestEnsureHttpHeaderConnectionCloseProperty property = BEANS.get(RestEnsureHttpHeaderConnectionCloseProperty.class);
    property.setValue(Boolean.FALSE);
    try {
      assertFalse(CONFIG.getPropertyValue(RestEnsureHttpHeaderConnectionCloseProperty.class).booleanValue());
      assertSyncAsyncHttpConnectionHeader(b -> b, CON_KEEP_ALIVE);
    }
    finally {
      property.setValue(Boolean.TRUE);
      assertTrue(CONFIG.getPropertyValue(RestEnsureHttpHeaderConnectionCloseProperty.class).booleanValue());
    }
  }

  @Test
  public void testExplicitCloseRequest() throws Exception {
    assertSyncAsyncHttpConnectionHeader(b -> b.header(CON_DIRECTIVE, CON_CLOSE), CON_CLOSE);
  }

  @Test
  public void testKeepAliveRequest() throws Exception {
    assertSyncAsyncHttpConnectionHeader(b -> b.header(CON_DIRECTIVE, CON_KEEP_ALIVE), CON_KEEP_ALIVE);
  }

  protected void assertSyncAsyncHttpConnectionHeader(UnaryOperator<Builder> builderConsumer, String expectedConnectionHeaderValue) {
    assertHttpConnectionHeader(true, builderConsumer, expectedConnectionHeaderValue);
    assertHttpConnectionHeader(false, builderConsumer, expectedConnectionHeaderValue);
  }

  protected void assertHttpConnectionHeader(boolean   syncInvocation, UnaryOperator<Builder> builderCustomizer, String expectedConnectionHeaderValue) {
    RunContexts.copyCurrent()
        .run(() -> {
          // invoke service
          Response response;
          WebTarget target = m_target.queryParam(STATUS, Response.Status.OK.getStatusCode());
          Builder builder = target
              .request()
              .accept(MediaType.APPLICATION_JSON);
          builder = builderCustomizer.apply(builder);

          if (syncInvocation) {
            response = builder.get();
          }
          else {
            response = builder.async().get().get();
          }

          // check expectations
          assertNotNull(response);
          RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
          assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), entity.getEcho().getCode());
          Map<String, String> receivedHeaders = entity.getReceivedHeaders();
          assertNotNull(receivedHeaders);
          assertTrue(receivedHeaders.containsKey(CON_DIRECTIVE));
          assertEquals(expectedConnectionHeaderValue, receivedHeaders.get(CON_DIRECTIVE));
        });
  }
}
