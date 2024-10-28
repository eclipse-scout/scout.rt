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

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.scout.rt.platform.util.concurrent.DeferredOperationQueue;

/**
 * Test fixture that does not schedule any concurrent flush jobs.
 */
public class FixtureDeferredOperationQueue<E> extends DeferredOperationQueue<E> {

  private final AtomicBoolean m_scheduleFlushJobInvoked = new AtomicBoolean();
  private final List<List<E>> m_executedBatches = new ArrayList<>();

  public FixtureDeferredOperationQueue(String transactionMemberId, int batchSize, long maxDelayMillis, Consumer<List<E>> batchOperation) {
    super(transactionMemberId, batchSize, maxDelayMillis, batchOperation);
  }

  @Override
  protected void scheduleFlushJob() {
    // do not schedule job but track method invocation
    m_scheduleFlushJobInvoked.set(true);
  }

  public boolean getAndResetScheduleFlushJobWasInvoked() {
    return m_scheduleFlushJobInvoked.getAndSet(false);
  }

  @Override
  protected Consumer<List<E>> getBatchOperation() {
    return (List<E> batch) -> {
      m_executedBatches.add(batch);
      super.getBatchOperation().accept(batch);
    };
  }

  public List<List<E>> getExecutedBatches() {
    return unmodifiableList(m_executedBatches);
  }

  public void resetExecutedBatches() {
    m_executedBatches.clear();
  }
}
