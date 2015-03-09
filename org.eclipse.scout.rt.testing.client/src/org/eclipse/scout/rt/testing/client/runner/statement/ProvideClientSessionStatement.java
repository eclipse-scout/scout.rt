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
package org.eclipse.scout.rt.testing.client.runner.statement;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.shared.ISession;
import org.junit.runners.model.Statement;

/**
 * Statement to provide the calling context with a client session. The session is created based on values of the current
 * thread-context.
 * 
 * @since5.1
 */
public class ProvideClientSessionStatement extends Statement {

  protected final Statement m_next;
  private final Class<? extends ClientSessionProvider> m_providerClass;

  /**
   * Creates a statement to provide the calling context with a client session.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param providerClass
   *          class to provide client sessions.
   */
  public ProvideClientSessionStatement(final Statement next, final Class<? extends ClientSessionProvider> providerClass) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_providerClass = Assertions.assertNotNull(providerClass, "session provider class must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final IClientSession clientSession = OBJ.one(m_providerClass).provide(ClientJobInput.defaults().copy());

    final ISession oldSession = ISession.CURRENT.get();

    ISession.CURRENT.set(clientSession);
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
