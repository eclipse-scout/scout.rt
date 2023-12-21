/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.id;

import static java.util.Collections.*;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.hashSet;
import static org.eclipse.scout.rt.platform.util.StringUtility.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.server.commons.id.IdSignatureFilter.SignatureHttpServletRequestWrapper;
import org.junit.Test;

public class SignatureHttpServletRequestWrapperTest {

  @Test
  public void testRequestWithoutHeader() {
    HttpServletRequest req = createHttpServletRequest();

    assertNull(req.getHeader(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER));
    assertNull(req.getHeader(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));

    assertEquals(Set.of(), set(req.getHeaders(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));
    assertEquals(Set.of(), set(req.getHeaders(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER))));

    assertEquals(Set.of(), set(req.getHeaderNames()));

    HttpServletRequest wrapper = new SignatureHttpServletRequestWrapper(req);

    assertEquals(Boolean.TRUE.toString(), wrapper.getHeader(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER));
    assertEquals(Boolean.TRUE.toString(), wrapper.getHeader(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));

    assertEquals(Set.of(Boolean.TRUE.toString()), set(wrapper.getHeaders(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));
    assertEquals(Set.of(Boolean.TRUE.toString()), set(wrapper.getHeaders(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER))));

    assertEquals(Set.of(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER), set(wrapper.getHeaderNames()));
  }

  @Test
  public void testRequestWithSignatureHeader() {
    HttpServletRequest req = createHttpServletRequest(Map.of(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER, Boolean.FALSE.toString()));

    assertEquals(Boolean.FALSE.toString(), req.getHeader(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER));
    assertEquals(Boolean.FALSE.toString(), req.getHeader(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));

    assertEquals(Set.of(Boolean.FALSE.toString()), set(req.getHeaders(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));
    assertEquals(Set.of(Boolean.FALSE.toString()), set(req.getHeaders(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER))));

    assertEquals(Set.of(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER), set(req.getHeaderNames()));

    HttpServletRequest wrapper = new SignatureHttpServletRequestWrapper(req);

    assertEquals(Boolean.TRUE.toString(), wrapper.getHeader(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER));
    assertEquals(Boolean.TRUE.toString(), wrapper.getHeader(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));

    assertEquals(Set.of(Boolean.FALSE.toString(), Boolean.TRUE.toString()), set(wrapper.getHeaders(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));
    assertEquals(Set.of(Boolean.FALSE.toString(), Boolean.TRUE.toString()), set(wrapper.getHeaders(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER))));

    assertEquals(Set.of(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER), hashSet(set(wrapper.getHeaderNames())));
  }

  @Test
  public void testRequestWithDummyHeader() {
    HttpServletRequest req = createHttpServletRequest(Map.of("Dummy", "42"));

    assertEquals("42", req.getHeader("Dummy"));
    assertEquals("42", req.getHeader("dummy"));

    assertEquals(Set.of("42"), set(req.getHeaders("Dummy")));
    assertEquals(Set.of("42"), set(req.getHeaders("dummy")));

    assertNull(req.getHeader(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER));
    assertNull(req.getHeader(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));

    assertEquals(Set.of(), set(req.getHeaders(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));
    assertEquals(Set.of(), set(req.getHeaders(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER))));

    assertEquals(Set.of("Dummy"), set(req.getHeaderNames()));

    HttpServletRequest wrapper = new SignatureHttpServletRequestWrapper(req);

    assertEquals("42", wrapper.getHeader("Dummy"));
    assertEquals("42", wrapper.getHeader("dummy"));

    assertEquals(Set.of("42"), set(wrapper.getHeaders("Dummy")));
    assertEquals(Set.of("42"), set(wrapper.getHeaders("dummy")));

    assertEquals(Boolean.TRUE.toString(), wrapper.getHeader(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER));
    assertEquals(Boolean.TRUE.toString(), wrapper.getHeader(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));

    assertEquals(Set.of(Boolean.TRUE.toString()), set(wrapper.getHeaders(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER)));
    assertEquals(Set.of(Boolean.TRUE.toString()), set(wrapper.getHeaders(lowercase(IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER))));

    assertEquals(Set.of("Dummy", IdSignatureFilter.ID_SIGNATURE_HTTP_HEADER), set(wrapper.getHeaderNames()));
  }

  protected static HttpServletRequest createHttpServletRequest() {
    return createHttpServletRequest(Map.of());
  }

  protected static HttpServletRequest createHttpServletRequest(Map<String, String> headers) {
    HttpServletRequest mock = mock(HttpServletRequest.class);
    when(mock.getHeader(any())).thenAnswer(invocation -> headers.entrySet().stream()
        .filter(entry -> equalsIgnoreCase((String) invocation.getArguments()[0], entry.getKey()))
        .map(Entry::getValue)
        .findFirst()
        .orElse(null));
    when(mock.getHeaders(any())).thenAnswer(invocation -> enumeration(headers.entrySet().stream()
        .filter(entry -> equalsIgnoreCase((String) invocation.getArguments()[0], entry.getKey()))
        .map(Entry::getValue)
        .collect(Collectors.toSet())));
    when(mock.getHeaderNames()).thenAnswer(invocation -> enumeration(headers.keySet()));
    return mock;
  }

  protected static <T> Set<T> set(Enumeration<T> enumeration) {
    return hashSet(list(enumeration));
  }
}
