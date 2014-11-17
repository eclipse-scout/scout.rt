/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal;

import java.security.AccessController;

import javax.security.auth.Subject;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.RunnableWithException;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerJobService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ITransactionRunnable;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.service.SERVICES;

public class ScoutTransactionDelegate {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutTransactionDelegate.class);

  private ScoutTransaction m_scoutTransaction;

  public ScoutTransactionDelegate(ScoutTransaction scoutTransaction) {
    m_scoutTransaction = scoutTransaction;
  }

  public <T> T runInTransaction(final RunnableWithException<T> runnable, MessageContext context) {
    IServerSession serverSession = ContextHelper.getContextSession(context);
    if (serverSession == null) {
      try {
        IServerSessionFactory factory = m_scoutTransaction.sessionFactory().newInstance();
        serverSession = SessionHelper.createNewServerSession(factory);
        ContextHelper.setContextSession(context, factory, serverSession);
      }
      catch (Exception e) {
        LOG.error("Failed to create server session for transactional handler", e);
      }
    }
    if (serverSession == null) {
      LOG.error("No server session applicable. Handler is not run in transactional scope.");
      return doRun(runnable);
    }

    // run in transaction in ServerJob (transaction wrapper)
    final P_Holder<T> resultHolder = new P_Holder<T>();
    final IHolder<RuntimeException> errorHolder = new Holder<RuntimeException>(RuntimeException.class);

    Subject subject = Subject.getSubject(AccessController.getContext());

    final IServerJobFactory jobFactory = SERVICES.getService(IServerJobService.class).createJobFactory(serverSession, subject);
    ServerJob serverJob = jobFactory.create("Transactional handler", new ITransactionRunnable() {

      @Override
      public IStatus run(IProgressMonitor monitor) throws ProcessingException {
        try {
          T result = ScoutTransactionDelegate.this.doRun(runnable);
          resultHolder.setValue(result);
        }
        catch (RuntimeException e) {
          errorHolder.setValue(e);
        }
        return Status.OK_STATUS;
      }
    });
    serverJob.setSystem(true);
    serverJob.runNow(new NullProgressMonitor());

    if (errorHolder.getValue() != null) {
      throw errorHolder.getValue();
    }
    return resultHolder.getValue();
  }

  protected <T> T doRun(RunnableWithException<T> runnable) {
    try {
      return runnable.run();
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new WebServiceException(e);
    }
  }

  private static class P_Holder<T> {
    private T m_value;

    public T getValue() {
      return m_value;
    }

    public void setValue(T value) {
      m_value = value;
    }
  }
}
