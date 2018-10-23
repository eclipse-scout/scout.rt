package org.eclipse.scout.rt.platform.transaction;

import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;

/**
 * @since 16.0
 */
public class TransactionCancelledError extends AbstractInterruptionError {
  private static final long serialVersionUID = 1L;

  public TransactionCancelledError() {
    super("Scout transaction is cancelled");
  }

  public TransactionCancelledError(String message, Object... args) {
    super(message, args);
  }
}
