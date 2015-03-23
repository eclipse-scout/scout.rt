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
package org.eclipse.scout.rt.testing.server.runner;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.ProcessingRuntimeException;
import org.eclipse.scout.rt.testing.platform.runner.statement.RegisterBeanStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.UnwrapProcessingRuntimeExceptionStatement;
import org.eclipse.scout.rt.testing.server.runner.statement.ClearServerRunContextStatement;
import org.eclipse.scout.rt.testing.server.runner.statement.ServerRunContextStatement;
import org.eclipse.scout.service.AbstractService;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require a session and transaction context.
 * <p/>
 * Use the following mandatory annotations to configure the Runner:
 * <ul>
 * <li><strong>RunWithServerSession</strong>:<br/>
 * to specify the server-session to be used; can be defined on class or method-level;</li>
 * <li><strong>RunWithSubject</strong>:<br/>
 * to specify the user on behalf of which to run the test; can be defined on class or method-level;</li>
 * </ul>
 * Example:
 *
 * <pre>
 * &#064;RunWith(ServerTestRunner.class)
 * &#064;RunWithServerSession()
 * &#064;RunWithSubject(&quot;anna&quot;)
 * public class YourTest {
 *    ...
 * }
 * </pre>
 *
 * Execution:
 * <ul>
 * <li>Each test-method is executed in a separate transaction - meaning that the transaction boundary starts before
 * executing the first 'before-method', and ends after executing the last 'after-method'.</li>
 * <li>By default, server sessions are shared among same users. This can be changed by setting the
 * {@link ServerSessionProvider} or a custom provider to {@link RunWithServerSession#provider()}.</li>
 * <li>'beforeClass' and 'afterClass' are executed in the same transaction.</li>
 * </ul>
 *
 * @see RunWithServerSession
 * @see RunWithSubject
 * @since 5.1
 */
public class ServerTestRunner extends PlatformTestRunner {

  public ServerTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement interceptClassLevelStatement(final Statement next, final Class<?> testClass) {
    final Statement s2 = interceptServerSessionStatement(next, testClass.getAnnotation(RunWithServerSession.class),-1000);
    final Statement s3 = super.interceptClassLevelStatement(s4, testClass);
    final Statement s2 = new RegisterBeanStatement(s3, new WrapAndThrowExceptionHandler()); // exception handler to not silently swallow exceptions.
    final Statement s1 = new ClearServerRunContextStatement(s2);

    return s1;
  }

  protected boolean expectsException(Test annotation) {
    return annotation != null && annotation.expected() != None.class;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s2 = interceptServerSessionStatement(next, ReflectionUtility.getAnnotation(RunWithServerSession.class, testMethod, testClass,-10001));
    final Statement s3 = super.interceptMethodLevelStatement(s4, testClass, testMethod);
    final Statement s2 = new RegisterBeanStatement(s3, new WrapAndThrowExceptionHandler()); // exception handler to not silently swallow exceptions.
    final Statement s1 = new ClearServerRunContextStatement(s2);
    return s1;
  }

  /**
   * <code>ExceptionHandler</code> that wraps <code>ProcessingException</code>s into
   * <code>ProcessingRuntimeException</code>s. That is to not silently swallow exceptions, but to propagate them to the
   * test instead. Exceptions of that type are unwrapped by {@link UnwrapProcessingRuntimeExceptionStatement}.
   */
  @ApplicationScoped
  protected class WrapAndThrowExceptionHandler extends AbstractService implements IExceptionHandlerService {

    @Override
    public void handleException(ProcessingException pe) {
      if (!pe.isConsumed()) {
        throw new ProcessingRuntimeException(pe);
      }
    }
  }
}
