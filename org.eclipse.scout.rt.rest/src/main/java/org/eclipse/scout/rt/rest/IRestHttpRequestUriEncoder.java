/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public interface IRestHttpRequestUriEncoder {

  /**
   * Encode URI in HTTP request line before it's sent over the socket.
   *
   * @param uri
   *     Default encoded request URI (RFC 3986), not {@code null}
   * @return (potentially) modified request URI to be sent over the socket as part of the HTTP header, not {@code null}
   */
  String encodeRequestUri(String uri);
}
