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
package org.eclipse.scout.rt.testing.client.runner;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.eclipse.scout.rt.testing.client.runner.statement.ClearClientRunContextStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.ClientRunContextStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.RunInModelJobStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.TimeoutClientRunContextStatement;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
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
 * <b>Note</b>: Usually, all {@link Before}, the {@link Test}-annotated method and all {@link After} methods are invoked
 * in a single model job. But if the {@link Test}-annotated method uses the timeout feature (i.e. {@link Test#timeout()}
 * ), the three parts are executed in different model jobs.
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
    final Statement s4 = new RunInModelJobStatement(next);
    final Statement s3 = new ClientRunContextStatement(s4, ReflectionUtility.getAnnotation(RunWithClientSession.class, testClass));
    final Statement s2 = super.interceptClassLevelStatement(s3, testClass);
    final Statement s1 = new ClearClientRunContextStatement(s2);
    return s1;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s4;
    if (hasNoTimeout(testMethod)) {
      s4 = new RunInModelJobStatement(next);
    }
    else {
      // Three different model jobs are scheduled for all @Before methods, the @Test-annotated method and all @After methods.
      s4 = next;
    }
    final Statement s3 = new ClientRunContextStatement(s4, ReflectionUtility.getAnnotation(RunWithClientSession.class, testMethod, testClass));
    final Statement s2 = super.interceptMethodLevelStatement(s3, testClass, testMethod);
    final Statement s1 = new ClearClientRunContextStatement(s2);
    return s1;
  }

  @Override
  @SuppressWarnings("deprecation")
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    long timeoutMillis = getTimeoutMillis(method.getMethod());
    if (timeoutMillis <= 0) {
      // no timeout specified
      return next;
    }
    return new TimeoutClientRunContextStatement(next, timeoutMillis);
  }

  @Override
  protected Statement interceptBeforeStatement(Statement next, Class<?> testClass, Method testMethod) {
    Statement interceptedBeforeStatement = super.interceptBeforeStatement(next, testClass, testMethod);
    if (hasNoTimeout(testMethod)) {
      // no timeout specified
      return interceptedBeforeStatement;
    }
    return new TimeoutClientRunContextStatement(interceptedBeforeStatement, 0);
  }

  @Override
  protected Statement interceptAfterStatement(Statement next, Class<?> testClass, Method testMethod) {
    Statement interceptedAfterStatement = super.interceptAfterStatement(next, testClass, testMethod);
    if (hasNoTimeout(testMethod)) {
      // no timeout specified
      return interceptedAfterStatement;
    }
    return new TimeoutClientRunContextStatement(interceptedAfterStatement, 0);
  }
}
