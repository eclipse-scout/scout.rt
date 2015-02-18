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
package org.eclipse.scout.rt.server.job;

import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.InitThreadLocalCallable;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.IServerSessionProvider;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletRoundtrip;
import org.eclipse.scout.rt.server.job.interceptor.SubjectCallable;
import org.eclipse.scout.rt.server.job.interceptor.TwoPhaseTransactionBoundaryCallable;
import org.eclipse.scout.rt.server.transaction.BasicTransaction;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job that operates on a {@link IServerSession} and provides your executing code with the server-context.
 * Jobs of this type return a result to the caller and run on behalf of the the JVM-wide {@link JobManager}.
 * <p/>
 * <strong>Every time that you run a {@link ServerJobWithResult}, a transaction is started and committed upon successful
 * completion or rolled back otherwise.</strong>
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in parallel among other server jobs;</li>
 * <li>run in a new transaction;</li>
 * <li>return a result to the caller;</li>
 * <li>are executed on behalf of a {@link Subject};</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IJob#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT}, {@link HttpServletRoundtrip#CURRENT_HTTP_SERVLET_REQUEST},
 * {@link HttpServletRoundtrip#CURRENT_HTTP_SERVLET_RESPONSE};</li>
 * </ul>
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @see ServerJob
 * @see JobManager
 * @since 5.1
 */
public abstract class ServerJobWithResult<RESULT> extends Job<RESULT> implements IServerSessionProvider {

  @Internal
  protected final IServerSession m_serverSession;
  @Internal
  protected Subject m_subject;
  @Internal
  protected long m_transactionId;

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   */
  public ServerJobWithResult(final String name, final IServerSession serverSession) {
    this(name, serverSession, null);
  }

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   * @param subject
   *          {@link Subject} of behalf of which this job is to be executed.
   */
  public ServerJobWithResult(final String name, final IServerSession serverSession, final Subject subject) {
    this(name, serverSession, subject, ITransaction.TX_ZERO_ID);
  }

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   * @param subject
   *          {@link Subject} of behalf of which this job is to be executed.
   * @param transactionId
   *          unique transaction <code>id</code> among the {@link IServerSession} or {@link ITransaction#TX_ZERO_ID} if
   *          not to be registered within the {@link IServerSession}; is primarily used to identify the transaction if
   *          the user likes to cancel a transaction.
   */
  public ServerJobWithResult(final String name, final IServerSession serverSession, final Subject subject, final long transactionId) {
    super(name);
    m_serverSession = Assertions.assertNotNull(serverSession);
    m_subject = subject;
    m_transactionId = transactionId;
  }

  @Override
  public final IServerSession getServerSession() {
    return m_serverSession;
  }

  /**
   * @return {@link Subject} of behalf of which this job is to be executed; is <code>null</code> if not executed on
   *         behalf of a subject.
   */
  public final Subject getSubject() {
    return m_subject;
  }

  /**
   * @return unique transaction <code>id</code> among the {@link IServerSession} or {@link ITransaction#TX_ZERO_ID} if
   *         not to be registered within the {@link IServerSession}; is primarily used to identify the transaction if
   *         the user likes to cancel a transaction.
   */
  public final long getTransactionId() {
    return m_transactionId;
  }

  @Override
  protected Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final ITransaction tx = createTransaction(m_transactionId);

    final Callable<RESULT> c8 = new TwoPhaseTransactionBoundaryCallable<>(next, tx);
    final Callable<RESULT> c7 = new SubjectCallable<>(c8, m_subject);
    final Callable<RESULT> c6 = new InitThreadLocalCallable<>(c7, ITransaction.CURRENT, tx);
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(c6, ScoutTexts.CURRENT, m_serverSession.getTexts());
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, NlsLocale.CURRENT, m_serverSession.getLocale());
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, ISession.CURRENT, m_serverSession);
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, HttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get());
    final Callable<RESULT> c1 = new InitThreadLocalCallable<>(c2, HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, HttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get());
    final Callable<RESULT> cHead = super.interceptCallable(c1);

    return cHead;
  }

  /**
   * Method invoked to create a {@link ITransaction}.
   *
   * @param transactionId
   *          unique transaction <code>id</code> among the {@link IServerSession} or {@link ITransaction#TX_ZERO_ID} if
   *          not to be registered within the {@link IServerSession}; is primarily used to identify the transaction if
   *          the user likes to cancel a transaction.
   * @return {@link ITransaction}; must not be <code>null</code>.
   */
  protected ITransaction createTransaction(final long transactionId) {
    return new BasicTransaction(transactionId);
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("jobName", getName());
    builder.attr("subjectName", getSubject());
    builder.attr("transactionId", getTransactionId());
    builder.ref("serverSession", m_serverSession);
    builder.ref("jobManager", m_jobManager);
    return builder.toString();
  }
}
