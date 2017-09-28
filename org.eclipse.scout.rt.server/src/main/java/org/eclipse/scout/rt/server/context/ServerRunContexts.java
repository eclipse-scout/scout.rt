/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Factory methods to create a new {@link ServerRunContext} objects to propagate server-side state. See
 * {@link RunContexts} for more information.
 * <p>
 * Usage:
 *
 * <pre>
 * ServerRunContexts.copyCurrent()
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
 * @since 5.1
 * @see ServerRunContext
 */
public final class ServerRunContexts {

  private ServerRunContexts() {
  }

  /**
   * Creates an empty {@link ServerRunContext} with all values managed by {@link ServerRunContext} class set to their
   * default value. This method does not require to already run in a {@link RunContext}.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is not registered as child monitor of {@link RunMonitor#CURRENT}, meaning that
   * the context is not cancelled upon cancellation of the current monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRES_NEW} which always starts a new transaction.
   */
  public static ServerRunContext empty() {
    return BEANS.get(ServerRunContextFactory.class).empty();
  }

  /**
   * Creates a "snapshot" of the current calling context for values managed by {@link ServerRunContext} class. This
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
   *           if not running in a {@link RunContext}
   */
  public static ServerRunContext copyCurrent() {
    return copyCurrent(false);
  }

  /**
   * Same as {@link ServerRunContexts#copyCurrent()}, but less strict if not running in a {@link RunContext} yet.
   *
   * @param orElseEmpty
   *          indicates whether to return an empty {@link RunContext} if not running in a context yet.
   * @throws AssertionException
   *           if not running in a {@link RunContext}, and <i>orElseEmpty</i> is set to <code>false</code>.
   */
  public static ServerRunContext copyCurrent(final boolean orElseEmpty) {
    if (RunContext.CURRENT.get() != null) {
      return BEANS.get(ServerRunContextFactory.class).copyCurrent();
    }
    if (orElseEmpty) {
      return BEANS.get(ServerRunContextFactory.class).empty();
    }
    return Assertions.fail("Not running in a RunContext. Use '{}.empty()' or {}.copyCurrent(true) instead.", ServerRunContexts.class.getSimpleName(), ServerRunContexts.class.getSimpleName());
  }

  /**
   * Factory to create initialized {@link ServerRunContext} objects.
   */
  @Replace
  public static class ServerRunContextFactory extends RunContextFactory {

    @Override
    public ServerRunContext empty() {
      return (ServerRunContext) super.empty()
          .withProperty(PropertyMap.PROP_SERVER_SCOPE, true)
          .withTransactionScope(TransactionScope.REQUIRES_NEW);
    }

    @Override
    public ServerRunContext copyCurrent() {
      return (ServerRunContext) super.copyCurrent()
          .withProperty(PropertyMap.PROP_SERVER_SCOPE, true)
          .withTransactionScope(TransactionScope.REQUIRES_NEW);
    }

    @Override
    protected ServerRunContext newInstance() {
      return BEANS.get(ServerRunContext.class);
    }
  }
}
