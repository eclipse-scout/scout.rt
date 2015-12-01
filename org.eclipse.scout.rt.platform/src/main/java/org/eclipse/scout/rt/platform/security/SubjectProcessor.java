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
package org.eclipse.scout.rt.platform.security;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.chain.IInvocationInterceptor;
import org.eclipse.scout.rt.platform.chain.InvocationChain;
import org.eclipse.scout.rt.platform.chain.InvocationChain.Chain;
import org.eclipse.scout.rt.platform.util.CompareUtility;

/**
 * Processor to run the subsequent sequence of actions on behalf of the given {@link Subject}.
 * <p>
 * Instances of this class are to be added to a {@link InvocationChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class SubjectProcessor<RESULT> implements IInvocationInterceptor<RESULT> {
  protected final Subject m_subject;

  public SubjectProcessor(final Subject subject) {
    m_subject = subject;
  }

  @Override
  public RESULT intercept(final Chain<RESULT> chain) throws Exception {
    try {
      return Subject.doAs(m_subject, new PrivilegedExceptionAction<RESULT>() {

        @Override
        public RESULT run() throws Exception {
          return chain.continueChain();
        }
      });
    }
    catch (final PrivilegedActionException e) {
      throw e.getException();
    }
  }

  @Override
  public boolean isEnabled() {
    return CompareUtility.notEquals(m_subject, Subject.getSubject(AccessController.getContext()));
  }
}
