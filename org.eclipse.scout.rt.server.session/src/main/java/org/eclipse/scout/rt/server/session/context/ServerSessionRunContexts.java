/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.context;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.server.context.ServerRunContexts.ServerRunContextFactory;

/**
 * Factory methods to create a new {@link ServerSessionRunContext} objects to propagate server-side state. See
 * {@link RunContexts} for more information.
 * <p>
 * Usage:
 *
 * <pre>
 * ServerSessionRunContexts.copyCurrent()
 *   .withLocale(Locale.US)
 *   .withSubject(...)
 *   .withSession(...)
 *   .withTransactionScope(TransactionScope.REQUIRES_NEW)
 *   .run(new IRunnable() {
 *
 *    &#064;Override
 *    public void run() {
 *      // run code on behalf of the context
 *    }
 * });
 * </pre>
 *
 * @see ServerSessionRunContext
 */
public final class ServerSessionRunContexts {

  private ServerSessionRunContexts() {
  }

  /**
   * Creates an empty {@link ServerSessionRunContext} with all values managed by {@link ServerSessionRunContext} class set to their
   * default value. This method does not require to already run in a {@link RunContext}.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is not registered as child monitor of {@link RunMonitor#CURRENT}, meaning that
   * the context is not cancelled upon cancellation of the current monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRES_NEW} which always starts a new transaction.
   */
  public static ServerSessionRunContext empty() {
    return BEANS.get(ServerSessionRunContextFactory.class).empty();
  }

  /**
   * Creates a "snapshot" of the current calling context for values managed by {@link ServerSessionRunContext} class. This
   * method requires to run in a {@link RunContext}, meaning that {@link RunContext#CURRENT} is set, or this method
   * throws an {@link AssertionException} otherwise.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is registered as child monitor of {@link RunMonitor#CURRENT}, meaning that the
   * context is cancelled upon cancellation of the current (parent) monitor. Cancellation works top-down, so
   * cancellation of the context's monitor has no effect to the current (parent) monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRES_NEW} which always starts a new transaction.
   * <p>
   * {@link ITransactionMember}<br>
   * If the current context has some transaction members registered, those are not registered with the new context.
   * <p>
   * {@link ThreadLocal}<br>
   * Thread-Locals associated with the current context via {@link RunContext#withThreadLocal(ThreadLocal, Object)} are
   * copied as well.
   *
   * @throws AssertionException
   *     if not running in a {@link RunContext}
   */
  public static ServerSessionRunContext copyCurrent() {
    return copyCurrent(false);
  }

  /**
   * Same as {@link ServerSessionRunContexts#copyCurrent()}, but less strict if not running in a {@link RunContext} yet.
   *
   * @param orElseEmpty
   *     indicates whether to return an empty {@link RunContext} if not running in a context yet.
   * @throws AssertionException
   *     if not running in a {@link RunContext}, and <i>orElseEmpty</i> is set to <code>false</code>.
   */
  public static ServerSessionRunContext copyCurrent(final boolean orElseEmpty) {
    if (RunContext.CURRENT.get() != null) {
      return BEANS.get(ServerSessionRunContextFactory.class).copyCurrent();
    }
    if (orElseEmpty) {
      return BEANS.get(ServerSessionRunContextFactory.class).empty();
    }
    return Assertions.fail("Not running in a RunContext. Use '{}.empty()' or {}.copyCurrent(true) instead.", ServerSessionRunContexts.class.getSimpleName(), ServerSessionRunContexts.class.getSimpleName());
  }

  /**
   * Factory to create initialized {@link ServerSessionRunContext} objects.
   */
  @Replace
  public static class ServerSessionRunContextFactory extends ServerRunContextFactory {

    @Override
    public ServerSessionRunContext empty() {
      return (ServerSessionRunContext) super.empty();
    }

    @Override
    public ServerSessionRunContext copyCurrent() {
      return (ServerSessionRunContext) super.copyCurrent();
    }
  }
}
