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

import java.security.AccessController;
import java.security.Principal;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IChainable;
import org.slf4j.MDC;

/**
 * Provides the {@link MDC#put(String, String)} properties {@value #SUBJECT_PRINCIPAL_NAME} with the principal of the
 * current subject.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class CurrentSubjectLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String SUBJECT_PRINCIPAL_NAME = "subject.principal.name";

  protected final Callable<RESULT> m_next;

  public CurrentSubjectLogCallable(final Callable<RESULT> next) {
    m_next = Assertions.assertNotNull(next);
  }

  @Override
  public RESULT call() throws Exception {
    final String currentUserName = getPrincipal(Subject.getSubject(AccessController.getContext()));

    final String oldUserName = MDC.get(SUBJECT_PRINCIPAL_NAME);
    try {
      MDC.put(SUBJECT_PRINCIPAL_NAME, currentUserName);
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

  protected String getPrincipal(final Subject subject) {
    if (subject == null || subject.getPrincipals().isEmpty()) {
      return null;
    }
    final Principal principal = subject.getPrincipals().iterator().next();
    return (principal != null ? principal.getName() : null);
  }
}
