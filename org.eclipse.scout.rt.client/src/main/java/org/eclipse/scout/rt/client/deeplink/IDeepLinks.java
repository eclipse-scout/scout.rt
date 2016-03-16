package org.eclipse.scout.rt.client.deeplink;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.0
 */
@ApplicationScoped
public interface IDeepLinks {

  /**
   * Name of the URL parameter which contains the deep-link in the format <code>[handler name]-[handler data]</code>.
   */
  String PARAM_NAME_DEEP_LINK = "deeplink";

  /**
   * Name of the optional URL parameter which contains a human readable, informative text about the deep-link.
   */
  String PARAM_NAME_INFO = "info";

  /**
   * @return True if the given path is a valid deep-link request (only syntax is checked at this point).
   * @path The path-info of the HTTP request (URL without protocol, host, port and context-path)
   */
  boolean canHandleDeepLink(String deepLinkPath);

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
  boolean handleDeepLink(String deepLinkPath) throws DeepLinkException;

  /**
   * @param webRoot
   *          including protocol, host, port and context-path (without trailing slash). Example:
   *          http://scout.eclipse.org:8080/widgets
   */
  void setWebRoot(String webRoot);

  String getWebRoot();

}
