package org.eclipse.scout.rt.ui.html.deeplink;

import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.IClientSession;

/**
 * Interface for all classes that provide deep-link logic.
 */
public interface IDeepLinkHandler {

  /**
   * @return True if this handler can handle the given path, false otherwise
   */
  boolean matches(String path);

  /**
   * Executes the deep-link action on the model.
   *
   * @throws DeepLinkException
   *           when something went wrong while executing the {@link #handleImpl(Matcher, IClientSession)} method. For
   *           instance the user has no permissions to view the requested resource or the resource is not available
   * @return True if this handler has handled the given path, false otherwise
   */
  boolean handle(String path, IClientSession clientSession) throws DeepLinkException;

}
