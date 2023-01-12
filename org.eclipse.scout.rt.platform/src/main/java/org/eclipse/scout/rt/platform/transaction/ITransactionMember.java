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

/**
 * Allows the participation of this member in the transaction's commit protocol.
 *
 * @since 3.4
 */
public interface ITransactionMember {

  /**
   * Returns the unique ID of this transaction member.
   */
  String getMemberId();

  /**
   * Queries this member whether it requires a commit.
   */
  boolean needsCommit();

  /**
   * Instructs this member to temporarily commit its data. When returning with <code>true</code>, this member is
   * expected to commit its data successfully upon the invocation of {@link #commitPhase2()}.
   */
  boolean commitPhase1();

  /**
   * Instructs this member to commit its data, and is invoked after all members accepted {@link #commitPhase1()} .
   */
  void commitPhase2();

  /**
   * Instructs this member to rollback all changes, and may also be called after {@link #commitPhase1()}.
   */
  void rollback();

  /**
   * Method invoked to release any resources allocated by this transaction member.
   */
  void release();

  /**
   * Instructs this member to cancel a potential running action, and implies the transaction manager to invoke
   * {@link #rollback()} upon crossing its transaction boundary.
   */
  void cancel();
}
