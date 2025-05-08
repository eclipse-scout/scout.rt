/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>AbstractHealthChecker</code> is the basis of all managed {@link IHealthChecker} implementations.
 * <p>
 * It provides asynchronous execution, time-to-live caching of results to avoid denial-of-service due to health check
 * flooding, and timeout of asynchronous executions to avoid hanging checks.
 *
 * @since 6.1
 */
public abstract class AbstractHealthChecker implements IHealthChecker {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractHealthChecker.class);

  protected final Map<HealthCheckCategoryId, LastStatusEntry> m_lastStatusMap = new ConcurrentHashMap<>();

  protected final String m_name;
  protected final long m_timeToLive;
  protected final long m_timeout;

  private IFuture<Boolean> m_future;
  private long m_futureStart;

  private final ReentrantLock m_lock = new ReentrantLock();

  public AbstractHealthChecker() {
    m_name = getConfiguredName();
    m_timeToLive = getConfiguredTimeToLiveMillis();
    m_timeout = getConfiguredTimeoutMillis();
  }

  protected String getConfiguredName() {
    return getClass().getSimpleName();
  }

  /**
   * @return The status lifetime duration in milliseconds. If greater than zero, status expire after the given duration.
   * This prevents denial-of-service due to health check flooding.
   */
  protected long getConfiguredTimeToLiveMillis() {
    return TimeUnit.SECONDS.toMillis(1);
  }

  /**
   * @return The health check timeout duration in milliseconds. If greater than zero,
   * {@link #execCheckHealth(HealthCheckCategoryId)} will time out after given duration.
   */
  protected long getConfiguredTimeoutMillis() {
    return 0;
  }

  protected abstract boolean execCheckHealth(HealthCheckCategoryId category) throws Exception;

  @Override
  public String getName() {
    return m_name;
  }

  protected IFuture getFuture() throws InterruptedException {
    m_lock.lockInterruptibly();
    try {
      return m_future;
    }
    finally {
      m_lock.unlock();
    }
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public boolean checkHealth(RunContext context, HealthCheckCategoryId category) {
    LastStatusEntry lastStatusEntry = m_lastStatusMap.get(category == null ? Empty.ID : category);
    if (lastStatusEntry == null) {
      lastStatusEntry = new LastStatusEntry(m_timeToLive);
    }
    if (!lastStatusEntry.isExpired()) {
      return lastStatusEntry.getLastStatus();
    }
    if (!m_lock.tryLock()) {
      return lastStatusEntry.getLastStatus();
    }

    // expired & lock acquired
    try {
      if (m_future != null) {
        if (m_future.isFinished()) {
          // new status available
          Boolean result = null;
          try {
            result = m_future.awaitDoneAndGet();
            LOG.debug("HealthCheck[{}] is finished and result is stored, status={}, future={}", getName(), result, m_future);
          }
          catch (Throwable t) {
            result = false;
            LOG.warn("HealthCheck[{}] failed, future={}.", getName(), m_future, t);
          }
          lastStatusEntry.setLastStatus(BooleanUtility.nvl(result));
          lastStatusEntry.resetTimestamp();
          m_future = null;
        }
        else if (m_timeout > 0 && m_futureStart + m_timeout < System.currentTimeMillis()) {
          LOG.warn("HealthCheck[{}] has timed out after {}ms, future={}. Cancelling job now.", getName(), m_timeout, m_future);
          m_future.cancel(true);
          m_future = null;
          lastStatusEntry.setLastStatus(false);
          lastStatusEntry.resetTimestamp();
        }
      }

      if (m_future == null && lastStatusEntry.isExpired()) {
        // time to refresh
        m_futureStart = System.currentTimeMillis();
        m_future = Jobs.schedule(() -> {
          LOG.debug("HealthCheck[{}] has started", getName());
          try {
            boolean result = execCheckHealth(category);
            notifyHealthCheckResult(result);
            return result;
          }
          catch (InterruptedException e) {
            LOG.debug("HealthCheck[{}] was interrupted", getName(), e);
            return false;
          }
        }, Jobs.newInput()
            .withRunContext(context)
            .withName(getName()));
        LOG.debug("HealthCheck[{}] was started with a new scheduled job, future={}", getName(), m_future);
      }

      return lastStatusEntry.getLastStatus();
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Called after a health check was executed.
   *
   * @param result
   *     status of last executed health check
   */
  protected void notifyHealthCheckResult(boolean result) {
    if (result) {
      LOG.debug("HealthCheck[{}] was successful", getName());
    }
    else {
      LOG.warn("HealthCheck[{}] failed.", getName());
    }
  }

  /**
   * This is a sentinel value and not a real category available for health checking
   */
  private static final class Empty implements IHealthCheckCategory {
    public static final HealthCheckCategoryId ID = HealthCheckCategoryId.of("empty");

    @Override
    public HealthCheckCategoryId getId() {
      return ID;
    }
  }

  protected class LastStatusEntry {
    private boolean m_lastStatus = false;
    private long m_timestamp = 0L;
    private final long m_timeToLive;

    LastStatusEntry(long timeToLive) {
      m_timeToLive = timeToLive;
    }

    public boolean getLastStatus() {
      return m_lastStatus;
    }

    public void setLastStatus(boolean lastStatus) {
      m_lastStatus = lastStatus;
    }

    public long getTimestamp() {
      return m_timestamp;
    }

    public void resetTimestamp() {
      m_timestamp = System.currentTimeMillis();
    }

    public boolean isExpired() {
      return m_timeToLive <= 0 || m_timestamp + m_timeToLive < System.currentTimeMillis();
    }
  }
}
