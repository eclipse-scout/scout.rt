package org.eclipse.scout.rt.platform.util.concurrent;

import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Common base error for all cases where a job get's interrupted
 *
 * @since 6.1
 */
public abstract class AbstractInterruptionError extends PlatformError {
  private static final long serialVersionUID = 1L;

  public AbstractInterruptionError(final String message, final Object... args) {
    this(MessageFormatter.arrayFormat(message, args));
  }

  /**
   * Creates a {@link PlatformError} with the given SLF4j format.
   */
  protected AbstractInterruptionError(final FormattingTuple format) {
    super(format.getMessage(), format.getThrowable());
  }

}
