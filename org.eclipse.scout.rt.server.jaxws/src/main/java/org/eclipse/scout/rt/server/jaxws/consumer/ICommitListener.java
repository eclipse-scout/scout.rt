/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

/**
 * Listener to participate in <code>2-phase-commit-protocol (2PC)</code>.
 *
 * @since 5.1
 */
public interface ICommitListener {

  /**
   * Invoked once the transaction is about to commit.
   *
   * @return <code>true</code> to continue committing the transaction, <code>false</code> to abort.
   */
  boolean onCommitPhase1();

  /**
   * Invoked to finally commit the transaction.
   */
  void onCommitPhase2();
}
