package org.eclipse.scout.rt.platform.exception;

import java.io.Serializable;

/**
 * @since 5.2
 */
public class PlatformException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = 1L;

  public PlatformException(String message) {
    super(message);
  }

  public PlatformException(String message, Throwable cause) {
    super(message, cause);
  }

}
