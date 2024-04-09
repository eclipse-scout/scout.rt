/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.traces;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.opentelemetry.IContextPropagationHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

@RunWith(PlatformTestRunner.class)
public class ContextPropagationHelperTest {

  @Test
  public void testCreateHttpRequestTextMapSetter() throws IOException {
    // Arrange
    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
    HttpRequest request = requestFactory.buildGetRequest(new GenericUrl("http://example.com"));
    String key = "testKey";
    String value = "testValue";

    // Act
    TextMapSetter<HttpRequest> mapSetter = BEANS.get(IContextPropagationHelper.class).createHttpRequestTextMapSetter();
    mapSetter.set(request, key, value);

    // Assert
    assertEquals(value, request.getHeaders().get(key));
  }

  @Test
  public void testCreateServletRequestTextMapGetter() {
    // Arrange
    String headerKey = "testKey";
    String headerValue = "testValue";
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    Enumeration<String> headerKeys = Collections.enumeration(List.of(headerKey));
    when(mockRequest.getHeaderNames()).thenReturn(headerKeys);
    when(mockRequest.getHeader(headerKey)).thenReturn(headerValue);

    // Act
    TextMapGetter<HttpServletRequest> getter = BEANS.get(IContextPropagationHelper.class).createServletRequestTextMapGetter();
    Iterable<String> iterableHeaderKeys = getter.keys(mockRequest);

    // Assert
    assertTrue(iterableHeaderKeys.iterator().hasNext());
    assertEquals(headerValue, getter.get(mockRequest, headerKey));
  }
}
