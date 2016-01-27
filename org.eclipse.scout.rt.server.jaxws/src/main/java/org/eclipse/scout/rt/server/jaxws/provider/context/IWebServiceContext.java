package org.eclipse.scout.rt.server.jaxws.provider.context;

import javax.xml.ws.WebServiceContext;

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
