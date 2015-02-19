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
package org.eclipse.scout.rt.client.job;

import java.util.concurrent.Callable;

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
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job that operates on a {@link IClientSession} and provides your executing code with the client-context.
 * Jobs of this type return a result to the caller and run on behalf of the the JVM-wide {@link JobManager}.
 * <p/>
 * <strong>If interacting with the client-model, use {@link ModelJobWithResult}.</strong>
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in parallel among other client jobs;</li>
 * <li>return a result to the caller;</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IJob#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @see ClientJob
 * @see JobManager
 * @since 5.1
 */
public abstract class ClientJobWithResult<RESULT> extends Job<RESULT> implements IClientSessionProvider {

  @Internal
  protected final IClientSession m_clientSession;

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param clientSession
   *          the {@link IClientSession} which this job belongs to; must not be <code>null</code>.
   */
  public ClientJobWithResult(final String name, final IClientSession clientSession) {
    super(name);
    m_clientSession = Assertions.assertNotNull(clientSession);
  }

  @Override
  public final IClientSession getClientSession() {
    return m_clientSession;
  }

  @Override
  protected Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, m_clientSession.getTexts());
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, NlsLocale.CURRENT, m_clientSession.getLocale());
    final Callable<RESULT> c1 = new InitThreadLocalCallable<>(c2, ISession.CURRENT, m_clientSession);
    final Callable<RESULT> cHead = super.interceptCallable(c1);

    return cHead;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("jobName", getName());
    builder.ref("jobManager", m_jobManager);
    builder.ref("clientSession", m_clientSession);
    return builder.toString();
  }
}
