/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client.proxy;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Strategy used by proxied REST clients for transforming technical REST exceptions, i.e.
 * {@link WebApplicationException} and {@link javax.ws.rs.ProcessingException}, into the current application context.
 */
@FunctionalInterface
public interface IRestClientExceptionTransformer {

  IRestClientExceptionTransformer IDENTITY = (e, r) -> {
    throw e;
  };

  static IRestClientExceptionTransformer identityIfNull(IRestClientExceptionTransformer t) {
    return t == null ? IDENTITY : t;
  }

  /**
   * Transforms the given {@link RuntimeException} and optional {@link Response}.
   *
   * @param e
   *          {@link WebApplicationException} or {@link javax.ws.rs.ProcessingException} caught during REST service
   *          invocation.
   * @param response
   *          optional response extracted from the given exception. <b>Note:</b> Could be null.
   */
  RuntimeException transform(RuntimeException e, Response response);
}
