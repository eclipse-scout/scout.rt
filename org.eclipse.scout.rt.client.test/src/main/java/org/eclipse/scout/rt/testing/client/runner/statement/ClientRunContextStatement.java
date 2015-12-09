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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
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
      evaluateWithClientRunContext();
    }
  }

  private void evaluateWithClientRunContext() throws Throwable {
    final Subject currentSubject = Subject.getSubject(AccessController.getContext());
    if (currentSubject == null) {
      Assertions.fail("Subject must not be null. Use the annotation '%s' to execute your test under a particular user. ", RunWithSubject.class.getSimpleName());
    }

    final IBean clientSessionBean = BEANS.getBeanManager().registerBean(new BeanMetaData(m_clientSessionAnnotation.value()).withOrder(-Long.MAX_VALUE));
    try {
      // Obtain the client session for the given subject. Depending on the session provider, a new session is created or a cached session returned.
      final IClientSession clientSession = BEANS.get(m_clientSessionAnnotation.provider()).provide(ClientRunContexts.copyCurrent().withSubject(currentSubject));

      // Run the test on behalf of a ClientRunContext.
      final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);
      ClientRunContexts.copyCurrent()
          .withSession(clientSession, true)
          .withSubject(currentSubject) // set the test subject explicitly in case it is different to the session subject
          .run(invoker);
      invoker.throwOnError();
    }
    finally {
      BEANS.getBeanManager().unregisterBean(clientSessionBean);
    }
  }
}
