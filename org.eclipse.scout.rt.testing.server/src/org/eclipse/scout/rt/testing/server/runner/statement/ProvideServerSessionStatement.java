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
package org.eclipse.scout.rt.testing.server.runner.statement;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.runners.model.Statement;

/**
 * Statement to provide the calling context with a server session. The session is created based on values of the current
 * thread-context.
 *
 * @since5.1
 */
public class ProvideServerSessionStatement extends Statement {

  protected final Statement m_next;
  private final Class<? extends ServerSessionProvider> m_providerClass;

  /**
   * Creates a statement to provide the calling context with a server session.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param providerClass
   *          class to provide server sessions.
   */
  public ProvideServerSessionStatement(final Statement next, final Class<? extends ServerSessionProvider> providerClass) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_providerClass = Assertions.assertNotNull(providerClass, "session provider class must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    Assertions.assertNotNull(Subject.getSubject(AccessController.getContext()), "Subject must not be null. Use the annotation '%s' to execute your test under a particular user. ", RunWithSubject.class.getSimpleName());

    final IServerSession serverSession = OBJ.get(m_providerClass).provide(ServerRunContexts.copyCurrent());

    final ISession oldSession = ISession.CURRENT.get();

    ISession.CURRENT.set(serverSession);
    try {
      m_next.evaluate();
    }
    finally {
      if (oldSession == null) {
        ISession.CURRENT.remove();
      }
      else {
        ISession.CURRENT.set(oldSession);
      }
    }
  }
}
