package org.eclipse.scout.rt.client.deeplink;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.0
 */
@ApplicationScoped
public interface IDeepLinks {

  /**
   * @return True if the given path is a valid deep-link path. Only syntax is checked at this point.
   * @param deepLinkPath
   *          The deep-link path in the format <code>[handler name]-[handler data]</code>.
   */
  boolean canHandleDeepLink(String deepLinkPath);

  /**
   * Handles the deep-link by delegating to a handler that can process the given path.
   *
   * @param deepLinkPath
   *          The deep-link path in the format <code>[handler name]-[handler data]</code>.
   * @return whether or not a handler has handled the request
   * @throws IllegalArgumentException
   *           when path is not a valid deep-link (check with canHandleDeepLink() before you call this method)
   * @throws DeepLinkException
   *           when the deep-link couldn't be processed for some reasons (e.g. missing permissions)
   */
  boolean handleDeepLink(String deepLinkPath) throws DeepLinkException;

}
