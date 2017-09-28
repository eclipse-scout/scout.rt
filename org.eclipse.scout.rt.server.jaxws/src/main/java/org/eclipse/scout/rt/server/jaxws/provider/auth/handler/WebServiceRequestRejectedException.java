/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.auth.handler;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPException;

/**
 * Exception to be thrown, if the call chain should be exit with a {@link HTTPException}.
 * <p>
 * This is used because some JAX-WS implementors (like METRO v2.2.10) do not exit the call chain if the {@link Handler}
 * returns with <code>false</code>. That happens for one-way communication requests. As a result, the endpoint operation
 * is still invoked.
 *
 * @since 5.2
 */
public class WebServiceRequestRejectedException extends Exception {

  private static final long serialVersionUID = 1L;

  private final int m_httpStatusCode;

  /**
   * @param httpStatusCode
   *          HTTP status code to be set in {@link HTTPException}.
   */
  public WebServiceRequestRejectedException(final int httpStatusCode) {
    m_httpStatusCode = httpStatusCode;
  }

  public int getHttpStatusCode() {
    return m_httpStatusCode;
  }
}
