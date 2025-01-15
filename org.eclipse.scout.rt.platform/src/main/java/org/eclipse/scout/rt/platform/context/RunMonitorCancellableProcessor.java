/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Callable decorator to register {@link RunMonitor} with a parent {@link RunMonitor} as a cancellable just during
 * execution. At the end of execution it must be unregistered again.
 */
public class RunMonitorCancellableProcessor implements ICallableDecorator {

  private final RunMonitor m_runMonitor;
  private final RunMonitor m_parentRunMonitor;

  /**
   * @param parentRunMonitor
   *     may be <code>null</code> if none available (in this case {@link #decorate()} does nothing)
   * @param runMonitor
   *     the run monitor to be registered with the parent, may never be <code>null</code>
   */
  public RunMonitorCancellableProcessor(RunMonitor parentRunMonitor, RunMonitor runMonitor) {
    m_runMonitor = Assertions.assertNotNull(runMonitor);
    m_parentRunMonitor = parentRunMonitor;
  }

  /**
   * the run monitor to be registered with the parent, never <code>null</code>
   */
  public RunMonitor getRunMonitor() {
    return m_runMonitor;
  }

  /**
   * may be <code>null</code>
   */
  public RunMonitor getParentRunMonitor() {
    return m_parentRunMonitor;
  }

  @Override
  public IUndecorator decorate() throws Exception {
    if (m_parentRunMonitor == null || m_runMonitor == m_parentRunMonitor) {
      return null;
    }

    m_parentRunMonitor.registerCancellable(m_runMonitor);

    return () -> {
      // setCleanupRunMonitor also checks itself if cleanup is already possible
      m_runMonitor.addCleanupRunMonitor(m_parentRunMonitor);
    };
  }
}
