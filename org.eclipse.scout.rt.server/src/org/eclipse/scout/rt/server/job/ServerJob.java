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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.ThreadLocalInitializer;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.IServerSessionProvider;
import org.eclipse.scout.rt.server.commons.servletfilter.IServlet;
import org.eclipse.scout.rt.server.job.interceptor.PrivilegedActionRunner;
import org.eclipse.scout.rt.server.job.interceptor.TransactionDemarcator;
import org.eclipse.scout.rt.server.transaction.BasicTransaction;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job that operates on a {@link IServerSession} and provides your executing code with the server-context, transaction
 * boundary demarcation and the possibility for privileged execution. Jobs of this type run on behalf of the the
 * JVM-wide {@link JobManager}.
 * <p/>
 * <strong>Every time that you run a {@link ServerJob}, a transaction (XA-style) is started and committed upon
 * successful completion or rolled back otherwise.</strong>
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>runs in parallel among other {@link ServerJob}s;</li>
 * <li>operates on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to ThreadLocals: {@link IJob#CURRENT}, {@link JobContext#CURRENT},
 * {@link ISession#CURRENT}, {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 *
 * @param <R>
 *          the result type of the job's computation; use {@link Void} in combination with {@link #onRunVoid()} if this
 *          job does not return a result.
 * @see IJob
 * @see Job
 * @see JobManager
 * @since 5.1
 */
public class ServerJob<R> extends Job<R> implements IServerSessionProvider {

  protected final IServerSession m_serverSession;
  protected Subject m_subject;
  protected long m_transactionId;

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>; must not be
   *          unique.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to.
   */
  public ServerJob(final String name, final IServerSession serverSession) {
    this(name, serverSession, null);
  }

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>; must not be
   *          unique.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to.
   * @param subject
   *          {@link Subject} of behalf of which this job is to be executed; use <code>null</code> if not to be executed
   *          in privileged mode.
   */
  public ServerJob(final String name, final IServerSession serverSession, final Subject subject) {
    this(name, serverSession, subject, ITransaction.TX_ZERO_ID);
  }

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>; must not be
   *          unique.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to.
   * @param subject
   *          {@link Subject} of behalf of which this job is to be executed; use <code>null</code> if not to be executed
   *          in privileged mode.
   * @param transactionId
   *          unique transaction <code>id</code> among the {@link IServerSession} or {@link ITransaction#TX_ZERO_ID} if
   *          not to be registered within the {@link IServerSession}; is primarily used to identify the transaction if
   *          the user likes to cancel a transaction.
   */
  public ServerJob(final String name, final IServerSession serverSession, final Subject subject, final long transactionId) {
    super(name);
    m_serverSession = Assertions.assertNotNull(serverSession);
    m_subject = subject;
    m_transactionId = transactionId;
  }

  @Override
  public IServerSession getServerSession() {
    return m_serverSession;
  }

  /**
   * @return {@link Subject} of behalf of which this this job is executed; is <code>null</code>if not executed in
   *         privileged mode.
   */
  public Subject getSubject() {
    return m_subject;
  }

  /**
   * @return unique transaction <code>id</code> among the {@link IServerSession} or {@link ITransaction#TX_ZERO_ID} if
   *         not to be registered within the {@link IServerSession}; is primarily used to identify the transaction if
   *         the user likes to cancel a transaction.
   */
  public long getTransactionId() {
    return m_transactionId;
  }

  @Override
  protected Callable<R> interceptCallable(final Callable<R> next) {
    final ITransaction tx = createTransaction(m_transactionId);

    final Callable<R> p8 = new TransactionDemarcator<>(next, tx);
    final Callable<R> p7 = new PrivilegedActionRunner<>(p8, m_subject);
    final Callable<R> p6 = new ThreadLocalInitializer<>(p7, ITransaction.CURRENT, tx);
    final Callable<R> p5 = new ThreadLocalInitializer<>(p6, ScoutTexts.CURRENT, m_serverSession.getTexts());
    final Callable<R> p4 = new ThreadLocalInitializer<>(p5, NlsLocale.CURRENT, m_serverSession.getLocale());
    final Callable<R> p3 = new ThreadLocalInitializer<>(p4, ISession.CURRENT, m_serverSession);
    final Callable<R> p2 = new ThreadLocalInitializer<>(p3, IServlet.CURRENT_HTTP_SERVLET_RESPONSE, IServlet.CURRENT_HTTP_SERVLET_RESPONSE.get());
    final Callable<R> p1 = new ThreadLocalInitializer<>(p2, IServlet.CURRENT_HTTP_SERVLET_REQUEST, IServlet.CURRENT_HTTP_SERVLET_REQUEST.get());
    final Callable<R> head = super.interceptCallable(p1);

    return head;
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

  public static ServerJob<?> get() {
    final IJob<?> currentJob = CURRENT.get();
    return (ServerJob<?>) (currentJob instanceof ServerJob ? currentJob : null);
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
}
