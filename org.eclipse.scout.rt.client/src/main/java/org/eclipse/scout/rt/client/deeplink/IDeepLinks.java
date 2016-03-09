package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.0
 */
@ApplicationScoped
public interface IDeepLinks {

  String DEEP_LINK_PREFIX = "view";

  Pattern DEEP_LINK_REGEX = Pattern.compile("^/" + DEEP_LINK_PREFIX + "/(.*)$");

  /**
   * @return True if the given path is a valid deep-link request (only syntax is checked at this point).
   * @path The path-info of the HTTP request (URL without protocol, host, port and context-path)
   */
  boolean isRequestValid(String path);

  /**
   * Handles the deep-link request.
   *
   * @path The path-info of the HTTP request (URL without protocol, host, port and context-path)
   * @return whether or not a handler has handled the request
   * @throws IllegalArgumentException
   *           when path is not a valid deep-link (check with isRequestValid() before you call this method)
   * @throws DeepLinkException
   *           when the deep-link couldn't be processed for some reasons (e.g. missing permissions)
   */
  boolean handleRequest(String path) throws DeepLinkException;

}
