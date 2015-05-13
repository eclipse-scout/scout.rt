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
package org.eclipse.scout.rt.platform.context.internal;

import java.security.Principal;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IChainable;
import org.slf4j.MDC;

/**
 * Provides the {@link MDC#put(String, String)} properties {@value #SCOUT_USER_NAME}
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class SubjectLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String SUBJECT_PRINCIPAL_NAME = "subject.principal.name";

  protected final Callable<RESULT> m_next;
  protected final Subject m_subject;

  public SubjectLogCallable(final Callable<RESULT> next, final Subject subject) {
    m_next = Assertions.assertNotNull(next);
    m_subject = subject;
  }

  @Override
  public RESULT call() throws Exception {
    String name = null;
    if (m_subject != null && !m_subject.getPrincipals().isEmpty()) {
      Principal p = m_subject.getPrincipals().iterator().next();
      if (p != null) {
        name = p.getName();
      }
    }

    String oldUserName = MDC.get(SUBJECT_PRINCIPAL_NAME);
    try {
      MDC.put(SUBJECT_PRINCIPAL_NAME, name);
      //
      return m_next.call();
    }
    finally {
      if (oldUserName != null) {
        MDC.put(SUBJECT_PRINCIPAL_NAME, oldUserName);
      }
      else {
        MDC.remove(SUBJECT_PRINCIPAL_NAME);
      }
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
