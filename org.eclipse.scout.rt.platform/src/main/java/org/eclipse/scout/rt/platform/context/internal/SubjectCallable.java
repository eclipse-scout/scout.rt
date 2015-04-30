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
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IChainable;

/**
 * Processor to run the subsequent sequence of actions on behalf of the given {@link Subject}.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class SubjectCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {

  protected final Callable<RESULT> m_next;
  protected final Subject m_subject;

  /**
   * Creates a processor to run the subsequent sequence of actions on behalf of the given {@link Subject}.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param subject
   *          {@link Subject} on behalf of which to run the following processors; use <code>null</code> if not to be run
   *          in privileged mode.
   */
  public SubjectCallable(final Callable<RESULT> next, final Subject subject) {
    m_next = Assertions.assertNotNull(next);
    m_subject = subject;
  }

  @Override
  public RESULT call() throws Exception {
    if (m_subject == null || m_subject.equals(Subject.getSubject(AccessController.getContext()))) {
      return m_next.call();
    }
    else {
      try {
        return Subject.doAs(m_subject, new PrivilegedExceptionAction<RESULT>() {

          @Override
          public RESULT run() throws Exception {
            return m_next.call();
          }
        });
      }
      catch (final PrivilegedActionException e) {
        throw e.getException();
      }
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
