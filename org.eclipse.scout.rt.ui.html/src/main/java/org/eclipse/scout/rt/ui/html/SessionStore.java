package org.eclipse.scout.rt.ui.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopUIFacade;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingMaxWaitShutdownProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreMaxWaitAllShutdownProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreMaxWaitWriteLockProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a session store per HTTP session.
 * <p>
 * It serves also as a listener for HTTP session invalidation. If it detects an invalid HTTP session, it tries to clean
 * up all associated client and UI sessions. See {@link #valueUnbound(HttpSessionBindingEvent)}.
 *
 * @since 5.2
 */
public class SessionStore implements ISessionStore, HttpSessionBindingListener {
  private static final Logger LOG = LoggerFactory.getLogger(SessionStore.class);

  private final HttpSession m_httpSession;
  private final String m_httpSessionId; // because getId() cannot be called on an invalid session
  private volatile boolean m_httpSessionValid = true;

  /**
   * key = clientSessionId
   */
  protected final Map<String, IClientSession> m_clientSessionMap = new HashMap<>();
  /**
   * key = uiSessionId
   */
  protected final Map<String, IUiSession> m_uiSessionMap = new HashMap<>();
  /**
   * key = clientSession (<i>not</i> clientSessionId!)<br>
   * value = set of UI sessions (technically there can be multiple UI sessions by client session, although usually there
   * is only one or none).
   */
  protected final Map<IClientSession, Set<IUiSession>> m_uiSessionsByClientSession = new HashMap<>();

  /**
   * Map of scheduled housekeeping jobs (key = clientSessionId). Using this map, scheduled but not yet executed
   * housekeepings can be cancelled again when the client session is still to be used.
   */
  protected final Map<String, IFuture<?>> m_housekeepingFutures = new HashMap<String, IFuture<?>>();

  protected final ReadLock m_readLock;
  protected final WriteLock m_writeLock;

  public SessionStore(HttpSession httpSession) {
    Assertions.assertNotNull(httpSession);
    m_httpSession = httpSession;
    m_httpSessionId = httpSession.getId();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();

    LOG.debug("Created new session store for HTTP session with ID {}", m_httpSessionId);
  }

  @Override
  public HttpSession getHttpSession() {
    return m_httpSession;
  }

  @Override
  public String getHttpSessionId() {
    return m_httpSessionId;
  }

  @Override
  public final boolean isHttpSessionValid() {
    return m_httpSessionValid;
  }

  /**
   * Can be used by subclasses to set {@link #m_httpSessionValid} to <code>false</code> (but not back to
   * <code>true</code>).
   */
  protected final void setHttpSessionInvalid() {
    m_httpSessionValid = false;
  }

  @Override
  public Map<String, IClientSession> getClientSessionMap() {
    m_readLock.lock();
    try {
      return new HashMap<>(m_clientSessionMap);
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public Map<String, IUiSession> getUiSessionMap() {
    m_readLock.lock();
    try {
      return new HashMap<>(m_uiSessionMap);
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public Map<IClientSession, Set<IUiSession>> getUiSessionsByClientSession() {
    m_readLock.lock();
    try {
      Map<IClientSession, Set<IUiSession>> copy = new HashMap<>();
      for (Entry<IClientSession, Set<IUiSession>> entry : m_uiSessionsByClientSession.entrySet()) {
        copy.put(entry.getKey(), (entry.getValue() == null ? null : new HashSet<>(entry.getValue())));
      }
      return copy;
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public int countUiSessions() {
    m_readLock.lock();
    try {
      return m_uiSessionMap.size();
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public int countClientSessions() {
    m_readLock.lock();
    try {
      return m_clientSessionMap.size();
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    m_readLock.lock();
    try {
      return m_uiSessionMap.isEmpty() && m_clientSessionMap.isEmpty() && m_uiSessionsByClientSession.isEmpty();
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public IUiSession getUiSession(String uiSessionId) {
    if (uiSessionId == null) {
      return null;
    }
    m_readLock.lock();
    try {
      return m_uiSessionMap.get(uiSessionId);
    }
    finally {
      m_readLock.unlock();
    }
  }

  @Override
  public void registerUiSession(final IUiSession uiSession) {
    Assertions.assertNotNull(uiSession);
    LOG.debug("Register UI session with ID {} in store (clientSessionId={})", uiSession.getUiSessionId(), uiSession.getClientSessionId());
    m_writeLock.lock();
    try {
      IClientSession clientSession = uiSession.getClientSession();

      // Store UI session
      m_uiSessionMap.put(uiSession.getUiSessionId(), uiSession);

      // Store client session (in case it was not yet stored)
      m_clientSessionMap.put(clientSession.getId(), clientSession);

      // Link to client session
      Set<IUiSession> map = m_uiSessionsByClientSession.get(clientSession);
      if (map == null) {
        map = new HashSet<>();
        m_uiSessionsByClientSession.put(clientSession, map);
      }
      map.add(uiSession);
    }
    finally {
      m_writeLock.unlock();
    }
  }

  @Override
  public void unregisterUiSession(final IUiSession uiSession) {
    if (uiSession == null) {
      return;
    }
    LOG.debug("Unregister UI session with ID {} from store (clientSessionId={})", uiSession.getUiSessionId(), uiSession.getClientSessionId());
    m_writeLock.lock();
    try {
      // Remove uiSession
      m_uiSessionMap.remove(uiSession.getUiSessionId());

      // Unlink uiSession from clientSession
      final IClientSession clientSession = uiSession.getClientSession();
      Set<IUiSession> map = m_uiSessionsByClientSession.get(clientSession);
      if (map != null) {
        map.remove(uiSession);
      }

      // Start housekeeping
      LOG.debug("{} UI sessions remaining for client session {}", (map == null ? 0 : map.size()), clientSession.getId());
      if (map == null || map.isEmpty()) {
        m_uiSessionsByClientSession.remove(clientSession);
        startHousekeeping(clientSession);
      }
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * If the given client session is still active, schedule a job that checks whether it is still in use after some time
   * (see {@link SessionStoreHousekeepingDelayProperty}). If not, it will be stopped and removed from the store. If the
   * session is inactive from the beginning, it is just removed from the store.
   * <p>
   * <b>Important:</b>: This method must be called from within a lock!
   */
  protected void startHousekeeping(final IClientSession clientSession) {
    // No client session, no house keeping necessary
    if (clientSession == null) {
      return;
    }

    // If client session is already inactive, simply update the maps, but take no further action.
    if (!clientSession.isActive()) {
      LOG.info("Session housekeeping: Removing inactive client session with ID {} from store", clientSession.getId());
      removeClientSession(clientSession);
      return;
    }

    // Check if client session is still used after a few moments
    LOG.debug("Session housekeeping: Schedule job for client session with ID {}", clientSession.getId());
    final IFuture<Void> future = Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        doHousekeeping(clientSession);
      }
    }, Jobs.newInput()
        .withName("Performing session housekeeping for client session with ID {}", clientSession.getId())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(CONFIG.getPropertyValue(SessionStoreHousekeepingDelayProperty.class), TimeUnit.SECONDS)));

    // Put the future in a list, so we can cancel it if the session is requested again
    m_housekeepingFutures.put(clientSession.getId(), future);
  }

  /**
   * Checks if the client session is still used by a UI session. If not, it is stopped and removed form the store.
   */
  protected void doHousekeeping(final IClientSession clientSession) {
    m_writeLock.lock();
    try {
      if (IFuture.CURRENT.get().isCancelled()) {
        return;
      }
      m_housekeepingFutures.remove(clientSession.getId());

      if (!clientSession.isActive() || clientSession.isStopping()) {
        LOG.info("Session housekeeping: Client session {} is {}, removing it from store", clientSession.getId(), (!clientSession.isActive() ? "inactive" : "stopping"));
        removeClientSession(clientSession);
        return;
      }

      // Check if the client session is referenced by any UI session
      Set<IUiSession> uiSessions = m_uiSessionsByClientSession.get(clientSession);
      LOG.debug("Session housekeeping: Client session {} referenced by {} UI sessions", clientSession.getId(), (uiSessions == null ? 0 : uiSessions.size()));
      if (uiSessions == null || uiSessions.isEmpty()) {
        LOG.info("Session housekeeping: Shutting down client session with ID {} because it is not used anymore", clientSession.getId());
        try {
          final IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              forceClientSessionShutdown(clientSession);
            }
          }, ModelJobs.newInput(ClientRunContexts.copyCurrent().withSession(clientSession, true))
              .withName("Force shutting down client session {} by session housekeeping", clientSession.getId()));

          int timeout = CONFIG.getPropertyValue(SessionStoreHousekeepingMaxWaitShutdownProperty.class).intValue();
          try {
            future.awaitDone(timeout, TimeUnit.SECONDS);
          }
          catch (TimedOutException e) {
            LOG.warn("Client session did no stop within {} seconds. Canceling shutdown job.", timeout);
            future.cancel(true);
          }
        }
        catch (ThreadInterruptedException e) {
          LOG.warn("Interruption encountered while waiting for client session {} to stop. Continuing anyway.", clientSession.getId(), e);
        }
        finally {
          removeClientSession(clientSession);
        }
      }
    }
    finally {
      m_writeLock.unlock();
    }
  }

  @Override
  public IClientSession getClientSessionForUse(String clientSessionId) {
    if (clientSessionId == null) {
      return null;
    }
    m_writeLock.lock();
    try {
      // If housekeeping is scheduled for this session, cancel it (session will be used again, so no cleanup necessary)
      IFuture<?> future = m_housekeepingFutures.get(clientSessionId);
      if (future != null) {
        LOG.debug("Client session with ID {} reserved for use - session housekeeping cancelled!", clientSessionId);
        future.cancel(false);
        m_housekeepingFutures.remove(clientSessionId);
      }

      IClientSession clientSession = m_clientSessionMap.get(clientSessionId);
      if (clientSession != null && (!clientSession.isActive() || clientSession.isStopping())) {
        // only return active sessions
        clientSession = null;
      }
      return clientSession;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  protected void removeClientSession(final IClientSession clientSession) {
    m_writeLock.lock();
    try {
      LOG.debug("Remove client session with ID {} from session store", clientSession.getId());
      m_clientSessionMap.remove(clientSession.getId());

      if (m_clientSessionMap.isEmpty() && m_httpSessionValid) {
        // no more client sessions -> invalidate HTTP session
        try {
          m_httpSession.getCreationTime(); // dummy call to prevent the following log statement when the session is already invalid
          LOG.info("Invalidate HTTP session with ID {} because session store contains no more client sessions", m_httpSessionId);
          m_httpSession.invalidate();
        }
        catch (IllegalStateException e) { // NOSONAR
          // already invalid
        }
      }
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Stops the given session if it is active. To stop it, {@link IDesktopUIFacade#closeFromUI(boolean)} is called, which
   * forces the desktop to close without opening any more forms (which could be the case when using
   * {@link IClientSession#stop()}).
   * <p>
   * If the client session is still active after that, a warning is printed to the log.
   */
  protected void forceClientSessionShutdown(IClientSession clientSession) {
    Assertions.assertNotNull(clientSession);
    IDesktop desktop = clientSession.getDesktop();
    if (!clientSession.isActive()) {
      LOG.debug("Client session with ID {} is already inactive.", clientSession.getId());
    }
    else if (clientSession.isStopping()) {
      LOG.debug("Client session with ID {} is already stopping.", clientSession.getId());
    }
    else {
      LOG.debug("Forcing session with ID {} to shut down...", clientSession.getId());
      desktop.getUIFacade().closeFromUI(true); // true = force
      if (clientSession.isActive()) {
        LOG.warn("Client session with ID {} is still {} after forcing it to shutdown!", clientSession.getId(), (clientSession.isStopping() ? "stopping" : "active"));
      }
      else {
        LOG.info("Client session with ID {} terminated.", clientSession.getId());
      }
    }
  }

  @Override
  public void valueBound(HttpSessionBindingEvent event) {
    // ignore notifications about being bound to an HTTP session
  }

  @Override
  public void valueUnbound(HttpSessionBindingEvent event) {
    if (!m_httpSessionValid) {
      // valueUnbound() has already been executed
      return;
    }
    m_httpSessionValid = false;
    LOG.info("Detected invalidation of HTTP session {}, cleaning up {} client sessions and {} UI sessions", m_httpSessionId, m_clientSessionMap.size(), m_uiSessionMap.size());
    final List<IFuture<?>> futures = new ArrayList<>();

    // Stop all client sessions (in parallel model jobs)
    try {
      int timeout = CONFIG.getPropertyValue(SessionStoreMaxWaitWriteLockProperty.class).intValue();
      if (m_writeLock.tryLock(timeout, TimeUnit.SECONDS)) {
        try {
          for (final IClientSession clientSession : m_clientSessionMap.values()) {
            futures.add(ModelJobs.schedule(new IRunnable() {
              @Override
              public void run() {
                LOG.debug("Shutting down client session with ID {} due to invalidation of HTTP session", clientSession.getId());
                forceClientSessionShutdown(clientSession);
                removeClientSession(clientSession);
              }
            }, ModelJobs.newInput(ClientRunContexts.copyCurrent().withSession(clientSession, true))
                .withName("Closing desktop due to HTTP session invalidation")));
          }
        }
        finally {
          m_writeLock.unlock();
        }
      }
      else {
        LOG.warn("Could not acquire write lock within {} seconds: [HTTP session: {}, uiSessionMap: {}, clientSessionMap: {}, uiSessionsByClientSession: {}]",
            timeout, m_uiSessionMap.size(), m_clientSessionMap.size(), m_uiSessionsByClientSession.size());
      }
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting on session store write lock", e);
    }

    if (futures.isEmpty()) {
      return;
    }

    // After some time, check if everything was cleaned up correctly ("leak detection").
    // (This is done in a separate job to not block the session invalidation.)
    Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        if (futures.size() > 0) {
          LOG.debug("Waiting for {} client sessions to stop...", futures.size());
          try {
            Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
                .andMatchFuture(futures)
                .toFilter(), CONFIG.getPropertyValue(SessionStoreMaxWaitAllShutdownProperty.class), TimeUnit.SECONDS);
            LOG.info("Session shutdown complete.");
          }
          catch (ThreadInterruptedException e) {
            LOG.warn("Interruption encountered while waiting for all client session to stop. Continuing anyway.", e);
          }
          catch (TimedOutException e) {
            LOG.warn("Timeout encountered while waiting for all client session to stop. Canceling still running client session shutdown jobs.", e);

            Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
                .andMatchFuture(futures)
                .andMatchNotState(JobState.DONE)
                .toFilter(), true);
          }
        }

        // Read map sizes outside a lock - dirty reads are acceptable here
        final int uiSessionMapSize = m_uiSessionMap.size();
        final int clientSessionMapSize = m_clientSessionMap.size();
        final int uiSessionsByClientSessionSize = m_uiSessionsByClientSession.size();
        if (uiSessionMapSize + clientSessionMapSize + uiSessionsByClientSessionSize > 0) {
          LOG.warn("Leak detection - Session store not empty after HTTP session invalidation: [uiSessionMap: {}, clientSessionMap: {}, uiSessionsByClientSession: {}]",
              uiSessionMapSize, clientSessionMapSize, uiSessionsByClientSessionSize);
        }
      }
    }, Jobs.newInput()
        .withName("Waiting for {} client sessions to shut down", futures.size()));
  }
}
