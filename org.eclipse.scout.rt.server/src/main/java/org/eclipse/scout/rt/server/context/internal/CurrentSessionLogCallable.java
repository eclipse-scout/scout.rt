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
 * Provides the {@link MDC#put(String, String)} properties {@value #SESSION_USER_ID} with the username of the current
 * session.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class CurrentSessionLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String SESSION_USER_ID = "session.userId";

  protected final Callable<RESULT> m_next;

  public CurrentSessionLogCallable(final Callable<RESULT> next) {
    m_next = next;
  }

  @Override
  public RESULT call() throws Exception {
    final ISession currentSession = ISession.CURRENT.get();

    final String oldSessionUserId = MDC.get(SESSION_USER_ID);
    try {
      MDC.put(SESSION_USER_ID, currentSession != null ? currentSession.getUserId() : null);
      //
      return m_next.call();
    }
    finally {
      if (oldSessionUserId != null) {
        MDC.put(SESSION_USER_ID, oldSessionUserId);
      }
      else {
        MDC.remove(SESSION_USER_ID);
      }
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
