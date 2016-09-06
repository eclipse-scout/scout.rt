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
