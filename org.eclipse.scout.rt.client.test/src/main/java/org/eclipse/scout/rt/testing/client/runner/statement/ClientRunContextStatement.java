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
package org.eclipse.scout.rt.testing.client.runner.statement;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.RegisterBeanStatement;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements within a <code>ClientRunContext</code>.
 *
 * @see RunWithClientSession
 * @since 5.1
 */
public class ClientRunContextStatement extends Statement {

  private final Statement m_next;
  private final RunWithClientSession m_clientSessionAnnotation;

  public ClientRunContextStatement(final Statement next, final RunWithClientSession clientSessionAnnotation) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_clientSessionAnnotation = clientSessionAnnotation;
  }

  @Override
  public void evaluate() throws Throwable {
    if (m_clientSessionAnnotation == null) {
      m_next.evaluate();
    }
    else {
      final Class<? extends ISession> clientSessionClass = m_clientSessionAnnotation.value();

      new RegisterBeanStatement(new Statement() {

        @Override
        public void evaluate() throws Throwable {
          final Class<? extends ClientSessionProvider> clientSessionProvider = m_clientSessionAnnotation.provider();
          final Subject subject =
              Assertions.assertNotNull(Subject.getSubject(AccessController.getContext()), "Subject must not be null. Use the annotation '%s' to execute your test under a particular user. ", RunWithSubject.class.getSimpleName());

          // Obtain the client session for the given subject. Depending on the session provider, a new session is created or a cached session returned.
          final IClientSession clientSession = BEANS.get(clientSessionProvider).provide(ClientRunContexts.copyCurrent().withSubject(subject));

          // Run the test in a new ClientRunContext. The subject is set explicitly in case a different subject is defined on the session.
          ClientRunContexts.copyCurrent().withSession(clientSession, true).withSubject(subject).run(new IRunnable() {

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
      }, new BeanMetaData(clientSessionClass).withOrder(-Long.MAX_VALUE)).evaluate();
    }
  }
}
