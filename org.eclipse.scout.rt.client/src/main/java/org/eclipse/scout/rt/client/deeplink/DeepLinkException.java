package org.eclipse.scout.rt.client.deeplink;

import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * A checked exception which indicates that a deep-link URL could not be processed for some (business logic) reason.
 * There are two cases:
 * <ol>
 * <li>The regex pattern is valid and matches, but no data has been found for the requested deep-link path</li>
 * <li>A resource has been found but the current user has no permissions to read the resource. In that case this
 * exception contains the original VetoException as cause</li>
 * </ol>
 */
public class DeepLinkException extends Exception {

  private static final long serialVersionUID = 1L;

  public DeepLinkException() {
  }

  /**
   * Use this constructor if resource requested by deep-link was not found.
   *
   * @param message
   */
  public DeepLinkException(String message) {
    super(message);
  }

  /**
   * Use this constructor if user has insufficient permissions to display the requested deep-link.
   *
   * @param cause
   *          the original VetoException thrown when permission is denied
   */
  public DeepLinkException(VetoException cause) {
    super(cause);
  }

}
