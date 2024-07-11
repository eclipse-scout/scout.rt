/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.opentelemetry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.opentelemetry.context.propagation.TextMapGetter;

/**
 * Test class for {@link HttpServletRequestTextMapGetter}
 */
@RunWith(PlatformTestRunner.class)
public class HttpServletRequestTextMapGetterTest {

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
    TextMapGetter<HttpServletRequest> getter = BEANS.get(HttpServletRequestTextMapGetter.class);
    Iterable<String> iterableHeaderKeys = getter.keys(mockRequest);

    // Assert
    assertTrue(iterableHeaderKeys.iterator().hasNext());
    assertEquals(headerValue, getter.get(mockRequest, headerKey));
  }
}
