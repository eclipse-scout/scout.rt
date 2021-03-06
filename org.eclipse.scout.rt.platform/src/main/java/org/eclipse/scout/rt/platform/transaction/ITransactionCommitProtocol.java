/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
