/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.index.AbstractMultiValueIndex;
import org.eclipse.scout.rt.platform.index.AbstractSingleValueIndex;
import org.eclipse.scout.rt.platform.index.IndexedStore;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Registry that contains the {@link RunMonitor}s of operations which are currently executing, and which are subject for
 * global cancellation. Typically, such operations are HTTP service requests initiated by a client, and are cancelled by
 * a respective cancellation request.
 *
 * @since 5.1
 */
@ApplicationScoped
public class RunMonitorCancelRegistry {

  private boolean m_destroyed;
  protected final IndexedStore<RegistryEntry> m_registry;
  private final Object m_registryLock = new Object();

  /** Index by {@link RunMonitor} object */
  protected final P_RunMonitorIndex m_runMonitorIndex = new P_RunMonitorIndex();
  /** Index by session ID */
  protected final P_SessionIdIndex m_sessionIdIndex = new P_SessionIdIndex();
  /** Index by client-server request ID */
  protected final P_SessionIdRequestIdIndex m_sessionIdRequestIdIndex = new P_SessionIdRequestIdIndex();

  public RunMonitorCancelRegistry() {
    m_registry = new IndexedStore<>();
    m_registry.registerIndex(m_runMonitorIndex);
    m_registry.registerIndex(m_sessionIdIndex);
    m_registry.registerIndex(m_sessionIdRequestIdIndex);
  }

  /**
   * Adds a {@link RunMonitor} to this registry, which will be cancelled upon shutdown of the platform.
   *
   * @param runMonitor
   *          the monitor to be registered.
   * @see #cancelAll()
   */
  public IRegistrationHandle register(final RunMonitor runMonitor) {
    return register(runMonitor, null, null);
  }

  /**
   * Adds a {@link RunMonitor} to this registry, which will be cancelled upon cancellation of the service request (if
   * specified), or shutdown of the session (if specified) or platform.
   *
   * @param runMonitor
   *          the monitor to be registered.
   * @param sessionId
   *          the sessionId under which to register the monitor, unless <code>null</code> is provided.
   * @param requestId
   *          the requestId under which to register the monitor, unless <code>null</code> is provided.
   * @see #cancelAllBySessionId(String)
   * @see #cancelAllBySessionIdAndRequestId(String, long)
   */
  public IRegistrationHandle register(final RunMonitor runMonitor, final String sessionId, final Long requestId) {
    final RegistryEntry entry = new RegistryEntry(runMonitor, sessionId, requestId);

    synchronized (m_registryLock) {
      Assertions.assertFalse(m_destroyed, "{} not available because the platform has been shut down.", getClass().getSimpleName());
      m_registry.add(entry);
    }

    return () -> {
      synchronized (m_registryLock) {
        m_registry.remove(entry);
      }
    };
  }

  /**
   * Cancels all registered monitors.
   *
   * @return <code>true</code> if all monitors could be cancelled, or <code>false</code> if no monitor is registered, or
   *         if at least one monitor was already cancelled or failed to be cancelled.
   */
  public boolean cancelAll() {
    final List<RegistryEntry> registryEntries;

    synchronized (m_registryLock) {
      registryEntries = m_runMonitorIndex.values();
      m_registry.remove(registryEntries);
    }

    return cancelRunMonitors(getRunMonitors(registryEntries));
  }

  /**
   * Cancels all registered monitors associated with the given 'sessionId' except the one that is currently running and
   * invoking this method (to ensure that the calling {@link RunMonitor} is not cancelled).
   *
   * @return <code>true</code> if all monitors matching the given 'sessionId' could be cancelled, or <code>false</code>
   *         if no monitor is registered, or if at least one monitor was already cancelled or failed to be cancelled.
   */
  public boolean cancelAllBySessionId(final String sessionId) {
    final List<RegistryEntry> registryEntries;

    final RunMonitor currentRunMonitor = RunMonitor.CURRENT.get();
    synchronized (m_registryLock) {
      registryEntries = m_sessionIdIndex.get(sessionId);
      registryEntries.removeIf(next -> next.getRunMonitor() == currentRunMonitor);
      m_registry.remove(registryEntries);
    }

    return cancelRunMonitors(getRunMonitors(registryEntries));
  }

  /**
   * Cancels all registered monitors associated with the given 'sessionId' and 'requestId'.
   *
   * @return <code>true</code> if all monitors matching the given 'sessionId' and 'requestId' could be cancelled, or
   *         <code>false</code> if no monitor is registered, or if at least one monitor was already cancelled or failed
   *         to be cancelled.
   */
  public boolean cancelAllBySessionIdAndRequestId(final String sessionId, final long requestId) {
    final List<RegistryEntry> entries;

    synchronized (m_registryLock) {
      entries = m_sessionIdRequestIdIndex.get(new CompositeObject(sessionId, requestId));
      m_registry.remove(entries);
    }

    return cancelRunMonitors(getRunMonitors(entries));
  }

  /**
   * Returns the monitors as contained in the given entries.
   */
  protected List<RunMonitor> getRunMonitors(final List<RegistryEntry> registryEntries) {
    final List<RunMonitor> runMonitors = new ArrayList<>(registryEntries.size());
    for (final RegistryEntry registryEntry : registryEntries) {
      runMonitors.add(registryEntry.getRunMonitor());
    }
    return runMonitors;
  }

  /**
   * Cancels the given monitors.
   */
  protected boolean cancelRunMonitors(final List<RunMonitor> runMonitors) {
    final Set<Boolean> status = new HashSet<>();

    for (final RunMonitor runMonitor : runMonitors) {
      status.add(runMonitor.cancel(true));
    }

    return Collections.singleton(Boolean.TRUE).equals(status);
  }

  /**
   * Method invoked to destroy this registry.
   */
  protected void destroy() {
    synchronized (m_registryLock) {
      m_destroyed = true;
    }
    cancelAll();
  }

  // Used for testing purpose.
  protected Set<RunMonitor> getAll() {
    return m_runMonitorIndex.indexValues();
  }

  // Used for testing purpose.
  protected List<RunMonitor> getAllBySession(final String sessionId) {
    return getRunMonitors(m_sessionIdIndex.get(sessionId));
  }

  // Used for testing purpose.
  protected List<RunMonitor> getAllBySessionIdAndRequestId(final String sessionId, final long requestId) {
    return getRunMonitors(m_sessionIdRequestIdIndex.get(new CompositeObject(sessionId, requestId)));
  }

  /**
   * A token representing the registration of a monitor. This token can later be used to unregister the monitor.
   */
  @FunctionalInterface
  public interface IRegistrationHandle {

    /**
     * Unregisters the associated {@link RunMonitor} from {@link RunMonitorCancelRegistry}.
     */
    void unregister();
  }

  protected static class RegistryEntry {

    private final RunMonitor m_runMonitor;
    private final String m_sessionId;
    private final Long m_requestId;

    public RegistryEntry(final RunMonitor runMonitor, final String sessionId, final Long requestId) {
      m_runMonitor = runMonitor;
      m_sessionId = sessionId;
      m_requestId = requestId;
    }

    public RunMonitor getRunMonitor() {
      return m_runMonitor;
    }

    public String getSessionId() {
      return m_sessionId;
    }

    public Long getRequestId() {
      return m_requestId;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .attr("runMonitor", m_runMonitor)
          .attr("sessionId", m_sessionId)
          .attr("requestId", m_requestId)
          .toString();
    }
  }

  // ====  Index definitions ==== //

  /**
   * Index by 'RunMonitor' (primary key).
   */
  protected static class P_RunMonitorIndex extends AbstractSingleValueIndex<RunMonitor, RegistryEntry> {

    @Override
    protected RunMonitor calculateIndexFor(final RegistryEntry runMonitorEntry) {
      return runMonitorEntry.getRunMonitor();
    }
  }

  /**
   * Index by 'sessionId'.
   */
  protected static class P_SessionIdIndex extends AbstractMultiValueIndex<String, RegistryEntry> {

    @Override
    protected String calculateIndexFor(final RegistryEntry runMonitorEntry) {
      return runMonitorEntry.getSessionId();
    }
  }

  /**
   * Index by 'sessionId' and 'requestId'.
   */
  protected static class P_SessionIdRequestIdIndex extends AbstractMultiValueIndex<CompositeObject, RegistryEntry> {

    @Override
    protected CompositeObject calculateIndexFor(final RegistryEntry runMonitorEntry) {
      return new CompositeObject(runMonitorEntry.getSessionId(), runMonitorEntry.getRequestId());
    }
  }

  /**
   * {@link IPlatformListener} to shutdown this registry upon platform shutdown.
   */
  public static class PlatformListener implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        BEANS.get(RunMonitorCancelRegistry.class).destroy();
      }
    }
  }
}
