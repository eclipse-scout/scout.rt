/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.transaction;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * A <code>TransactionRequiredException</code> is thrown if a {@link ServerRunContext} requires a transaction to be
 * available.
 *
 * @see TransactionScope#MANDATORY
 * @since 5.1
 */
public class TransactionRequiredException extends PlatformException {

  private static final long serialVersionUID = 1L;

  public TransactionRequiredException() {
    super("Transaction expected, but no transaction is available");
  }
}
