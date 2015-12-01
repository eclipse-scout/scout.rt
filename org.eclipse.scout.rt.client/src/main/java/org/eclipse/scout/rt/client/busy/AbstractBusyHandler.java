/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.busy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Shows blocking progress when {@link ModelJobManager} is doing a long operation.
 * </p>
 * <p>
 * The decision whether or not the progress should be visible is made in the acceptor
 * {@link AbstractBusyHandler#acceptFuture(IFuture, IJobManager)}
 * </p>
 * <p>
 * The strategy to display busy and blocking progress can be changed by overriding {@link #runBusy(Object)} and
 * {@link #runBusy(IRunnableWithProgress)}.
 * </p>
 * Implementations are ui specific and can be found in the rendering projects.
 * <p>
 * This abstract implementation is Thread-safe.
 *
 * @author imo
 * @since 3.8
 */
public abstract class AbstractBusyHandler implements IBusyHandler {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBusyHandler.class);
  private static final String TIMER_PROPERTY = AbstractBusyHandler.class.getName() + ".timer";
  private static final String BUSY_OPERATION_PROPERTY = AbstractBusyHandler.class.getName() + ".busy";

  private final Object m_stateLock = new Object();
  private final Set<IFuture<?>> m_list = Collections.synchronizedSet(new HashSet<IFuture<?>>());
  private long m_shortOperationMillis = 200L;
  private long m_longOperationMillis = 3000L;
  private boolean m_enabled = true;

  private int m_blockingCount;
  private final Object m_blockingCountLock = new Object();

  public AbstractBusyHandler() {
  }

  @Override
  public boolean acceptFuture(IFuture<?> future) {
    return ModelJobs.isModelJob(future);
  }

  @Override
  public void onJobBegin(IFuture<?> future) {
    addTimer(future);
  }

  @Override
  public void onJobEnd(IFuture<?> future) {
    removeTimer(future);
    //avoid unnecessary locks
    if (isBusyOperationNoLock(future)) {
      removeBusyOperation(future);
    }
  }

  @Override
  public void onBlockingBegin() {
    synchronized (m_blockingCountLock) {
      m_blockingCount++;
    }
  }

  @Override
  public void onBlockingEnd() {
    synchronized (m_blockingCountLock) {
      if (m_blockingCount == 0) {
        return;
      }

      m_blockingCount--;

      if (m_blockingCount == 0) {
        m_blockingCountLock.notifyAll();
      }
    }
  }

  @Override
  public boolean isBlocking() {
    synchronized (m_blockingCountLock) {
      return m_blockingCount > 0;
    }
  }

  @Override
  public void waitForBlockingToEnd() {
    synchronized (m_blockingCountLock) {
      while (isBlocking()) {
        LOG.debug("Waiting for the application to exit blocking mode");
        try {
          m_blockingCountLock.wait();
        }
        catch (InterruptedException e) {
          LOG.warn("Interrupted while waiting for the application to exit blocking mode.");
        }
      }
    }
  }

  @Override
  public final Object getStateLock() {
    return m_stateLock;
  }

  @Override
  public void cancel() {
    synchronized (getStateLock()) {
      for (IFuture<?> job : m_list) {
        try {
          job.cancel(true);
        }
        catch (Exception t) {
          //nop
        }
      }
    }
  }

  /**
   * This method is called directly before calling {@link #runBusy()} and can be used to late-check if busy handling is
   * really necessary at that point in time.
   * <p>
   * The default checks if the job is in a smart tree operation and ignores busy, see
   * {@link ContentAssistTreeForm#JOB_PROPERTY_LOAD_TREE}.
   */
  protected boolean shouldRunBusy(IFuture<?> future) {
    return true;
  }

  /**
   * This method is called directly from the job listener after {@link #getShortOperationMillis()}.
   * <p>
   * {@link #getStateLock()} can be used synchronized to check {@link #isBusy()}
   * <p>
   * Be careful what to do, since this might be expensive. The default starts a {@link BusyJob} or a subclass of the
   * {@link BusyJob}
   */
  protected abstract void runBusy(IFuture<?> future);

  /**
   * @retrun true if blocking is active
   */
  @Override
  public final boolean isBusy() {
    return m_list.size() > 0;
  }

  @Override
  public long getShortOperationMillis() {
    return m_shortOperationMillis;
  }

  public void setShortOperationMillis(long shortOperationMillis) {
    m_shortOperationMillis = shortOperationMillis;
  }

  @Override
  public long getLongOperationMillis() {
    return m_longOperationMillis;
  }

  public void setLongOperationMillis(long longOperationMillis) {
    m_longOperationMillis = longOperationMillis;
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  private void addTimer(IFuture<?> future) {
    P_TimerJob runnable = new P_TimerJob(future);
    future.getJobInput().getRunContext().withProperty(TIMER_PROPERTY, runnable);

    Jobs.schedule(runnable, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withSchedulingDelay(getShortOperationMillis(), TimeUnit.MILLISECONDS));
  }

  private void removeTimer(IFuture<?> future) {
    future.getJobInput().getRunContext().withProperty(TIMER_PROPERTY, null);
  }

  private void addBusyOperation(IFuture<?> future) {
    int oldSize, newSize;
    synchronized (getStateLock()) {
      future.getJobInput().getRunContext().withProperty(BUSY_OPERATION_PROPERTY, Boolean.TRUE);
      oldSize = m_list.size();
      m_list.add(future);
      newSize = m_list.size();
      getStateLock().notifyAll();
    }
    if (oldSize == 0 && newSize == 1) {
      runBusy(future);
    }
  }

  private void removeBusyOperation(IFuture<?> future) {
    synchronized (getStateLock()) {
      future.getJobInput().getRunContext().withProperty(BUSY_OPERATION_PROPERTY, null);
      m_list.remove(future);
      getStateLock().notifyAll();
    }
  }

  private boolean isBusyOperationNoLock(IFuture<?> future) {
    return future.getJobInput().getRunContext().containsProperty(BUSY_OPERATION_PROPERTY);
  }

  private static boolean isJobActive(IFuture<?> future) {
    return !future.isDone() && !future.isBlocked();
  }

  private class P_TimerJob implements IRunnable {
    private final IFuture<?> m_future;

    public P_TimerJob(IFuture<?> future) {
      m_future = future;
    }

    @Override
    public void run() throws Exception {
      if (P_TimerJob.this != m_future.getJobInput().getRunContext().getProperty(TIMER_PROPERTY)) {
        return;
      }
      removeTimer(m_future);
      if (isJobActive(m_future)) {
        if (!isEnabled() || !shouldRunBusy(m_future)) {
          return;
        }
        addBusyOperation(m_future);
      }
      //double check after queuing (avoid unnecessary locks)
      if (!isJobActive(m_future)) {
        removeBusyOperation(m_future);
      }
    }
  }

}
