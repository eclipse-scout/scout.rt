/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client;

import static org.eclipse.scout.rt.rest.jersey.EchoServletParameters.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.client.config.CookieSpecs;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.rest.client.RestClientProperties.LoggerVerbosity;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.rest.jersey.ProxyServletParameters;
import org.eclipse.scout.rt.rest.jersey.RestClientHttpProxyServlet;
import org.eclipse.scout.rt.rest.jersey.RestClientTestEchoResponse;
import org.eclipse.scout.rt.rest.jersey.RestClientTestEchoServlet;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ScoutApacheConnectorTest {

  protected static final String MY_COOKIE_VALUE = "my-cookie";
  protected static final String SET_COOKIE_HEADER = "Set-Cookie";
  protected static final String COOKIE_HEADER = "Cookie";

  protected static final String MOCK_PROXY_USER = "user";
  protected static final String MOCK_PROXY_PASSWORD = "pass";

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  @Test
  public void testCookiesEnabled_NoCookieSpec() {
    runTestCookie((helper, clientBuilder) -> {
      // enable cookies without cookie spec
      clientBuilder.property(RestClientProperties.ENABLE_COOKIES, true);
    }, true);
  }

  @Test
  public void testCookiesEnabled_DefaultCookieSpec() {
    runTestCookie((helper, clientBuilder) -> {
      clientBuilder.property(RestClientProperties.ENABLE_COOKIES, true);
      clientBuilder.property(RestClientProperties.COOKIE_SPEC, CookieSpecs.DEFAULT);
    }, true);
  }

  @Test
  public void testCookiesEnabled_IgnoreCookiesSpec() {
    runTestCookie((helper, clientBuilder) -> {
      clientBuilder.property(RestClientProperties.ENABLE_COOKIES, true);
      // ignore cookies overrides enable-cookie property
      clientBuilder.property(RestClientProperties.COOKIE_SPEC, CookieSpecs.IGNORE_COOKIES);
    }, false);
  }

  @Test
  public void testCookiesDisabled() {
    runTestCookie((helper, clientBuilder) -> {
      // disable cookies completely
      clientBuilder.property(RestClientProperties.ENABLE_COOKIES, false);
    }, false);
  }

  protected void runTestCookie(BiConsumer<JerseyTestRestClientHelper, ClientBuilder> clientBuilderCustomizer, boolean expectCookie) {
    WebTarget m_target = newHelper(clientBuilderCustomizer).target("echo");
    RunContexts.copyCurrent()
        .run(() -> {
          // invoke service and force to set a cookie on response
          Response response;
          WebTarget target = m_target
              .queryParam(STATUS, Response.Status.OK.getStatusCode())
              .queryParam(COOKIE_VALUE, MY_COOKIE_VALUE);
          Builder builder = target
              .request()
              .accept(MediaType.APPLICATION_JSON);
          response = builder.get();

          // check expectations
          assertNotNull(response);
          RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
          assertEquals(RestClientTestEchoServlet.ECHO_SERVLET_COOKIE + "=" + MY_COOKIE_VALUE, response.getHeaderString(SET_COOKIE_HEADER));
          response.close();
          response = null;
          assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), entity.getEcho().getCode());

          // second call to verify cookie
          target = m_target.queryParam(STATUS, Response.Status.OK.getStatusCode());
          builder = target
              .request()
              .accept(MediaType.APPLICATION_JSON);
          response = builder.get();

          // check expectations
          assertNotNull(response);
          entity = response.readEntity(RestClientTestEchoResponse.class);
          assertNull(response.getHeaderString(SET_COOKIE_HEADER));
          response.close();
          response = null;
          assertEquals(Integer.valueOf(Response.Status.OK.getStatusCode()), entity.getEcho().getCode());
          Map<String, String> receivedHeaders = entity.getReceivedHeaders();
          assertNotNull(receivedHeaders);
          if (expectCookie) {
            assertTrue(receivedHeaders.containsKey(COOKIE_HEADER));
            assertEquals(RestClientTestEchoServlet.ECHO_SERVLET_COOKIE + "=" + MY_COOKIE_VALUE, receivedHeaders.get(COOKIE_HEADER));
          }
          else {
            assertFalse(receivedHeaders.containsKey(COOKIE_HEADER));
          }
        });
  }

  @Test
  public void testRedirect_followRedirects() {
    runTestRedirect((helper, clientBuilder) -> {
      // follow redirects explicitely (parameter is true as default)
      clientBuilder.property(RestClientProperties.FOLLOW_REDIRECTS, true);
    }, false);
  }

  @Test
  public void testRedirect_dontFollowRedirects() {
    runTestRedirect((helper, clientBuilder) -> {
      // do not follow redirects at all
      clientBuilder.property(RestClientProperties.FOLLOW_REDIRECTS, false);
    }, true);
  }

  @Test
  public void testRedirect_defaultDontFollowRedirects() {
    // parameter RestClientProperties.FOLLOW_REDIRECTS is true per default
    runTestRedirect((helper, clientBuilder) -> {}, false);
  }

  protected void runTestRedirect(BiConsumer<JerseyTestRestClientHelper, ClientBuilder> clientBuilderCustomizer, boolean expectRedirectionException) {
    WebTarget m_target = newHelper(clientBuilderCustomizer).target("echo");
    RunContexts.copyCurrent()
        .run(() -> {
          try {
            // invoke service and force to set a cookie on response
            Response response;
            WebTarget target = m_target
                .queryParam(STATUS, Status.OK.getStatusCode())
                .queryParam(REDIRECT_URL, "/echo?" + STATUS + "=" + Status.CREATED.getStatusCode());
            Builder builder = target
                .request()
                .accept(MediaType.APPLICATION_JSON);
            response = builder.get();

            // check expectations
            assertNotNull(response);
            RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
            assertEquals(Integer.valueOf(Status.CREATED.getStatusCode()), entity.getEcho().getCode());
            assertFalse(expectRedirectionException);
            response.close();
            response = null;
          }
          catch (ProcessingException e) {
            assertTrue(expectRedirectionException);
            assertEquals(RedirectionException.class, e.getCause().getClass());
          }
        });
  }

  @Test
  public void testProxyActive_String() {
    runTestProxy((helper, clientBuilder) -> {
      // use proxy URI-string without user/password
      clientBuilder.property(RestClientProperties.PROXY_URI, "http://localhost:" + BEANS.get(JerseyTestApplication.class).getProxyPort());
    }, false, true);
  }

  @Test
  public void testProxyActive_URI() {
    runTestProxy((helper, clientBuilder) -> {
      // use proxy URI without user/password
      clientBuilder.property(RestClientProperties.PROXY_URI, URI.create("http://localhost:" + BEANS.get(JerseyTestApplication.class).getProxyPort()));
    }, false, true);
  }

  @Test
  public void testProxyActiveAuthenticate() {
    runTestProxy((helper, clientBuilder) -> {
      clientBuilder.property(RestClientProperties.PROXY_URI, "http://localhost:" + BEANS.get(JerseyTestApplication.class).getProxyPort());
      clientBuilder.property(RestClientProperties.PROXY_USERNAME, MOCK_PROXY_USER);
      clientBuilder.property(RestClientProperties.PROXY_PASSWORD, MOCK_PROXY_PASSWORD);
    }, true, true);
  }

  @Test
  public void testProxyInactive() {
    runTestProxy((helper, clientBuilder) -> {
      // null-proxy (e.g. no proxy)
      clientBuilder.property(RestClientProperties.PROXY_URI, null);
    }, false, false);
  }

  @Test
  public void testNoProxy() {
    runTestProxy((helper, clientBuilder) -> {}, false, false);
  }

  protected void runTestProxy(BiConsumer<JerseyTestRestClientHelper, ClientBuilder> clientBuilderCustomizer, boolean requireAuth, boolean expectProxyOK) {
    WebTarget m_target = newHelper(clientBuilderCustomizer).target("echo");
    final String CID = UUID.randomUUID().toString();
    RunContexts.copyCurrent()
        .withCorrelationId(CID)
        .run(() -> {
          Response response;
          WebTarget target = m_target.queryParam(STATUS, Status.OK.getStatusCode());
          if (requireAuth) {
            target = target
                .queryParam(ProxyServletParameters.REQUIRE_AUTH, "true")
                .queryParam(ProxyServletParameters.PROXY_USER, MOCK_PROXY_USER)
                .queryParam(ProxyServletParameters.PROXY_PASSWORD, MOCK_PROXY_PASSWORD);
          }
          Builder builder = target
              .request()
              .accept(MediaType.APPLICATION_JSON);
          response = builder.get();

          // check expectations
          assertNotNull(response);
          assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
          assertEquals(CID, response.getHeaderString(CorrelationId.HTTP_HEADER_NAME));

          RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
          if (expectProxyOK) {
            assertEquals(RestClientHttpProxyServlet.PROXY_OK_RESPONSE, entity.getEcho().getData());
          }
          else {
            assertEquals(Response.Status.OK.getStatusCode(), entity.getEcho().getCode().intValue());
          }
          response.close();
          response = null;
        });
  }

  @Test
  public void testRequestWithBody() {
    runTestRequestWithBody((helper, clientBuilder) -> {}, "chunked");
  }

  @Test
  public void testRequestWithBodyChunkedEnabled() {
    runTestRequestWithBody((helper, clientBuilder) -> {
      // enable chunked transfer (e.g. buffering in memory is disabled)
      clientBuilder.property(RestClientProperties.DISABLE_CHUNKED_TRANSFER_ENCODING, false);
    }, "chunked");
  }

  @Test
  public void testRequestWithBodyChunkedDisabled() {
    runTestRequestWithBody((helper, clientBuilder) -> {
      // disable chunked transfer (e.g. buffering in memory is enabled)
      clientBuilder.property(RestClientProperties.DISABLE_CHUNKED_TRANSFER_ENCODING, true);
    }, null);
  }

  protected void runTestRequestWithBody(BiConsumer<JerseyTestRestClientHelper, ClientBuilder> clientBuilderCustomizer, String expectedTransferEncoding) {
    WebTarget target = newHelper(clientBuilderCustomizer).target("echo")
        .queryParam(STATUS, Status.OK.getStatusCode());

    Builder builder = target
        .request()
        .accept(MediaType.APPLICATION_JSON);

    String largeIntString = IntStream.range(1, 1000).mapToObj(Integer::toString).collect(Collectors.joining("#"));
    IDoEntity payload = BEANS.get(DoEntityBuilder.class)
        .put("attribute", "value")
        .put("attributeLarge", largeIntString)
        .build();
    Response response = builder.post(Entity.json(payload));
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
    assertEquals(Response.Status.OK.getStatusCode(), entity.getEcho().getCode().intValue());
    assertEquals(expectedTransferEncoding, entity.getReceivedHeaders().get("Transfer-Encoding"));
    assertEquals(payload, BEANS.get(IDataObjectMapper.class).readValueRaw(entity.getEcho().getBody()));

    response.close();
  }

  @Test
  public void testDefaultUserAgent() {
    runTestUserAgent((helper, clientBuilder) -> { /* nop */ }, null, "Generic");
  }

  @Test
  public void testNoUserAgent() {
    runTestUserAgent((helper, clientBuilder) -> {
      // disable chunked transfer (e.g. buffering in memory is enabled)
      clientBuilder.property(RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT, true);
    }, null, null);
  }

  @Test
  public void testCustomUserAgent() {
    runTestUserAgent((helper, clientBuilder) -> {},
        builder -> builder.header(HttpHeaders.USER_AGENT, "mockAgent"),
        "mockAgent");
  }

  @Test
  public void testCustomUserAgentEmpty() {
    runTestUserAgent((helper, clientBuilder) -> {},
        builder -> builder.header(HttpHeaders.USER_AGENT, ""),
        "");
  }

  @Test
  public void testCustomUserAgentNull() {
    runTestUserAgent((helper, clientBuilder) -> {},
        builder -> builder.header(HttpHeaders.USER_AGENT, null),
        "Generic");
  }

  @Test
  public void testCustomUserAgentNullDefaultAgentSuppressed() {
    //noinspection CodeBlock2Expr
    runTestUserAgent((helper, clientBuilder) -> {
      clientBuilder.property(RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT, true);
    },
        builder -> builder.header(HttpHeaders.USER_AGENT, null),
        null);
  }

  @Test
  public void testCustomUserAgentDefaultAgentSuppressed() {
    //noinspection CodeBlock2Expr
    runTestUserAgent((helper, clientBuilder) -> {
      clientBuilder.property(RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT, true);
    },
        builder -> builder.header(HttpHeaders.USER_AGENT, "mockAgent"),
        "mockAgent");
  }

  @Test
  public void testUserAgentDefaultAgentSuppressedOnRequest() {
    runTestUserAgent((helper, clientBuilder) -> {},
        builder -> builder.property(RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT, true),
        null);
  }

  protected void runTestUserAgent(BiConsumer<JerseyTestRestClientHelper, ClientBuilder> clientBuilderCustomizer, Function<Builder, Builder> invocationBuilderCustomizer, String expectedUserAgent) {
    WebTarget target = newHelper(clientBuilderCustomizer).target("echo")
        .queryParam(STATUS, Status.OK.getStatusCode());

    Builder builder = target
        .request();
    if (invocationBuilderCustomizer != null) {
      builder = invocationBuilderCustomizer.apply(builder);
    }
    builder.accept(MediaType.APPLICATION_JSON);
    Response response = builder.get();

    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    RestClientTestEchoResponse entity = response.readEntity(RestClientTestEchoResponse.class);
    assertEquals(Response.Status.OK.getStatusCode(), entity.getEcho().getCode().intValue());
    assertEquals(expectedUserAgent, entity.getReceivedHeaders().get(HttpHeaders.USER_AGENT));

    response.close();
  }

  protected JerseyTestRestClientHelper newHelper(BiConsumer<JerseyTestRestClientHelper, ClientBuilder> clientBuilderCustomizer) {
    return new JerseyTestRestClientHelper() {
      @Override
      protected void configureClientBuilder(ClientBuilder clientBuilder) {
        super.configureClientBuilder(clientBuilder);
        clientBuilderCustomizer.accept(this, clientBuilder);
        clientBuilder.property(RestClientProperties.LOGGING_LOGGER_NAME, ScoutApacheConnectorTest.class.getName());
        clientBuilder.property(RestClientProperties.LOGGING_LOGGER_LEVEL, Level.INFO);
        clientBuilder.property(RestClientProperties.LOGGING_LOGGER_VERBOSITY, LoggerVerbosity.PAYLOAD_ANY);
      }
    };
  }
}
