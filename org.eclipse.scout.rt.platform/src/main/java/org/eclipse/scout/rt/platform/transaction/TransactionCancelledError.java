/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.transaction;

import org.eclipse.scout.rt.platform.util.concurrent.AbstractInterruptionError;

/**
 * @since 16.1
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
