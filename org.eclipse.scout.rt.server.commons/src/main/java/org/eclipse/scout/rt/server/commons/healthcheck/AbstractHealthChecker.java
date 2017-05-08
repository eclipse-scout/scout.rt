package org.eclipse.scout.rt.server.commons.healthcheck;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
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

  private final AtomicBoolean m_lastStatus = new AtomicBoolean(false);
  private final AtomicLong m_timestamp = new AtomicLong(0);

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
   *         This prevents denial-of-service due to health check flooding.
   */
  protected long getConfiguredTimeToLiveMillis() {
    return TimeUnit.SECONDS.toMillis(1);
  }

  /**
   * @return The health check timeout duration in milliseconds. If greater than zero, {@link #execCheckHealth()} will
   *         time out after given duration.
   */
  protected long getConfiguredTimeoutMillis() {
    return 0;
  }

  protected abstract boolean execCheckHealth() throws Exception;

  @Override
  public String getName() {
    return m_name;
  }

  public boolean getLastStatus() {
    return m_lastStatus.get();
  }

  public boolean isExpired() {
    return m_timeToLive <= 0 || m_timestamp.get() + m_timeToLive < System.currentTimeMillis();
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
  public boolean checkHealth(RunContext context) {
    if (!isExpired()) {
      return m_lastStatus.get();
    }
    if (!m_lock.tryLock()) {
      return m_lastStatus.get();
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
            LOG.warn("HealthCheck[{}] failed, future={}.", getName(), m_future, t);
            throw new ProcessingException("HealthCheck[{}] failed, future={}", getName(), m_future, t);
          }
          m_lastStatus.set(BooleanUtility.nvl(result));
          m_timestamp.set(System.currentTimeMillis());
          m_future = null;
        }
        else if (m_timeout > 0 && m_futureStart + m_timeout < System.currentTimeMillis()) {
          LOG.warn("HealthCheck[{}] has timed out after {}ms, future={}. Cancelling job now.", getName(), m_future, m_timeout);
          m_future.cancel(true);
          m_future = null;
          m_lastStatus.set(false);
          m_timestamp.set(System.currentTimeMillis());
          throw new TimedOutError("HealthCheck[{}] has timed, job cancelled.", getName()).withContextInfo("timeout", "{}ms", m_timeout);
        }
      }

      if (m_future == null && isExpired()) {
        // time to refresh
        m_futureStart = System.currentTimeMillis();
        m_future = Jobs.schedule(new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            LOG.debug("HealthCheck[{}] has started", getName());
            try {
              boolean result = execCheckHealth();
              if (result) {
                LOG.debug("HealthCheck[{}] was successful", getName());
              }
              else {
                LOG.warn("HealthCheck[{}] failed.", getName());
              }
              return result;
            }
            catch (InterruptedException e) {
              LOG.debug("HealthCheck[{}] was interrupted", getName(), e);
              return false;
            }
          }
        }, Jobs.newInput()
            .withRunContext(context)
            .withName(getName()));
        LOG.debug("HealthCheck[{}] was started with a new scheduled job, future={}", getName(), m_future);
      }

      return m_lastStatus.get();
    }
    finally {
      m_lock.unlock();
    }
  }

}
