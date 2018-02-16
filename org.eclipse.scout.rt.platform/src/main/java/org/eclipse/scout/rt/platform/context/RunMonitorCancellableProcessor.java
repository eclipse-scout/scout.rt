package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Callable decorator to register {@link RunMonitor} with a parent {@link RunMonitor} as a cancellable just during
 * execution. At the end of execution it must be unregistered again.
 */
public class RunMonitorCancellableProcessor implements ICallableDecorator {

  private RunMonitor m_parentRunMonitor;
  private RunMonitor m_runMonitor;

  /**
   * @param parentRunMonitor
   *          may be <code>null</code> if none available (in this case {@link #decorate()} does nothing)
   * @param runMonitor
   *          the run monitor to be registered with the parent, may never be <code>null</code>
   */
  public RunMonitorCancellableProcessor(RunMonitor parentRunMonitor, RunMonitor runMonitor) {
    Assertions.assertNotNull(runMonitor);
    m_parentRunMonitor = parentRunMonitor;
    m_runMonitor = runMonitor;
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

    return new IUndecorator() {

      @Override
      public void undecorate() {
        m_parentRunMonitor.unregisterCancellable(m_runMonitor);
      }
    };
  }

}
