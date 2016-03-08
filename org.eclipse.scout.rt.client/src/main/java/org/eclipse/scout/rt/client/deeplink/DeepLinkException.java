package org.eclipse.scout.rt.client.deeplink;

/**
 * A checked exception which indicates that a deep-link URL could not be processed for some (business logic) reason. The
 * exception is typically thrown when the pattern is valid, but no data has been found for the requested resource.
 */
public class DeepLinkException extends Exception {

  private static final long serialVersionUID = 1L;

}
