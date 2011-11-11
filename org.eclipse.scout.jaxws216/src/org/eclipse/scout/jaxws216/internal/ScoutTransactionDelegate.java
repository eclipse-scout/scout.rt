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
package org.eclipse.scout.jaxws216.internal;

import java.security.AccessController;

import javax.security.auth.Subject;
import javax.xml.ws.WebServiceException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.RunnableWithException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;

public class ScoutTransactionDelegate {

  public <T> T runInTransaction(final RunnableWithException<T> runnable, IServerSession serverSession) {
    if (serverSession == null) {
      return doRun(runnable);
    }

    // run in transaction in ServerJob (transaction wrapper)
    final P_Holder<T> resultHolder = new P_Holder<T>();
    final IHolder<RuntimeException> errorHolder = new Holder<RuntimeException>(RuntimeException.class);

    // this call must run on behalf of the subject of the current access context.
    // If an authentication handler is in front of the handler chain, the subject in the context represent the authenticated user.
    Subject subject = Subject.getSubject(AccessController.getContext());
    ServerJob serverJob = new ServerJob("Transactional handler", serverSession, subject) {

      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        try {
          T result = ScoutTransactionDelegate.this.doRun(runnable);
          resultHolder.setValue(result);
        }
        catch (RuntimeException e) {
          errorHolder.setValue(e);
        }
        return Status.OK_STATUS;
      }
    };
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
