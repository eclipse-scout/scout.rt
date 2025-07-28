/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

import jakarta.ws.rs.core.HttpHeaders;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Helper for checking whether requests are originating from ScoutJS (i.e. proxied REST call).
 *
 * <br><b>Example:</b> The following example GET resource is responded with HTTP status code
 * 403 if it was proxied.
 * <pre>
 * &#064;GET
 * &#064;Path("info")
 * public String info(@Context HttpHeaders headers) {
 *   BEANS.get(ProxiedRestRequestHelper.class).throwForbiddenIfProxied(headers);
 *   return "info";
 * }
 * </pre>
 *
 * <br><b>Note:</b> this class is considered beta and will be validated again in the context of ticket #419045.
 *
 * @see RestHttpHeaders#PROXIED_REQUEST_HTTP_HEADER
 */
@ApplicationScoped
public class ProxiedRestRequestHelper {

  /**
   * @return {@code true} in case of a proxied ScoutJS request. Otherwise, {@code false}.
   */
  public boolean isProxied(HttpHeaders headers) {
    return Boolean.TRUE.toString().equalsIgnoreCase(headers.getHeaderString(RestHttpHeaders.PROXIED_REQUEST_HTTP_HEADER));
  }

  /**
   * @throws AccessForbiddenException
   *     in case of a proxied ScoutJS request.
   */
  public void throwForbiddenIfProxied(HttpHeaders headers) {
    if (isProxied(headers)) {
      throw new AccessForbiddenException();
    }
  }
}
