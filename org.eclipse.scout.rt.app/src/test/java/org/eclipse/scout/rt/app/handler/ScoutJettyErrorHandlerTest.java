/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app.handler;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertComparableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpFields.Mutable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.QuotedQualityCSV;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.Components;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Test for {@link ScoutJettyErrorHandler}
 */
public class ScoutJettyErrorHandlerTest {

  @Test
  public void testError_textPlain() throws Exception {
    String expectedMesssage = """
        HTTP ERROR 400
        STATUS: 400
        MESSAGE: Bad Request""";

    runTestError("text/plain", expectedMesssage, "text/plain;charset=utf-8");
  }

  @Test
  public void testError_applicationJson() throws Exception {
    String expectedMesssage = """
        {"status":"400","message":"Bad Request"}""";

    runTestError("application/json", expectedMesssage, "application/json");
    runTestError("text/json", expectedMesssage, "text/json");
  }

  @Test
  public void testError_textHtml() throws Exception {
    String expectedMesssage = """
        <html>
        <head>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
        <title>Error 400</title>
        </head>
        <body>
        <h2>HTTP ERROR 400 Bad Request</h2>
        <table>
        <tr><th>URI:</th><td>n/a</td></tr>
        <tr><th>STATUS:</th><td>400</td></tr>
        <tr><th>MESSAGE:</th><td>Bad Request</td></tr>
        </table>

        </body>
        </html>""";

    runTestError("text/html", expectedMesssage, "text/html;charset=utf-8");
    runTestError("text/*", expectedMesssage, "text/html;charset=utf-8");
    runTestError("*/*", expectedMesssage, "text/html;charset=utf-8");
  }

  protected void runTestError(String acceptHeader, String expectedMessage, String expectedContentType) throws Exception {
    ScoutJettyErrorHandler errorHandler = BEANS.get(ScoutJettyErrorHandler.class);
    Request request = Mockito.mock(Request.class);
    Response response = Mockito.mock(Response.class);
    Callback callback = Mockito.mock(Callback.class);
    Mutable responseHeaders = Mockito.mock(Mutable.class);
    HttpFields requestHeaders = Mockito.mock(HttpFields.class);
    ConnectionMetaData connectionMetaData = Mockito.mock(ConnectionMetaData.class);
    HttpConfiguration httpConfiguration = Mockito.mock(HttpConfiguration.class);
    Components components = Mockito.mock(Components.class);
    ByteBufferPool byteBufferPool = new ArrayByteBufferPool();
    HttpURI httpUri = Mockito.mock(HttpURI.class);

    when(request.getMethod()).thenReturn("GET");
    when(request.getAttribute(ErrorHandler.ERROR_MESSAGE)).thenReturn("Bad Request");
    when(request.getAttribute(ErrorHandler.ERROR_EXCEPTION)).thenReturn(new Throwable("DO NOT EXPOSE"));

    when(request.getHttpURI()).thenReturn(httpUri);
    when(httpUri.toString()).thenReturn("DO NOT EXPOSE");

    when(request.getHeaders()).thenReturn(requestHeaders);
    when(requestHeaders.getQualityCSV(HttpHeader.ACCEPT, QuotedQualityCSV.MOST_SPECIFIC_MIME_ORDERING)).thenReturn(List.of(acceptHeader));
    when(requestHeaders.getQualityCSV(HttpHeader.ACCEPT_CHARSET)).thenReturn(List.of(StandardCharsets.UTF_8.name()));

    when(request.getConnectionMetaData()).thenReturn(connectionMetaData);
    when(connectionMetaData.getHttpConfiguration()).thenReturn(httpConfiguration);
    when(httpConfiguration.getOutputBufferSize()).thenReturn(1000);

    when(request.getComponents()).thenReturn(components);
    when(components.getByteBufferPool()).thenReturn(byteBufferPool);

    when(response.getStatus()).thenReturn(400);
    when(response.getHeaders()).thenReturn(responseHeaders);

    Map<HttpHeader, HttpField> responseHeaderFields = new HashMap<>();
    when(responseHeaders.put(any(HttpField.class))).thenAnswer(i -> {
      HttpField field = i.getArgument(0, HttpField.class);
      responseHeaderFields.put(field.getHeader(), field);
      return responseHeaders;
    });

    errorHandler.handle(request, response, callback);

    ArgumentCaptor<ByteBuffer> byteBufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
    verify(response).write(any(Boolean.class), byteBufferCaptor.capture(), any(Callback.class));
    verify(response, atLeastOnce()).getHeaders();
    verify(response, atLeastOnce()).getStatus();

    String responsePayload = new String(byteBufferCaptor.getValue().array(), StandardCharsets.UTF_8);
    assertComparableEquals(expectedMessage, toUnixLineEndingsAndTrim(responsePayload));

    assertComparableEquals("must-revalidate,no-cache,no-store", responseHeaderFields.get(HttpHeader.CACHE_CONTROL).getValue());
    assertComparableEquals(expectedContentType, responseHeaderFields.get(HttpHeader.CONTENT_TYPE).getValue());

    verifyNoMoreInteractions(response);
  }

  protected String toUnixLineEndingsAndTrim(String s) {
    if (s == null) {
      return s;
    }
    return s.replaceAll("\\r\\n", "\n").trim();
  }
}
