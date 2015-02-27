/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.job.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.internal.JobFuture;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.commons.job.internal.callable.InitThreadLocalCallable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.internal.callable.TwoPhaseTransactionBoundaryCallable;
import org.eclipse.scout.rt.server.transaction.BasicTransaction;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Default implementation of {@link IServerJobManager}.
 *
 * @see IServerJobManager
 * @since 5.1
 */
public class ServerJobManager extends JobManager<ServerJobInput> implements IServerJobManager {

  public ServerJobManager() {
    super("scout-server-thread");
  }

  @Override
  protected void validateInput(final ServerJobInput input) {
    Assertions.assertNotNull(input, "ServerJobInput must not be null");
    Assertions.assertNotNull(input.getSession(), "ServerSession must not be null");
  }

  @Override
  protected ServerJobInput createDefaultJobInput() {
    return ServerJobInput.defaults();
  }

  @Override
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> next, final ServerJobInput input) {
    final ITransaction tx = Assertions.assertNotNull(createTransaction());

    final Callable<RESULT> c8 = new TwoPhaseTransactionBoundaryCallable<>(next, tx, input);
    final Callable<RESULT> c7 = new InitThreadLocalCallable<>(c8, ITransaction.CURRENT, tx);
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, ScoutTexts.CURRENT, input.getSession().getTexts());
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, UserAgent.CURRENT, input.getUserAgent());
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, ISession.CURRENT, input.getSession());
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, input.getServletResponse());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, input.getServletRequest());
    final Callable<RESULT> c1 = super.interceptCallable(c2, input);

    return c1;
  }

  @Override
  protected <RESULT> JobFuture<RESULT> interceptFuture(final JobFuture<RESULT> future) {
    return new ServerJobFuture<>(future, (ServerJobInput) future.getInput());
  }

  /**
   * Method invoked to create a {@link ITransaction}.
   *
   * @return {@link ITransaction}; must not be <code>null</code>.
   */
  protected ITransaction createTransaction() {
    return new BasicTransaction();
  }

  @Override
  public boolean cancel(final long id, final IServerSession serverSession) {
    final Set<Boolean> status = new HashSet<>();

    visit(new IFutureVisitor() {

      @Override
      public boolean visit(final Future<?> future) {
        final ServerJobInput input = ((ServerJobFuture) future).getInput();
        if (serverSession.equals(input.getSession()) && id == input.getId()) {
          status.add(future.cancel(true));
        }
        return true;
      }
    });

    return Collections.singleton(true).equals(status);
  }
}
