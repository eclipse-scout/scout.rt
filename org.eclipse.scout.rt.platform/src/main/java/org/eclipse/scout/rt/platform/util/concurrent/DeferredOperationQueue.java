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
package org.eclipse.scout.rt.platform.util.concurrent;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue for elements for which a particular operation is performed once either enough items have been added or a time
 * delay has been reached (based on the arrival of the first of the currently pending elements).
 */
public class DeferredOperationQueue<E> {

  private static final Logger LOG = LoggerFactory.getLogger(DeferredOperationQueue.class);

  private final String m_transactionMemberId;
  private final int m_batchSize;
  private final long m_maxDelayMillis;
  private final Consumer<List<E>> m_batchOperation;
  private final BlockingQueue<E> m_queue;
  private final AtomicBoolean m_flushJobScheduled;
  private final ReadWriteLock m_lock;
  private volatile IFuture<?> m_flushJobFuture;

  public DeferredOperationQueue(String transactionMemberId, int batchSize, long maxDelayMillis, Consumer<List<E>> batchOperation) {
    m_transactionMemberId = assertNotNull(transactionMemberId, "transactionMemberId is required");
    assertTrue(batchSize > 0, "batchSize must be greater than 0 [given value:{}]", batchSize);
    assertTrue(maxDelayMillis >= 0, "maxDelayMillis must be positive [given value:{}]", batchSize);
    m_batchSize = batchSize;
    m_maxDelayMillis = maxDelayMillis;
    m_batchOperation = assertNotNull(batchOperation, "batchOperation is required");
    m_queue = new LinkedBlockingQueue<>();
    m_flushJobScheduled = new AtomicBoolean();
    m_lock = new ReentrantReadWriteLock();
  }

  public void add(E element) {
    assertNotNull(element, "element must not be null");
    getOrCreateTransactionMember().add(element);
  }

  public void addAll(Stream<E> stream) {
    assertNotNull(stream, "stream must not be null");
    DeferredOperationQueue<E>.P_DeferredOperationQueueTransactionMember transactionMember = getOrCreateTransactionMember();
    stream.forEach(transactionMember::add);
  }

  protected void addAllInternal(Stream<E> stream) {
    assertNotNull(stream, "stream must not be null");
    m_lock.readLock().lock();
    try {
      stream.filter(Objects::nonNull).forEach(element -> {
        if (!m_queue.offer(element)) {
          throw new PlatformException("Could not add element {}", element);
        }
      });
      if (m_flushJobScheduled.compareAndSet(false, true)) {
        scheduleFlushJob();
      }
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  protected int getBatchSize() {
    return m_batchSize;
  }

  protected long getMaxDelayMillis() {
    return m_maxDelayMillis;
  }

  protected Consumer<List<E>> getBatchOperation() {
    return m_batchOperation;
  }

  protected void scheduleFlushJob() {
    m_flushJobFuture = Jobs.schedule(
        () -> flushDeferred(false),
        Jobs.newInput().withRunContext(RunContexts.empty()));
  }

  /**
   * Flush elements. This method waits up to {@link #getMaxDelayMillis()} milliseconds or until {@link #getBatchSize()}
   * elements are available before invoking the {@link #getBatchOperation()}.
   *
   * @param singleRun
   *          if {@code true}, exactly one batch (either terminated by time or size restrictions) is executed and the
   *          method completes. Otherwise, another round is performed if the batch of the current one was not empty.
   */
  protected void flushDeferred(boolean singleRun) {
    final int batchSize = getBatchSize();
    boolean interrupted = false;
    do {
      List<E> nextBatch = new ArrayList<>(batchSize);
      // get at most batchSize elements at once
      m_queue.drainTo(nextBatch, batchSize);

      if (nextBatch.size() < batchSize) {
        // wait maxDelayMillis for additional elements
        long timeout = getMaxDelayMillis();
        final long deadline = System.currentTimeMillis() + timeout;
        try {
          E next;
          while (timeout > 0 && (next = m_queue.poll(timeout, TimeUnit.MILLISECONDS)) != null) {
            nextBatch.add(next);
            if (nextBatch.size() == batchSize) {
              break;
            }
            timeout = deadline - System.currentTimeMillis();
          }
        }
        catch (InterruptedException e) {
          // do not exit here but execute batch operations on elements that were already collected
          // and update internal state so that next add will schedule another flush job.
          interrupted = true;
        }
      }

      if (nextBatch.isEmpty()) {
        break;
      }

      try {
        getBatchOperation().accept(nextBatch);
      }
      catch (RuntimeException e) {
        LOG.error("Exception occurred while execution batch operation", e);
      }
    }
    while (!interrupted && !singleRun);

    // check whether other elements arrived in the mean time within an exclusive lock
    // to prevent race conditions. This job will finish. But if new items arrived, a new
    // job is scheduled.
    m_lock.writeLock().lock();
    try {
      if (m_queue.isEmpty()) {
        // no new elements -> reset flushJobScheduled property so that the next invocation of
        // add will schedule a new flush job
        m_flushJobScheduled.set(false);
        m_flushJobFuture = null;
      }
      else {
        // there are already new elements -> schedule a new flush job (Note: m_flushJobScheduled
        // is still true, hence invocations of add did not schedule another job).
        scheduleFlushJob();
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * Flushes collected elements synchronously.
   */
  public void flush() {
    final IFuture<?> flushJobFuture = m_flushJobFuture;
    if (flushJobFuture != null) {
      flushJobFuture.cancel(true);
      try {
        flushJobFuture.awaitFinished(1, TimeUnit.SECONDS);
      }
      catch (RuntimeException | PlatformError e) {
        // ignore runtime exceptions and platform errors
      }
    }
    List<E> batch = new LinkedList<>();
    m_queue.drainTo(batch);
    if (!batch.isEmpty()) {
      getBatchOperation().accept(batch);
    }
  }

  protected P_DeferredOperationQueueTransactionMember getOrCreateTransactionMember() {
    assertNotNull(m_transactionMemberId, "Queue created without a transaction member ID; Operation not supported.");
    ITransaction transaction = assertNotNull(ITransaction.CURRENT.get(), "Not running within a transaction");
    @SuppressWarnings("unchecked")
    P_DeferredOperationQueueTransactionMember transactionMember = (DeferredOperationQueue<E>.P_DeferredOperationQueueTransactionMember) transaction.getMember(m_transactionMemberId);
    if (transactionMember == null) {
      transactionMember = new P_DeferredOperationQueueTransactionMember(m_transactionMemberId);
      transaction.registerMember(transactionMember);
    }
    return transactionMember;
  }

  protected class P_DeferredOperationQueueTransactionMember extends AbstractTransactionMember {
    private final List<E> m_elements = new ArrayList<>();

    public P_DeferredOperationQueueTransactionMember(String transactionMemberId) {
      super(transactionMemberId);
    }

    public void add(E element) {
      m_elements.add(element);
    }

    @Override
    public boolean needsCommit() {
      return !m_elements.isEmpty();
    }

    @Override
    public void commitPhase2() {
      DeferredOperationQueue.this.addAllInternal(m_elements.stream());
    }
  }
}
