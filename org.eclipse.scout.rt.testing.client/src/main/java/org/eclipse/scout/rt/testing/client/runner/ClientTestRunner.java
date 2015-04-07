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
package org.eclipse.scout.rt.testing.client.runner;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.testing.client.runner.statement.ClearClientRunContextStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.ClientRunContextStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.RunInModelJobStatement;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.ProcessingRuntimeException;
import org.eclipse.scout.rt.testing.platform.runner.statement.RegisterBeanStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.UnwrapProcessingRuntimeExceptionStatement;
import org.eclipse.scout.service.AbstractService;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require a session context.
 * <p/>
 * Use the following mandatory annotations to configure the Runner:
 * <ul>
 * <li><strong>RunWithClientSession</strong>:<br/>
 * to specify the client-session to be used; can be defined on class or method-level;</li>
 * <li><strong>RunWithSubject</strong>:<br/>
 * to specify the user on behalf of which to run the test; can be defined on class or method-level;</li>
 * </ul>
 * Example:
 *
 * <pre>
 * &#064;RunWith(ClientTestRunner.class)
 * &#064;RunWithClientSession()
 * &#064;RunWithSubject(&quot;anna&quot;)
 * public class YourTest {
 *    ...
 * }
 * </pre>
 *
 * Execution:
 * <ul>
 * <li>By default, client sessions are shared among same users. This can be changed by setting the
 * {@link ClientSessionProvider} or a custom provider to {@link RunWithClientSession#provider()}.</li>
 * </ul>
 *
 * @see RunWithClientSession
 * @see RunWithSubject
 * @since 5.1
 */
public class ClientTestRunner extends PlatformTestRunner {

  public ClientTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement interceptClassLevelStatement(final Statement next, final Class<?> testClass) {
    final Statement s5 = new RunInModelJobStatement(next);
    final Statement s4 = new ClientRunContextStatement(s5, ReflectionUtility.getAnnotation(RunWithClientSession.class, testClass));
    final Statement s3 = super.interceptClassLevelStatement(s4, testClass);
    final Statement s2 = new RegisterBeanStatement(s3, WrapAndThrowExceptionHandler.class); // exception handler to not silently swallow exceptions.
    final Statement s1 = new ClearClientRunContextStatement(s2);
    return s1;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s5 = new RunInModelJobStatement(next);
    final Statement s4 = new ClientRunContextStatement(s5, ReflectionUtility.getAnnotation(RunWithClientSession.class, testMethod, testClass));
    final Statement s3 = super.interceptMethodLevelStatement(s4, testClass, testMethod);
    final Statement s2 = new RegisterBeanStatement(s3, WrapAndThrowExceptionHandler.class); // exception handler to not silently swallow exceptions.
    final Statement s1 = new ClearClientRunContextStatement(s2);
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
