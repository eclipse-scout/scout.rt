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

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Protocol to commit or roll back a transaction.
 *
 * @since 5.1
 */
@FunctionalInterface
@ApplicationScoped
public interface ITransactionCommitProtocol {

  /**
   * Commits the transaction on success, or rolls it back on error.
   */
  void commitOrRollback(ITransaction tx);
}
