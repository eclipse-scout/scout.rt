package org.eclipse.scout.rt.mail.smtp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * This class implements pooling for SMTP connections. It is intended to be used in conjunction with {@link SmtpHelper}
 * but can also be used standalone.<br>
 * The <em>pooling</em> behavior of this class is implemented as follows:<br>
 * There are no connections created initially. Instead, whenever a call to {@link #leaseConnection(SmtpServerConfig)} is
 * made, the returned {@link LeasedSmtpConnection} is determined as follows:
 * <ol>
 * <li>A check is made whether there already exists an idle connection matching the provided config (the match is made
 * using the {@link SmtpServerConfig#equals(Object)} method).
 * <ol type="a">
 * <li>If such a connection exists, it is marked as "leased" and returned to the caller.</li>
 * <li>If no such connection exits, the current number of connections (idle and leased) matching the provided
 * {@link SmtpServerConfig} object is checked against the {@link SmtpServerConfig#getPoolSize()}). If the limit has not
 * yet been reached, a new connection is created, marked as leased and returned to the caller.</li>
 * </ol>
 * </li>
 * <li>If there is still no connection (no idle connection and pool size limit reached), the calling thread will wait
 * until a connection is released. The wait time will be according to the
 * {@link SmtpPoolWaitForConnectionTimeoutProperty} property. If the property's value is 0, the wait time will be
 * infinite.</li>
 * </ol>
 * If a connection is released by calling {@link #releaseConnection(LeasedSmtpConnection)}, it is not closed immediately
 * but instead returned to the pool as idle connection. If however a released connection has reached the max connection
 * lifetime ({@link SmtpPoolMaxConnectionLifetimeProperty}), it is closed. Additionally all waiting threads are notified
 * which causes them to return to step 1 and recheck for idle connections or space left in the pool.<br>
 * As soon as a connection is created, a background job is started which monitors idle connections. If they reach the
 * max idle time ({@link SmtpPoolMaxIdleTimeProperty}) or max connection lifetime, they are closed and removed from the
 * pool.
 */
@ApplicationScoped
public class SmtpConnectionPool {

  private static final Logger LOG = LoggerFactory.getLogger(SmtpConnectionPool.class);

  protected static final String JOB_NAME_CLOSE_IDLE_CONNECTIONS = "smtp-close-idle-connections";

  protected final Object m_poolLock = new Object();
  protected final Set<SmtpConnectionPoolEntry> m_idleEntries = new HashSet<>();
  protected final Set<SmtpConnectionPoolEntry> m_leasedEntries = new HashSet<>();
  protected final String m_jobExecutionHint = "smtp-connection-pool." + UUID.randomUUID().toString();
  protected long m_lastPoolEntryNo = 0;

  protected long m_maxIdleTime;
  protected long m_maxConnectionLifetime;
  protected int m_waitForConnectionTimeout;

  protected boolean m_destroyed;

  @PostConstruct
  protected void init() {
    m_maxIdleTime = CONFIG.getPropertyValue(SmtpPoolMaxIdleTimeProperty.class) * 1000;
    m_maxConnectionLifetime = CONFIG.getPropertyValue(SmtpPoolMaxConnectionLifetimeProperty.class) * 1000;
    m_waitForConnectionTimeout = CONFIG.getPropertyValue(SmtpPoolWaitForConnectionTimeoutProperty.class) * 1000;
  }

  /**
   * Call this method in order to retrieve a {@link LeasedSmtpConnection} from the pool. Make sure to call
   * {@link LeasedSmtpConnection#close()} in order to return the connection to the pool. The easiest way to accomplish
   * this is by using the try-with-resources construct as follows:
   *
   * <pre>
   * try (LeasedSmtpConnection connection = BEANS.get(SmtpConnectionPool.class).leaseConnection(config)) {
   *   connection.sendMessage(message, recipients);
   * }
   * </pre>
   *
   * @param smtpServerConfig
   *          An {@link SmtpServerConfig} object containing all the necessary information to create a new connection or
   *          find a matching one in the pool.
   */
  public LeasedSmtpConnection leaseConnection(SmtpServerConfig smtpServerConfig) {
    Assertions.assertGreater(smtpServerConfig.getPoolSize(), 0, "Pool size of provided SmtpServerConfig must be greater 0.");
    synchronized (m_poolLock) {
      Assertions.assertFalse(m_destroyed, "SmtpConnectionPool not available because it has already been destroyed.");
      SmtpConnectionPoolEntry candidate = null;
      while (candidate == null) {
        // try to find an idle connection or create a new connection if poolsize has not been reached yet.
        // candidate may be null as a result
        try {
          candidate = tryGetIdleOrCreateNew(smtpServerConfig);
        }
        catch (MessagingException e) {
          throw new ProcessingException("MessagingException caught while trying to connect to smtp server.", e);
        }

        // if we could not find an idle connection and the pool has already reached its limit in terms of connection count,
        // we wait until someone releases a connection (@see #releaseConnection(LeasedSmtpConnection))
        if (candidate == null) {
          try {
            long startWaitMillis = System.currentTimeMillis();
            m_poolLock.wait(m_waitForConnectionTimeout);
            if (m_waitForConnectionTimeout > 0 &&
                System.currentTimeMillis() >= startWaitMillis + m_waitForConnectionTimeout) {
              // Object.wait(long) does not indicate why it returned (some form of notify called or timeout).
              // So we check, if the current time is greater or equal our recorded starttime plus the configured
              // wait for connection timeout and then assume, that we have been woken up by the timeout.
              throw new ProcessingException("Wait for connection timeout of {}ms exceeded while waiting for an SMTP connection.", m_waitForConnectionTimeout);
            }
          }
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedError("Interrupted while waiting for idle smtp connection");
          }
        }

        // check candidate for valid connection. If candidate connection is disconnected remove it from the pool
        // candidate may be null as a result
        candidate = cleanupDisconnectedIdleConnection(candidate);
      }
      // we found a valid candidate. Remove it from the idle connection set, add it to the leased connection set and return a LeasedSmtpConnection
      m_idleEntries.remove(candidate);
      m_leasedEntries.add(candidate);
      LOG.debug("Leasing pooled SMTP connection {}", candidate);
      return BEANS.get(LeasedSmtpConnection.class)
          .withConnectionPool(this)
          .withConnectionPoolEntry(candidate)
          .withTransport(candidate.getTransport());
    }
  }

  protected SmtpConnectionPoolEntry tryGetIdleOrCreateNew(SmtpServerConfig smtpServerConfig) throws MessagingException {
    Assertions.assertGreater(smtpServerConfig.getPoolSize(), 0, "Invalid pool size '{}'; must be greater 0", smtpServerConfig.getPoolSize());
    Predicate<SmtpConnectionPoolEntry> poolFilter = ssc -> ssc.matchesConfig(smtpServerConfig);
    SmtpConnectionPoolEntry candidate = m_idleEntries.stream()
        .filter(poolFilter)
        .sorted((pe1, pe2) -> {
          // sort ascending by create time to favor younger connections when picking from the pool
          // as a result, older connections (which are less likely to be picked) are thus more likely to reach max idle time and are collected.
          return (int) (pe1.getCreateTime() - pe2.getCreateTime());
        })
        .findFirst()
        .orElse(null);

    boolean firstConnection = m_idleEntries.isEmpty() && m_leasedEntries.isEmpty();
    // there was no idle connection so we create a new connection if the pool size has not been reached for this config
    long idleEntryCount = m_idleEntries.stream()
        .filter(poolFilter)
        .count();
    long leasedEntryCount = m_leasedEntries.stream()
        .filter(poolFilter)
        .count();

    if (candidate == null && idleEntryCount + leasedEntryCount < smtpServerConfig.getPoolSize()) {
      Session session = BEANS.get(SmtpHelper.class).createSession(smtpServerConfig);
      @SuppressWarnings("resource") // suppress warning about resource leak, we are managing transports ourselves
      Transport transport = session.getTransport();
      BEANS.get(SmtpHelper.class).connect(session, transport, smtpServerConfig.getPassword());
      IDateProvider dateProvider = BEANS.get(IDateProvider.class);
      SmtpConnectionPoolEntry poolEntry = BEANS.get(SmtpConnectionPoolEntry.class)
          .withName(getNextPoolEntryName())
          .withSmtpServerConfig(smtpServerConfig)
          .withSession(session)
          .withTransport(transport)
          .withCreateTime(dateProvider.currentMillis().getTime())
          .withIdleSince(dateProvider.currentMillis().getTime());

      LOG.debug("Created new pooled SMTP connection {}", poolEntry);
      m_idleEntries.add(poolEntry);
      candidate = poolEntry;

      if (firstConnection) {
        // if there were neither idle nor leased connections before, the first connection has just been created
        // start job for connection cleanup
        LOG.debug("First connection created, starting close-idle-connections job.");
        Jobs.schedule(this::closeIdleConnections, Jobs.newInput()
            .withName(JOB_NAME_CLOSE_IDLE_CONNECTIONS)
            .withExecutionHint(m_jobExecutionHint)
            .withExecutionTrigger(Jobs.newExecutionTrigger()
                .withStartIn(1, TimeUnit.MINUTES)
                .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.MINUTES))));
      }
    }

    return candidate;
  }

  protected SmtpConnectionPoolEntry cleanupDisconnectedIdleConnection(SmtpConnectionPoolEntry candidate) {
    if (candidate != null && !candidate.getTransport().isConnected()) {
      m_idleEntries.remove(candidate);
      safeCloseTransport(candidate);
      candidate = null;
    }
    return candidate;
  }

  /**
   * This method releases the provided {@link LeasedSmtpConnection} and returns the associated {@link Transport} back to
   * the pool as idle connection.<br>
   * DO NOT DIRECTLY CALL THIS METHOD<br>
   * Instead, this method should be called by {@link LeasedSmtpConnection#close()} only. For details on how to work with
   * {@link LeasedSmtpConnection} see the comment of {@link #leaseConnection(SmtpServerConfig)}.
   */
  protected void releaseConnection(LeasedSmtpConnection leasedSmtpConnection) {
    synchronized (m_poolLock) {
      Assertions.assertFalse(m_destroyed, "SmtpConnectionPool not available because it has already been destroyed.");
      SmtpConnectionPoolEntry candidate = null;

      // Find matching SmtpConnectionPoolEntry for LeasedSmtpConnection
      for (Iterator<SmtpConnectionPoolEntry> it = m_leasedEntries.iterator(); it.hasNext();) {
        SmtpConnectionPoolEntry entry = it.next();
        if (leasedSmtpConnection.getTransport() == entry.getTransport()) {
          candidate = entry;
          it.remove();
          break;
        }
      }

      if (candidate != null && !candidate.getTransport().isConnected()) {
        LOG.debug("Releasing pooled SMTP connection {}; transport is already closed, not returning to idle pool.", candidate);
        candidate = null;
      }

      if (candidate != null) {
        IDateProvider dateProvider = BEANS.get(IDateProvider.class);
        P_ReuseCheckResult reuseCheckResult = isReuseAllowed(candidate);
        if (reuseCheckResult.isReuseAllowed()) {
          LOG.debug("Releasing pooled SMTP connection {}; returning to idle pool.", candidate);
          candidate.withIdleSince(dateProvider.currentMillis().getTime());
          m_idleEntries.add(candidate);
        }
        else {
          safeCloseTransport(candidate);
          LOG.debug("Releasing pooled SMTP connection {}; {}, not returning to idle pool.", candidate, reuseCheckResult.getReuseDeniedReason());
        }
      }
      m_poolLock.notifyAll();
    }
  }

  protected P_ReuseCheckResult isReuseAllowed(SmtpConnectionPoolEntry smtpConnectionPoolEntry) {
    IDateProvider dateProvider = BEANS.get(IDateProvider.class);
    if (dateProvider.currentMillis().getTime() - smtpConnectionPoolEntry.getCreateTime() < m_maxConnectionLifetime) {
      return new P_ReuseCheckResult(false, "pooled connection reached max lifetime of {}s", m_maxConnectionLifetime / 1000d);
    }
    int maxMessagesPerConnection = smtpConnectionPoolEntry.getSmtpServerConfig().getMaxMessagesPerConnection();
    if (maxMessagesPerConnection > 0 && smtpConnectionPoolEntry.getMessagesSent() < maxMessagesPerConnection) {
      return new P_ReuseCheckResult(false, "pooled connection reached max messages sent of {}", maxMessagesPerConnection);
    }
    return new P_ReuseCheckResult(true, null);
  }

  protected String getNextPoolEntryName() {
    return "pool-entry-" + ++m_lastPoolEntryNo;
  }

  protected void closeIdleConnections() {
    synchronized (m_poolLock) {
      try {
        for (Iterator<SmtpConnectionPoolEntry> it = m_idleEntries.iterator(); it.hasNext();) {
          SmtpConnectionPoolEntry idleEntry = it.next();
          IDateProvider dateProvider = BEANS.get(IDateProvider.class);
          if (dateProvider.currentMillis().getTime() - idleEntry.getIdleSince() >= m_maxIdleTime ||
              dateProvider.currentMillis().getTime() - idleEntry.getCreateTime() >= m_maxConnectionLifetime) {
            safeCloseTransport(idleEntry);
            it.remove();
          }
        }
        if (m_idleEntries.isEmpty() && m_leasedEntries.isEmpty()) {
          LOG.debug("Last pooled connection closed, stopping close-idle-connections job.");
          Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
              .andMatchName(JOB_NAME_CLOSE_IDLE_CONNECTIONS)
              .andMatchExecutionHint(m_jobExecutionHint)
              .toFilter(), false);
        }
      }
      catch (RuntimeException e) {
        LOG.warn("Caught RuntimeException while trying to close idle SMTP connections.", e);
      }
    }
  }

  protected void safeCloseTransport(SmtpConnectionPoolEntry smtpConnectionPoolEntry) {
    LOG.debug("Closing pooled SMTP connection {}", smtpConnectionPoolEntry);
    try {
      smtpConnectionPoolEntry.getTransport().close();
    }
    catch (MessagingException e) {
      LOG.warn("Could not close transport for pooled SMTP connection {}; assume already closed/crashed.", smtpConnectionPoolEntry, e);
    }
  }

  protected void destroy() {
    if (m_destroyed) {
      return;
    }

    synchronized (m_poolLock) {
      if (m_destroyed) {
        return;
      }

      Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
          .andMatchExecutionHint(m_jobExecutionHint)
          .toFilter(), true);

      Stream.of(m_idleEntries, m_leasedEntries)
          .flatMap(Collection::stream)
          .forEach(this::safeCloseTransport);

      m_idleEntries.clear();
      m_leasedEntries.clear();
      m_destroyed = true;
    }
  }

  public static class SmtpPoolMaxIdleTimeProperty extends AbstractPositiveIntegerConfigProperty {
    @Override
    public Integer getDefaultValue() {
      return 60; // 1 minute
    }

    @Override
    public String getKey() {
      return "scout.smtp.pool.maxIdleTime";
    }

    @Override
    public String description() {
      return "Max. idle time for pooled connections in seconds.";
    }
  }

  public static class SmtpPoolMaxConnectionLifetimeProperty extends AbstractPositiveIntegerConfigProperty {
    @Override
    public Integer getDefaultValue() {
      return 1800; // 30 minutes
    }

    @Override
    public String getKey() {
      return "scout.smtp.pool.maxConnectionLifetime";
    }

    @Override
    public String description() {
      return "Max. lifetime of pooled connections in seconds.";
    }
  }

  public static class SmtpPoolWaitForConnectionTimeoutProperty extends AbstractPositiveIntegerConfigProperty {
    @Override
    public Integer getDefaultValue() {
      return 300; // 5 minutes
    }

    @Override
    public String getKey() {
      return "scout.smtp.pool.waitForConnectionTimeout";
    }

    @Override
    public String description() {
      return "Max. wait time for SMTP connection in seconds. If the value is 0, callers will wait infinitely long for SMTP connections.";
    }
  }

  public static class PlatformListener implements IPlatformListener {
    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        BEANS.get(SmtpConnectionPool.class).destroy();
      }
    }
  }

  private static class P_ReuseCheckResult {

    private final boolean m_reuseAllowed;
    private final FormattingTuple m_reuseDeniedReason;

    public P_ReuseCheckResult(boolean reuseAllowed, String reuseDeniedReason, Object... messageArgs) {
      m_reuseAllowed = reuseAllowed;
      m_reuseDeniedReason = MessageFormatter.arrayFormat(reuseDeniedReason, messageArgs);
    }

    public boolean isReuseAllowed() {
      return m_reuseAllowed;
    }

    public String getReuseDeniedReason() {
      return m_reuseDeniedReason.getMessage();
    }
  }
}
