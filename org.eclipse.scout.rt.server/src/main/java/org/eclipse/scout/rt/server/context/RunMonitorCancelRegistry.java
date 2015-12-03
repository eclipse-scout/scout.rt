/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Registry that contains the {@link RunMonitor}s of operations which are currently executed, and which are subject for
 * global cancellation. Typically, such operations are HTTP service requests initiated by a client, and are cancelled by
 * a respective cancellation request.
 * <p>
 * The RunMonitors are not really hold in this registry, but bound to the given {@link ISession}.
 */
@ApplicationScoped
public class RunMonitorCancelRegistry {

  protected static final String RUN_MONITORS_KEY = "activeRunMonitors";

  /**
   * Registers the given {@link RunMonitor} by binding it to the given {@link ISession} with the given <code>id</code>.
   * Has no effect if the given <code>id</code> is <code>0</code>.
   */
  public void register(final ISession session, final long id, final RunMonitor monitor) {
    Assertions.assertNotNull(session, "session must not be null");
    put(session, id, monitor);
  }

  /**
   * Unregisters the {@link RunMonitor} bound to the given {@link ISession} and <code>id</code>. Has no effect if no
   * monitor is found.
   */
  public void unregister(final ISession session, final long id) {
    Assertions.assertNotNull(session, "session must not be null");
    remove(session, id);
  }

  /**
   * Cancels and removes the {@link RunMonitor} which was bound to the given {@link ISession} and <code>id</code>. Has
   * no effect if no monitor is found.
   *
   * @return <code>true</code> if cancel was successful.
   */
  public boolean cancel(final ISession session, final long id) {
    Assertions.assertNotNull(session, "session must not be null");
    final RunMonitor runMonitor = remove(session, id);
    return (runMonitor != null ? runMonitor.cancel(true) : false);
  }

  // === Internal methods ===

  protected void put(final ISession session, final long id, final RunMonitor monitor) {
    if (id != 0L) {
      synchronized (session) {
        get(session, true).put(id, new WeakReference<RunMonitor>(monitor));
      }
    }
  }

  protected RunMonitor remove(final ISession session, final long id) {
    if (id == 0L) {
      return null;
    }
    WeakReference<RunMonitor> monitorWeakRef = null;
    synchronized (session) {
      final Map<Long, WeakReference<RunMonitor>> monitors = get(session, false);
      if (monitors != null) {
        monitorWeakRef = monitors.remove(id);
        if (monitors.isEmpty()) {
          session.setData(RUN_MONITORS_KEY, null); // free memory
        }
      }
    }
    return (monitorWeakRef != null ? monitorWeakRef.get() : null);
  }

  @SuppressWarnings("unchecked")
  protected Map<Long, WeakReference<RunMonitor>> get(final ISession session, final boolean autoCreate) {
    Map<Long, WeakReference<RunMonitor>> monitors = (Map<Long, WeakReference<RunMonitor>>) session.getData(RUN_MONITORS_KEY);
    if (monitors == null && autoCreate) {
      monitors = new HashMap<>();
      session.setData(RUN_MONITORS_KEY, monitors);
    }
    return monitors;
  }
}
