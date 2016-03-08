package org.eclipse.scout.rt.client.deeplink;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.0
 */
@ApplicationScoped
public interface IDeepLinks {

  /**
   * @return True if the given path is a valid deep-link request (only syntax is checked at this point).
   * @path The path-info of the HTTP request (URL without protocol, host, port and context-path)
   */
  boolean isRequestValid(String path);

  /**
   * Handles the deep-link request.
   *
   * @path The path-info of the HTTP request (URL without protocol, host, port and context-path)
   */
  boolean handleRequest(String path);

}
