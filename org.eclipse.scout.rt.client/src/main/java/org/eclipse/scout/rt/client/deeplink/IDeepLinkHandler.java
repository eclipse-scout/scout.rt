package org.eclipse.scout.rt.client.deeplink;

import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Interface for all classes that provide deep-link logic.
 */
@ApplicationScoped
public interface IDeepLinkHandler {

  String NUMERIC_REGEX = "\\d+";

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
  boolean handle(String path) throws DeepLinkException;

  /**
   * @return the name of this handler as used in the deep-link URL.
   */
  String getName();

}
