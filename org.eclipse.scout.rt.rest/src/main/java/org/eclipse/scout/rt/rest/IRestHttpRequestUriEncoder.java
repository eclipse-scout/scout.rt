/*******************************************************************************
 * Copyright (c) 2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public interface IRestHttpRequestUriEncoder {

  /**
   * Encode URI in HTTP request line before it's sent over the socket.
   *
   * @param uri
   *          Default encoded request URI (RFC 3986), not {@code null}
   * @return (potentially) modified request URI to be sent over the socket as part of the HTTP header, not {@code null}
   */
  String encodeRequestUri(String uri);
}
