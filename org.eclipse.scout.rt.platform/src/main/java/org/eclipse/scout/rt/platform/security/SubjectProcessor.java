/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Processor to run the subsequent sequence of actions on behalf of the given {@link Subject}.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class SubjectProcessor<RESULT> implements ICallableInterceptor<RESULT> {
  protected final Subject m_subject;

  public SubjectProcessor(final Subject subject) {
    m_subject = subject;
  }

  @Override
  public RESULT intercept(final Chain<RESULT> chain) throws Exception {
    try {
      return Subject.doAs(m_subject, (PrivilegedExceptionAction<RESULT>) chain::continueChain);
    }
    catch (final PrivilegedActionException e) { // NOSONAR
      throw e.getException();
    }
  }

  @Override
  public boolean isEnabled() {
    return ObjectUtility.notEquals(m_subject, Subject.getSubject(AccessController.getContext()));
  }
}
