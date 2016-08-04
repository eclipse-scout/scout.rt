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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Factory methods to create new {@link ServerRunContext} objects to propagate server-side state and to control
 * {@link TransactionScope}.
 * <p/>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * server state among different threads, or allows temporary state changes to be done for the time of executing some
 * code.
 * <p/>
 * A transaction scope controls in which transaction to run executables. By default, a new transaction is started, and
 * committed or rolled back upon completion.
 * <p/>
 * Usage:
 *
 * <pre>
 * ServerRunContexts.copyCurrent().withLocale(Locale.US).withSubject(...).withSession(...).run(new IRunnable() {
 *
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
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
   * Creates an empty {@link ServerRunContext}.
   * <p>
   * <strong>RunMonitor</strong><br>
   * a new {@link RunMonitor} is created. However, even if there is a current {@link RunMonitor}, it is NOT registered
   * as child monitor, meaning that it will not be cancelled once the current {@link RunMonitor} is cancelled.
   * <p>
   * <strong>TransactionScope</strong><br>
   * {@link TransactionScope#REQUIRES_NEW}.
   */
  public static ServerRunContext empty() {
    final ServerRunContext runContext = BEANS.get(ServerRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling server context.
   * <p>
   * <strong>RunMonitor</strong><br>
   * a new {@link RunMonitor} is created, and if the current calling context contains a {@link RunMonitor}, it is also
   * registered within that {@link RunMonitor}. That makes the <i>returned</i> {@link RunContext} to be cancelled as
   * well once the current calling {@link RunContext} is cancelled, but DOES NOT cancel the current calling
   * {@link RunContext} if the <i>returned</i> {@link RunContext} is cancelled.
   * <p>
   * <strong>Transaction</strong><br>
   * the {@link RunContext} returned contains the transaction of the current calling context. However, by default,
   * {@link TransactionScope} is set to {@link TransactionScope#REQUIRES_NEW}, meaning that when executing the runnable,
   * a new transaction is created, and therefore committed or rolled back upon completion. To work on behalf of the
   * current transaction, set the scope to {@link TransactionScope#MANDATORY}.
   * <p>
   * <strong>TransactionScope</strong><br>
   * {@link TransactionScope#REQUIRES_NEW}.
   */
  public static ServerRunContext copyCurrent() {
    final ServerRunContext runContext = BEANS.get(ServerRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
