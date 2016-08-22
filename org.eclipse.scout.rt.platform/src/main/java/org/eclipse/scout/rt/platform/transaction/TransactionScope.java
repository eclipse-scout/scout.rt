/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.transaction;

/**
 * The <code>TransactionScope</code> controls the transaction demarcation of a {@link ServerRunContext}.
 *
 * @see ServerRunContext
 * @since 5.1
 */
public enum TransactionScope {
  /**
   * This transaction scope guarantees to always run in a transaction, either within the caller's transaction, or if not
   * applicable, in a new transaction.
   * <p/>
   * If the caller is running within a transaction, the executable is run within the caller’s transaction. If the caller
   * is not associated with a transaction, the <code>ServerRunContext</code> starts a new transaction before running the
   * executable, which is committed or rolled back upon completion.
   */
  REQUIRED,
  /**
   * This transaction scope guarantees to always run in a new transaction.
   * <p/>
   * If the caller is already running within a transaction, that transaction is suspended for the time of running the
   * executable, and resumed upon its completion. However, the executable is always run in a new transaction, which is
   * committed or rolled back upon completion.
   */
  REQUIRES_NEW,
  /**
   * This transaction scope enforces to run in the caller's transaction.
   * <p/>
   * If the caller is running within a transaction, that transaction is used to run the executable. If the caller is not
   * associated with a transaction, a {@link TransactionRequiredException} is thrown. Use the <code>MANDATORY</code>
   * scope if the executable must use the transaction of the caller.
   */
  MANDATORY;
}
