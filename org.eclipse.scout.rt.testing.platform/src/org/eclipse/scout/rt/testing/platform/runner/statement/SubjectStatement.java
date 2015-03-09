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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.runners.model.Statement;

/**
 * Statement to execute the following statements under a particular user.
 * 
 * @since5.1
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
    m_subject = new Subject();
    m_subject.getPrincipals().add(new SimplePrincipal(StringUtility.nvl(principal, "anonymous")));
    m_subject.setReadOnly();
  }

  @Override
  public void evaluate() throws Throwable {
    final Holder<Throwable> throwable = new Holder<>();
    Subject.doAs(m_subject, new PrivilegedExceptionAction<Void>() {

      @Override
      public Void run() throws Exception {
        try {
          m_next.evaluate();
        }
        catch (final Throwable t) {
          throwable.setValue(t);
        }
        return null;
      }
    });

    if (throwable.getValue() != null) {
      throw throwable.getValue();
    }
  }
}
