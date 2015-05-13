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
package org.eclipse.scout.rt.server.context.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.rt.server.IServerSession;
import org.slf4j.MDC;

/**
 * Provides the {@link MDC#put(String, String)} properties {@value #SCOUT_USER_NAME}
 */
public class ServerSessionLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String SCOUT_USER_NAME = "scout.user.name";

  protected final Callable<RESULT> m_next;
  protected final IServerSession m_session;

  public ServerSessionLogCallable(Callable<RESULT> next, IServerSession session) {
    m_next = next;
    m_session = session;
  }

  @Override
  public RESULT call() throws Exception {
    String oldUserName = MDC.get(SCOUT_USER_NAME);
    try {
      MDC.put(SCOUT_USER_NAME, m_session != null ? m_session.getUserId() : null);
      //
      return m_next.call();
    }
    finally {
      if (oldUserName != null) {
        MDC.put(SCOUT_USER_NAME, oldUserName);
      }
      else {
        MDC.remove(SCOUT_USER_NAME);
      }
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }

}
