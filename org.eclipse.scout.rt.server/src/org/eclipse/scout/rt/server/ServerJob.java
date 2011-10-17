/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.transaction.BasicTransaction;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Perform a transaction on a {@link IServerSession}<br>
 */
public abstract class ServerJob extends JobEx implements IServerSessionProvider {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerJob.class);

  private IServerSession m_serverSession;
  private Subject m_subject;

  /**
   * Perform a transaction on a {@link IServerSession} within a security {@link Subject} (optional)<br>
   * 
   * @param serverSession
   *          must not be null
   */
  public ServerJob(String name, IServerSession serverSession) {
    this(name, serverSession, null);
  }

  /**
   * Perform a transaction on a {@link IServerSession} within a security {@link Subject} (optional)<br>
   * 
   * @param serverSession
   *          must not be null
   * @param subject
   *          (optional) the job is run inside a {@link Subject#doAs(Subject, java.security.PrivilegedAction)} section
   */
  public ServerJob(String name, IServerSession serverSession, Subject subject) {
    super(name);
    if (serverSession == null) {
      throw new IllegalArgumentException("serverSession is null");
    }
    m_serverSession = serverSession;
    m_subject = subject;
    setProperty(ScoutTexts.JOB_PROPERTY_NAME, m_serverSession.getNlsTexts());
  }

  /**
   * {@link ServerJob}s belong to the family of type {@link ServerJob}.class
   */
  @Override
  public boolean belongsTo(Object family) {
    if (family == ServerJob.class) {
      return true;
    }
    return false;
  }

  @Override
  public IServerSession getServerSession() {
    return m_serverSession;
  }

  public Subject getSubject() {
    return m_subject;
  }

  /**
   * The subject can only be modified as long as the job is not scheduled
   */
  public void setSubject(Subject subject) {
    if (getState() != NONE) {
      throw new IllegalStateException("This job is already scheduled/running");
    }
    m_subject = subject;
  }

  @Override
  public boolean shouldSchedule() {
    if (getServerSession() != null && getServerSession().isWebSession()) {
      runNow(new NullProgressMonitor());
      return false;
    }
    else {
      return super.shouldSchedule();
    }
  }

  /**
   * <p>
   * All subsequent calls within {@link ServerJob#runTransaction(IProgressMonitor))} have the {@link IServerSession} set
   * in their thread context. After execution completes, {@link IServerSession} is cleared from the thread context.
   * </p>
   * <p>
   * By calling this method, a new transaction on {@link IServerSession} is created and automatically comitted after
   * successful completion.
   * </p>
   * 
   * @param monitor
   */
  @Override
  public final IStatus runNow(IProgressMonitor monitor) {
    return super.runNow(monitor);
  }

  @Override
  protected final IStatus run(final IProgressMonitor monitor) {
    try {
      if (m_subject != null) {
        try {
          return Subject.doAs(
              m_subject,
              new PrivilegedExceptionAction<IStatus>() {
                @Override
                public IStatus run() throws Exception {
                  return runTransactionWrapper(monitor);
                }
              }
              );
        }
        catch (PrivilegedActionException e) {
          Throwable t = e.getCause();
          if (t instanceof ProcessingException) {
            return ((ProcessingException) t).getStatus();
          }
          else {
            return new ProcessingStatus(t.getMessage(), t, 0, ProcessingStatus.ERROR);
          }
        }
      }
      else {
        return runTransactionWrapper(monitor);
      }
    }
    catch (Throwable t) {
      if (t instanceof ProcessingException) {
        return ((ProcessingException) t).getStatus();
      }
      else {
        return new ProcessingStatus(t.getMessage(), t, 0, ProcessingStatus.ERROR);
      }
    }
  }

  private IStatus runTransactionWrapper(IProgressMonitor monitor) throws Exception {
    ITransaction transaction = createNewTransaction();
    Map<Class, Object> backup = ThreadContext.backup();
    Locale oldLocale = LocaleThreadLocal.get();
    NlsLocale oldNlsLocale = NlsLocale.getDefault();
    try {
      ThreadContext.put(m_serverSession);
      ThreadContext.put(transaction);
      LocaleThreadLocal.set(m_serverSession.getLocale());
      NlsLocale.setThreadDefault(m_serverSession.getNlsLocale());
      //
      IStatus status = runTransaction(monitor);
      if (status == null) {
        status = Status.OK_STATUS;
      }
      return status;
    }
    catch (Throwable t) {
      if (t instanceof UndeclaredThrowableException) {
        Throwable test = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
        if (test != null) {
          t = test;
        }
      }
      if (t.getCause() instanceof ProcessingException) {
        t = t.getCause();
      }
      transaction.addFailure(t);
      String contextMsg = "Identity=" + ServerJob.getIdentity() + ", Job=" + getName();
      ProcessingException pe;
      if (t instanceof ProcessingException) {
        pe = (ProcessingException) t;
        pe.addContextMessage(contextMsg);
      }
      else {
        pe = new ProcessingException(contextMsg, t);
      }
      throw pe;
    }
    finally {
      if (transaction.hasFailures()) {
        // xa rollback
        transaction.rollback();
      }
      else {
        // xa commit
        if (transaction.commitPhase1()) {
          transaction.commitPhase2();
        }
      }
      // xa release
      try {
        transaction.release();
      }
      catch (Throwable t) {
        LOG.warn(null, t);
      }
      // restore thread context
      try {
        ThreadContext.restore(backup);
      }
      catch (Throwable t) {
        LOG.warn(null, t);
      }
      LocaleThreadLocal.set(oldLocale);
      NlsLocale.setThreadDefault(oldNlsLocale);
    }
  }

  protected abstract IStatus runTransaction(IProgressMonitor monitor) throws Exception;

  /**
   * @return {@link IClientSession} if the current job is a {@link IClientSessionProvider}
   */
  public static final IServerSession getCurrentSession() {
    return getCurrentSession(IServerSession.class);
  }

  /**
   * @return {@link IClientSession} if the current job is a {@link IClientSessionProvider}
   */
  @SuppressWarnings("unchecked")
  public static final <T extends IServerSession> T getCurrentSession(Class<T> type) {
    // try current job
    Job job = getJobManager().currentJob();
    if (job instanceof IServerSessionProvider) {
      IServerSession s = ((IServerSessionProvider) job).getServerSession();
      if (s != null && type.isAssignableFrom(s.getClass())) {
        return (T) s;
      }
    }
    // try ThreadContext
    return ThreadContext.get(type);
  }

  /**
   * Convenience to obtain name of current subjects first principal
   */
  public static String getIdentity() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject != null) {
      for (Principal p : subject.getPrincipals()) {
        return p.getName();
      }
    }
    return "anonymous";
  }

  protected ITransaction createNewTransaction() {
    return new BasicTransaction();
  }

}
