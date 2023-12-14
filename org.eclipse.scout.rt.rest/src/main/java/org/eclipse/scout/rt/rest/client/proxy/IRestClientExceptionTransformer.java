/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.proxy;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Strategy used by proxied REST clients for transforming technical REST exceptions ({@link WebApplicationException} and
 * {@link jakarta.ws.rs.ProcessingException}) into the current application context.
 */
@FunctionalInterface
public interface IRestClientExceptionTransformer {

  IRestClientExceptionTransformer IDENTITY = (e, r) -> e;

  static IRestClientExceptionTransformer identityIfNull(IRestClientExceptionTransformer t) {
    return t == null ? IDENTITY : t;
  }

  /**
   * Transforms the given {@link RuntimeException} and optional {@link Response}.
   *
   * @param e
   *          {@link WebApplicationException} or {@link jakarta.ws.rs.ProcessingException} caught during REST service
   *          invocation.
   * @param response
   *          optional response extracted from the given exception. <b>Note:</b> Could be null.
   */
  RuntimeException transform(RuntimeException e, Response response);
}
