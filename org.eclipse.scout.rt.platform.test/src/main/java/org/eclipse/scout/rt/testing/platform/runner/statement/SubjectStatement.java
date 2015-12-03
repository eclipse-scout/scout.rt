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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.runners.model.Statement;

/**
 * Statement to execute the following statements under a particular user.
 *
 * @see RunWithSubject
 * @since 5.1
 */
public class SubjectStatement extends Statement {

  private final Statement m_next;
  private final Subject m_subject;

  /**
   * Creates a statement to execute the following statements under a particular user.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param annotation
   *          {@link RunWithSubject}-annotation to read the user.
   */
  public SubjectStatement(final Statement next, final RunWithSubject annotation) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");

    final String principal = (annotation != null ? annotation.value() : null);
    if (principal != null) {
      m_subject = new Subject();
      m_subject.getPrincipals().add(new SimplePrincipal(principal));
      m_subject.setReadOnly();
    }
    else {
      m_subject = null;
    }
  }

  @Override
  public void evaluate() throws Throwable {
    if (m_subject != null) {
      try {
        Subject.doAs(m_subject, new PrivilegedExceptionAction<Void>() {

          @Override
          public Void run() throws Exception {
            try {
              m_next.evaluate();
              return null;
            }
            catch (final Exception | Error e) {
              throw e;
            }
            catch (final Throwable e) {
              throw new Error(e);
            }
          }
        });
      }
      catch (PrivilegedActionException e) {
        throw e.getCause();
      }
    }
    else {
      m_next.evaluate();
    }
  }
}
