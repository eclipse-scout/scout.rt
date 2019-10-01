package org.eclipse.scout.rt.mail.smtp;

import java.net.SocketException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

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

import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * This class implements pooling for SMTP connections. It is intended to be used in conjunction with {@link SmtpHelper}
 * but can also be used standalone.<br>
 * The <em>pooling</em> behavior of this class is implemented as follows:<br>
 * There are no connections created initially. Instead, whenever a call to {@link #leaseConnection(SmtpServerConfig)} is
 * made, the returned {@link SmtpConnectionPoolEntry} is determined as follows:
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
 * If a connection is released by calling {@link #releaseConnection(SmtpConnectionPoolEntry)}, it is not closed
 * immediately but instead returned to the pool as idle connection. A connection is not returned to the pool if one of
 * the following situations occurs:
 * <ul>
 * <li>The connection has reached the max connection lifetime ({@link SmtpPoolMaxConnectionLifetimeProperty}).</li>
 * <li>The connection has reached the max messagex sent per connetion limit
 * ({@link SmtpServerConfig#getMaxMessagesPerConnection()}).</li>
 * <li>An exception occurred while trying to send a message using this connection.</li>
 * </ul>
 * In any case, all threads waiting for connections are notified which causes them to return to step 1 and recheck for
 * idle connections or space left in the pool.<br>
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
   * This method provides means of sending the provided {@link MimeMessage} via a pooled smtp connection.
   *
   * @param smtpServerConfig
   *          An {@link SmtpServerConfig} object containing all the necessary information to create a new connection or
   *          find a matching one in the pool.
   * @param message
   *          The {@link MimeMessage} to want to send.
   * @param recipients
   *          Provide a list of all the intended recipients of the message (to, cc and bcc).
   */
  public void sendMessage(SmtpServerConfig smtpServerConfig, MimeMessage message, Address[] recipients) throws MessagingException {
    SmtpConnectionPoolEntry poolEntry = leaseConnection(smtpServerConfig);
    try {
      poolEntry.sendMessage(message, recipients);
    }
    catch (MessagingException e) {
      if (!isConnectionFailure(e)) {
        throw e;
      }
      LOG.info("Sending message failed on first try due to a connection failure with the leased connection. Will retry with a new connection.", e);
      // if sending failed caused by a connection problem, we exchange our connection for a brand new one
      // exchangeConnection will take care of releasing the connection we provide
      poolEntry = exchangeConnection(poolEntry);
      // we must not close the poolEntry here as this is taken care of by the surrounding try/finally block
      try {
        poolEntry.sendMessage(message, recipients);
      }
      catch (MessagingException e1) {
        LOG.error("Sending failed with the second try", e1);
        throw e1;
      }
    }
    finally {
      releaseConnection(poolEntry);
    }
  }

  /**
   * Call this method in order to retrieve a {@link SmtpConnectionPoolEntry} from the pool. Make sure to call
   * {@link #releaseConnection(SmtpConnectionPoolEntry)} in order to return the connection to the pool.
   *
   * @param smtpServerConfig
   *          An {@link SmtpServerConfig} object containing all the necessary information to create a new connection or
   *          find a matching one in the pool.
   */
  protected SmtpConnectionPoolEntry leaseConnection(SmtpServerConfig smtpServerConfig) {
    Assertions.assertGreater(smtpServerConfig.getPoolSize(), 0, "Pool size of provided SmtpServerConfig must be greater 0.");
    synchronized (m_poolLock) {
      Assertions.assertFalse(m_destroyed, "SmtpConnectionPool not available because it has already been destroyed.");
      SmtpConnectionPoolEntry candidate = null;
      while (candidate == null) {
        // try to find an idle connection or create a new connection if poolsize has not been reached yet.
        // candidate may be null as a result
        try {
          candidate = tryGetIdleOrCreateNewConnection(smtpServerConfig);
        }
        catch (MessagingException e) {
          throw new ProcessingException("MessagingException caught while trying to connect to smtp server.", e);
        }

        // if we could not find an idle connection and the pool has already reached its limit in terms of connection count,
        // we wait until someone releases a connection (@see #releaseConnection(SmtpConnectionPoolEntry))
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
      }
      // we found a valid candidate. Remove it from the idle connection set, add it to the leased connection set and return it
      m_idleEntries.remove(candidate);
      m_leasedEntries.add(candidate);
      LOG.debug("Leasing pooled SMTP connection {}", candidate);
      return candidate;
    }
  }

  protected SmtpConnectionPoolEntry tryGetIdleOrCreateNewConnection(SmtpServerConfig smtpServerConfig) throws MessagingException {
    Assertions.assertGreater(smtpServerConfig.getPoolSize(), 0, "Invalid pool size '{}'; must be greater 0", smtpServerConfig.getPoolSize());
    synchronized (m_poolLock) {
      Predicate<SmtpConnectionPoolEntry> poolFilter = getPoolFilter(smtpServerConfig);
      SmtpConnectionPoolEntry candidate = null;
      candidate = m_idleEntries.stream()
          .filter(poolFilter)
          .sorted((pe1, pe2) -> {
            // sort ascending by create time to favor younger connections when picking from the pool
            // as a result, older connections (which are less likely to be picked) are thus more likely to reach max idle time and are collected.
            return (int) (pe1.getCreateTime() - pe2.getCreateTime());
          })
          .findFirst()
          .orElse(null);

      if (candidate != null) {
        return candidate;
      }

      return createNewConnection(smtpServerConfig);
    }
  }

  protected SmtpConnectionPoolEntry createNewConnection(SmtpServerConfig smtpServerConfig) throws MessagingException {
    synchronized (m_poolLock) {
      Predicate<SmtpConnectionPoolEntry> poolFilter = getPoolFilter(smtpServerConfig);
      boolean firstConnection = m_idleEntries.isEmpty() && m_leasedEntries.isEmpty();
      // there was no idle connection so we create a new connection if the pool size has not been reached for this config
      long idleEntryCount = m_idleEntries.stream()
          .filter(poolFilter)
          .count();
      long leasedEntryCount = m_leasedEntries.stream()
          .filter(poolFilter)
          .count();
      SmtpConnectionPoolEntry poolEntry = null;

      if (idleEntryCount + leasedEntryCount < smtpServerConfig.getPoolSize()) {
        Session session = BEANS.get(SmtpHelper.class).createSession(smtpServerConfig);
        @SuppressWarnings("resource") // suppress warning about resource leak, we are managing transports ourselves
        Transport transport = session.getTransport();
        BEANS.get(SmtpHelper.class).connect(session, transport, smtpServerConfig.getPassword());
        IDateProvider dateProvider = BEANS.get(IDateProvider.class);
        poolEntry = BEANS.get(SmtpConnectionPoolEntry.class)
            .withName(getNextPoolEntryName())
            .withSmtpServerConfig(smtpServerConfig)
            .withSession(session)
            .withTransport(transport)
            .withCreateTime(dateProvider.currentMillis().getTime())
            .withIdleSince(dateProvider.currentMillis().getTime());

        LOG.debug("Created new pooled SMTP connection {}", poolEntry);
        m_idleEntries.add(poolEntry);

        if (firstConnection) {
          // if there were neither idle nor leased connections before, the first connection has just been created
          // start job for connection cleanup
          LOG.debug("First connection created, starting close-idle-connections job.");
          startCloseIdleConnectionsJob();
        }
      }
      return poolEntry;
    }
  }

  protected Predicate<SmtpConnectionPoolEntry> getPoolFilter(SmtpServerConfig smtpServerConfig) {
    return ssc -> ssc.matchesConfig(smtpServerConfig);
  }

  protected void startCloseIdleConnectionsJob() {
    Jobs.schedule(this::closeIdleConnections, Jobs.newInput()
        .withName(JOB_NAME_CLOSE_IDLE_CONNECTIONS)
        .withExecutionHint(m_jobExecutionHint)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MINUTES)
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.MINUTES))));
  }

  /**
   * This method releases the provided {@link SmtpConnectionPoolEntry} and returns it back to the pool as idle
   * connection.<br>
   */
  protected void releaseConnection(SmtpConnectionPoolEntry poolEntry) {
    synchronized (m_poolLock) {
      Assertions.assertFalse(m_destroyed, "SmtpConnectionPool not available because it has already been destroyed.");

      m_leasedEntries.remove(poolEntry);

      if (poolEntry.isFailed()) {
        LOG.debug("Releasing pooled SMTP connection {}; transport is broken, not returning to idle pool.", poolEntry);
        safeCloseTransport(poolEntry);
        poolEntry = null;
      }

      if (poolEntry != null) {
        P_ReuseCheckResult reuseCheckResult = isReuseAllowed(poolEntry);
        if (reuseCheckResult.isReuseAllowed()) {
          LOG.debug("Releasing pooled SMTP connection {}; returning to idle pool.", poolEntry);
          poolEntry.withIdleSince(BEANS.get(IDateProvider.class).currentMillis().getTime());
          m_idleEntries.add(poolEntry);
        }
        else {
          safeCloseTransport(poolEntry);
          LOG.debug("Releasing pooled SMTP connection {}; {}, not returning to idle pool.", poolEntry, reuseCheckResult.getReuseDeniedReason());
        }
      }
      m_poolLock.notifyAll();
    }
  }

  /**
   * Closes the transport of the provided {@link SmtpConnectionPoolEntry} and removes it from the pool. After that, a
   * new connection is created, integrated into the pool and returned to the caller.<br>
   * Use this method when some sort of connection problem occurred while trying to send a message and you suspect that
   * sending might work with a fresh connection.
   *
   * @param oldEntry
   *          The {@link SmtpConnectionPoolEntry} you wish to exchange for a new one.
   * @return Returns the just created {@link SmtpConnectionPoolEntry} using the same {@link SmtpServerConfig} as the
   *         provided entry.
   */
  protected SmtpConnectionPoolEntry exchangeConnection(SmtpConnectionPoolEntry oldEntry) throws MessagingException {
    synchronized (m_poolLock) {

      m_leasedEntries.remove(oldEntry);
      safeCloseTransport(oldEntry);

      SmtpConnectionPoolEntry newEntry = createNewConnection(oldEntry.getSmtpServerConfig());

      m_idleEntries.remove(newEntry);
      m_leasedEntries.add(newEntry);

      return newEntry;
    }
  }

  protected P_ReuseCheckResult isReuseAllowed(SmtpConnectionPoolEntry smtpConnectionPoolEntry) {
    IDateProvider dateProvider = BEANS.get(IDateProvider.class);
    if (dateProvider.currentMillis().getTime() - smtpConnectionPoolEntry.getCreateTime() >= m_maxConnectionLifetime) {
      return new P_ReuseCheckResult(false, "pooled connection reached max lifetime of {}s", m_maxConnectionLifetime / 1000d);
    }
    int maxMessagesPerConnection = smtpConnectionPoolEntry.getSmtpServerConfig().getMaxMessagesPerConnection();
    if (maxMessagesPerConnection > 0 && smtpConnectionPoolEntry.getMessagesSent() >= maxMessagesPerConnection) {
      return new P_ReuseCheckResult(false, "pooled connection reached max messages sent of {}", maxMessagesPerConnection);
    }
    return new P_ReuseCheckResult(true, null);
  }

  protected String getNextPoolEntryName() {
    synchronized (m_poolLock) {
      return "pool-entry-" + ++m_lastPoolEntryNo;
    }
  }

  protected boolean isConnectionFailure(MessagingException e) {
    // when trying to send an e-mail using a broken exception there seem to be two variants of exceptions being thrown:
    // 1. MessagingException with a next SocketException as next exception
    // 2. SMTPSendFailedException with "[EOF]" as message
    return e != null && (e.getNextException() instanceof SocketException ||
        (e instanceof SMTPSendFailedException && "[EOF]".equals(e.getMessage())));
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

  protected void safeCloseTransport(SmtpConnectionPoolEntry poolEntry) {
    LOG.debug("Closing pooled SMTP connection {}", poolEntry);
    try {
      poolEntry.getTransport().close();
    }
    catch (MessagingException e) {
      LOG.warn("Could not close transport for pooled SMTP connection {}; assume already closed/crashed.", poolEntry, e);
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
