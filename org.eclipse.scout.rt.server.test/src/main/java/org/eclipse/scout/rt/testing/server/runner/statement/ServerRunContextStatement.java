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
package org.eclipse.scout.rt.testing.server.runner.statement;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements within a <code>ServerRunContext</code>.
 *
 * @see RunWithServerSession
 * @since 5.1
 */
public class ServerRunContextStatement extends Statement {

  private final Statement m_next;
  private final RunWithServerSession m_serverSessionAnnotation;

  public ServerRunContextStatement(final Statement next, final RunWithServerSession serverSessionAnnotation) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_serverSessionAnnotation = serverSessionAnnotation;
  }

  @Override
  public void evaluate() throws Throwable {
    if (m_serverSessionAnnotation == null) {
      m_next.evaluate();
    }
    else {
      evaluateWithServerRunContext();
    }
  }

  private void evaluateWithServerRunContext() throws Throwable {
    final Subject currentSubject = Subject.getSubject(AccessController.getContext());
    if (currentSubject == null) {
      Assertions.fail("Subject must not be null. Use the annotation '{}' to execute your test under a particular user. ", RunWithSubject.class.getSimpleName());
    }

    UserAgent userAgent = UserAgents.createDefault();

    Class<? extends ISession> sessionClass = m_serverSessionAnnotation.value();
    IBean<? extends ISession> sessionBean = BEANS.getBeanManager().uniqueBean(sessionClass);
    if (sessionBean != null) {
      sessionClass = sessionBean.getBeanClazz();
    }
    final IBean serverSessionBean = BEANS.getBeanManager().registerBean(new BeanMetaData(sessionClass).withOrder(-Long.MAX_VALUE));
    try {
      // Obtain the server session for the given subject. Depending on the session provider, a new session is created or a cached session returned.
      final IServerSession serverSession = BEANS.get(m_serverSessionAnnotation.provider()).provide(
          ServerRunContexts.copyCurrent()
              .withSubject(currentSubject)
              .withUserAgent(userAgent));

      // Run the test on behalf of a ServerRunContext.
      final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);
      ServerRunContexts.copyCurrent()
          .withSession(serverSession)
          .withSubject(currentSubject) // set the test subject explicitly in case it is different to the session subject
          .withUserAgent(userAgent)
          .run(invoker);
      invoker.throwOnError();
    }
    finally {
      BEANS.getBeanManager().unregisterBean(serverSessionBean);
    }
  }
}
