/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.concurrent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.concurrent.DeferredOperationQueue;

/**
 * Test fixture that does not schedule any concurrent flush jobs.
 */
public class FixtureDeferredOperationQueue<E> extends DeferredOperationQueue<E> {

  private final AtomicBoolean m_scheduleFlushJobInvoked = new AtomicBoolean();

  public FixtureDeferredOperationQueue(String transactionMemberId, int batchSize, long maxDelayMillis, Consumer<List<E>> batchOperation) {
    this(transactionMemberId, batchSize, maxDelayMillis, batchOperation, null);
  }

  public FixtureDeferredOperationQueue(String transactionMemberId, int batchSize, long maxDelayMillis, Consumer<List<E>> batchOperation, String flushJobName) {
    super(transactionMemberId, batchSize, maxDelayMillis, batchOperation, null, flushJobName);
  }

  @Override
  protected void scheduleFlushJob() {
    // do not schedule job but track method invocation
    m_scheduleFlushJobInvoked.set(true);
  }

  public boolean getAndResetScheduleFlushJobWasInvoked() {
    return m_scheduleFlushJobInvoked.getAndSet(false);
  }
}
