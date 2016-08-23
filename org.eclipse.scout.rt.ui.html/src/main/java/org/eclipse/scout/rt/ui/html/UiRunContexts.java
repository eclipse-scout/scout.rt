/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Factory methods to create new {@link UiRunContext} objects to propagate UI state like {@link UiSession}.
 * <p>
 * Usage:
 *
 * <pre>
 * UiRunContexts.copyCurrent().withSession(...).run(new IRunnable() {
 *
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 *
 * @since 5.2
 * @see UiRunContext
 */
public final class UiRunContexts {

  private UiRunContexts() {
  }

  /**
   * Creates a "snapshot" of the current calling context for values managed by {@link UiRunContext} class. This method
   * requires to run in a {@link RunContext}, meaning that {@link RunContext#CURRENT} is set, or this method throws an
   * {@link AssertionException} otherwise.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is registered as child monitor of {@link RunMonitor#CURRENT}, meaning that the
   * context is cancelled upon cancellation of the current (parent) monitor. Cancellation works top-down, so
   * cancellation of the context's monitor has no effect to the current (parent) monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRED} which starts a new transaction only if not running in
   * a transaction yet.
   * <p>
   * {@link ITransactionMember}<br>
   * If the current context has some transaction members registered, those are not registered with the new context.
   * <p>
   * {@link ThreadLocal}<br>
   * Thread-Locals associated with the current context via {@link RunContext#withThreadLocal(ThreadLocal, Object)} are
   * copied as well.
   *
   * @throws AssertionException
   *           if not running in a {@link RunContext}
   */
  public static UiRunContext copyCurrent() {
    return BEANS.get(UiRunContextFactory.class).copyCurrent();
  }

  /**
   * Factory to create initialized {@link UiRunContext} objects.
   */
  @ApplicationScoped
  public static class UiRunContextFactory extends RunContextFactory {

    @Override
    public UiRunContext copyCurrent() {
      return (UiRunContext) super.copyCurrent();
    }

    @Override
    public UiRunContext empty() {
      throw new UnsupportedOperationException(); // not supported to not loose context information accidentally (e.g. the authenticated subject)
    }

    @Override
    protected UiRunContext newInstance() {
      return BEANS.get(UiRunContext.class);
    }
  }
}
