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
package org.eclipse.scout.rt.platform.context;

import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Factory methods to create a new {@link RunContext} objects to propagate context like {@link Subject} or
 * {@link Locale}, and to demarcate the transaction boundary.
 * <p>
 * Usage:
 *
 * <pre>
 * RunContexts.copyCurrent()
 *   .withLocale(Locale.US)
 *   .withSubject(...)
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
 * @see RunContext
 */
public final class RunContexts {

  private RunContexts() {
  }

  /**
   * Creates an empty {@link RunContext} with all values managed by {@link RunContext} class set to their default value.
   * This method does not require to already run in a {@link RunContext}.
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
  public static RunContext empty() {
    return BEANS.get(RunContextFactory.class).empty();
  }

  /**
   * Creates a "snapshot" of the current calling context for values managed by {@link RunContext} class. This method
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
  public static RunContext copyCurrent() {
    return copyCurrent(false);
  }

  /**
   * Same as {@link RunContexts#copyCurrent()}, but less strict if not running in a {@link RunContext}.
   *
   * @param orElseEmpty
   *          indicates whether to return an empty {@link RunContext} if not running in a context yet.
   * @throws AssertionException
   *           if not running in a {@link RunContext}, and <i>orElseEmpty</i> is set to <code>false</code>.
   */
  public static RunContext copyCurrent(final boolean orElseEmpty) {
    if (RunContext.CURRENT.get() != null) {
      return BEANS.get(RunContextFactory.class).copyCurrent();
    }
    if (orElseEmpty) {
      return BEANS.get(RunContextFactory.class).empty();
    }
    return Assertions.fail("Not running in a RunContext. Use '{}.empty()' or {}.copyCurrent(true) instead.", RunContexts.class.getSimpleName(), RunContexts.class.getSimpleName());
  }

  /**
   * Factory to create initialized {@link RunContext} objects.
   */
  @ApplicationScoped
  public static class RunContextFactory {

    public RunContext empty() {
      RunContext rc = newInstance()
          .withRunMonitor(BEANS.get(RunMonitor.class))
          .withTransactionScope(TransactionScope.REQUIRED);
      rc.fillEmpty();
      return rc;
    }

    public RunContext copyCurrent() {
      final RunContext currentRunContext = Assertions.assertNotNull(RunContext.CURRENT.get());
      final RunContext newRunContext = newInstance();

      // Take a snapshot of the calling context, and apply it to the new context.
      newRunContext.fillCurrentValues();

      // Initialize the context.
      newRunContext
          .withRunMonitor(BEANS.get(RunMonitor.class))
          .withTransactionScope(TransactionScope.REQUIRED)
          .withoutTransactionMembers();

      // Register the run monitor for propagated cancellation.
      currentRunContext.getRunMonitor().registerCancellable(newRunContext.getRunMonitor());

      return newRunContext;
    }

    protected RunContext newInstance() {
      return BEANS.get(RunContext.class);
    }
  }
}
