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
package org.eclipse.scout.rt.testing.server.runner.statement;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.RegisterBeanStatement;
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
      final Class<? extends ISession> serverSessionClass = m_serverSessionAnnotation.value();

      new RegisterBeanStatement(new Statement() {

        @Override
        public void evaluate() throws Throwable {
          final Class<? extends ServerSessionProvider> serverSessionProvider = m_serverSessionAnnotation.provider();
          final Subject subject =
              Assertions.assertNotNull(Subject.getSubject(AccessController.getContext()), "Subject must not be null. Use the annotation '%s' to execute your test under a particular user. ", RunWithSubject.class.getSimpleName());

          // Obtain the server session for the given subject. Depending on the session provider, a new session is created or a cached session returned.
          final IServerSession serverSession = BEANS.get(serverSessionProvider).provide(ServerRunContexts.copyCurrent().withSubject(subject));

          // Run the test in a new ServerRunContext. The subject is set explicitly in case a different subject is defined on the session.
          ServerRunContexts.copyCurrent().withSession(serverSession).withSubject(subject).run(new IRunnable() {

            @Override
            public void run() throws Exception {
              try {
                m_next.evaluate();
              }
              catch (final Exception | Error e) {
                throw e;
              }
              catch (final Throwable e) {
                throw new Error(e);
              }
            }
          }, BEANS.get(ThrowableTranslator.class));
        }
      }, new BeanMetaData(serverSessionClass).withOrder(-Long.MAX_VALUE)).evaluate();
    }
  }
}
