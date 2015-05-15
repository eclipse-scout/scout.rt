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
import org.eclipse.scout.rt.shared.ISession;
import org.slf4j.MDC;

/**
 * Provides the {@link MDC#put(String, String)} properties {@value #SCOUT_USER_NAME} with the username of the current
 * session.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class CurrentSessionLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String SCOUT_USER_NAME = "scout.user.name";

  protected final Callable<RESULT> m_next;

  public CurrentSessionLogCallable(final Callable<RESULT> next) {
    m_next = next;
  }

  @Override
  public RESULT call() throws Exception {
    final ISession currentSession = ISession.CURRENT.get();

    final String oldUserName = MDC.get(SCOUT_USER_NAME);
    try {
      MDC.put(SCOUT_USER_NAME, currentSession != null ? currentSession.getUserId() : null);
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
