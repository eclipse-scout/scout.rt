/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.context;

import jakarta.xml.ws.WebServiceContext;

/**
 * Interface to hold information about an ongoing webservice request.
 *
 * @since 5.2
 */
public interface IWebServiceContext {

  /**
   * The JAX-WS {@link WebServiceContext} which is currently associated with the current thread.
   */
  ThreadLocal<WebServiceContext> CURRENT = new ThreadLocal<>();
}
