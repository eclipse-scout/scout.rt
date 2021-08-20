/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
}
