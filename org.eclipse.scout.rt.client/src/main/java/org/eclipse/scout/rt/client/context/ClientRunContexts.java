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
package org.eclipse.scout.rt.client.context;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.context.PropertyMap;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunContexts.RunContextFactory;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Factory methods to create a new {@link ClientRunContext} objects to propagate client-side state. See
 * {@link RunContexts} for more information.
 * <p>
 * Usage:
 *
 * <pre>
 * ClientRunContexts.copyCurrent()
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
 * @see ClientRunContext
 */
public final class ClientRunContexts {

  private ClientRunContexts() {
  }

  /**
   * Creates an empty {@link ClientRunContext} with all values managed by {@link ClientRunContext} class set to their
   * default value. This method does not require to already run in a {@link RunContext}.
   * <p>
   * {@link RunMonitor}<br>
   * Uses a new {@link RunMonitor} which is not registered as child monitor of {@link RunMonitor#CURRENT}, meaning that
   * the context is not cancelled upon cancellation of the current monitor.
   * <p>
   * {@link TransactionScope}<br>
   * Uses the transaction scope {@link TransactionScope#REQUIRED}. Because 'empty' constructs a {@link RunContext}
   * without a transaction associated, it starts a new transaction if not specifying a transaction via
   * {@link RunContext#withTransaction(ITransaction)}.
   */
  public static ClientRunContext empty() {
    return BEANS.get(ClientRunContextFactory.class).empty();
  }

  /**
   * Creates a "snapshot" of the current calling context for values managed by {@link ClientRunContext} class. This
   * method requires to run in a {@link RunContext}, meaning that {@link RunContext#CURRENT} is set, or this method
   * throws an {@link AssertionException} otherwise.
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
  public static ClientRunContext copyCurrent() {
    return copyCurrent(false);
  }

  /**
   * Same as {@link ClientRunContexts#copyCurrent()}, but less strict if not running in a {@link RunContext}.
   *
   * @param orElseEmpty
   *          indicates whether to return an empty {@link RunContext} if not running in a context yet.
   * @throws AssertionException
   *           if not running in a {@link RunContext}, and <i>orElseEmpty</i> is set to <code>false</code>.
   */
  public static ClientRunContext copyCurrent(final boolean orElseEmpty) {
    if (RunContext.CURRENT.get() != null) {
      return BEANS.get(ClientRunContextFactory.class).copyCurrent();
    }
    if (orElseEmpty) {
      return BEANS.get(ClientRunContextFactory.class).empty();
    }
    return Assertions.fail("Not running in a RunContext. Use '{}.empty()' or {}.copyCurrent(true) instead.", ClientRunContexts.class.getSimpleName(), ClientRunContexts.class.getSimpleName());
  }

  /**
   * Factory to create initialized {@link ClientRunContext} objects.
   */
  @Replace
  @Order(4900) // specify an order so that if ClientRunContextFactory and ServerRunContextFactory are on the class-path the winning one is defined (client first)
  public static class ClientRunContextFactory extends RunContextFactory {

    @Override
    public ClientRunContext empty() {
      return (ClientRunContext) super.empty()
          .withProperty(PropertyMap.PROP_CLIENT_SCOPE, true);
    }

    @Override
    public ClientRunContext copyCurrent() {
      return (ClientRunContext) super.copyCurrent()
          .withProperty(PropertyMap.PROP_CLIENT_SCOPE, true);
    }

    @Override
    protected ClientRunContext newInstance() {
      return BEANS.get(ClientRunContext.class);
    }
  }
}
