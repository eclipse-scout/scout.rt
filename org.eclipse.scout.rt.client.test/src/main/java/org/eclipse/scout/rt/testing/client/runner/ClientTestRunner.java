/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client.runner;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.internal.VirtualDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.eclipse.scout.rt.testing.client.runner.statement.CleanupPagesStatements.CleanupPagesAfterClassStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.CleanupPagesStatements.CleanupPagesBeforeClassStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.CleanupPagesStatements.CleanupPagesMethodStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.ClientRunContextStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.RunInModelJobStatement;
import org.eclipse.scout.rt.testing.client.runner.statement.TimeoutClientRunContextStatement;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.BlockingTestUtility;
import org.eclipse.scout.testing.client.BlockingTestUtility.IBlockingConditionTimeoutHandle;
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
 * <p>
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
    final Statement s4 = new CleanupDesktopStatement(next);
    final Statement s3 = new RunInModelJobStatement(s4);
    final Statement s2 = new ClientRunContextStatement(s3, ReflectionUtility.getAnnotation(RunWithClientSession.class, testClass));
    final Statement s1 = super.interceptClassLevelStatement(s2, testClass);
    return s1;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s6 = new CleanupDesktopStatement(next);
    final Statement s5 = new CleanupPagesMethodStatement(s6);
    final Statement s4;
    if (hasNoTimeout(testMethod)) {
      s4 = new RunInModelJobStatement(s5);
    }
    else {
      // Three different model jobs are scheduled for all @Before methods, the @Test-annotated method and all @After methods.
      s4 = s5;
    }
    final Statement s3 = new AddBlockingConditionTimeoutStatement(s4);
    final Statement s2 = new ClientRunContextStatement(s3, ReflectionUtility.getAnnotation(RunWithClientSession.class, testMethod, testClass));
    final Statement s1 = super.interceptMethodLevelStatement(s2, testClass, testMethod);
    return s1;
  }

  @Override
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    long timeoutMillis = getTimeoutMillis(method.getMethod());
    if (timeoutMillis <= 0) {
      // no timeout specified
      return next;
    }
    return new TimeoutClientRunContextStatement(next, timeoutMillis);
  }

  @Override
  protected Statement interceptBeforeClassStatement(Statement s, Class<?> javaClass) {
    s = new CleanupPagesBeforeClassStatement(s);
    s = super.interceptBeforeClassStatement(s, javaClass);
    return s;
  }

  @Override
  protected Statement interceptAfterClassStatement(Statement s, Class<?> javaClass) {
    s = new CleanupPagesAfterClassStatement(s);
    s = super.interceptAfterClassStatement(s, javaClass);
    return s;
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

  @Override
  protected RunContext createJUnitRunContext() {
    return ClientRunContexts.empty();
  }

  protected static class AddBlockingConditionTimeoutStatement extends Statement {
    private final Statement m_statement;

    public AddBlockingConditionTimeoutStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      IBlockingConditionTimeoutHandle reg = BlockingTestUtility.addBlockingConditionTimeoutListener(2, TimeUnit.MINUTES);
      try {
        m_statement.evaluate();
        if (reg.getFirstException() != null) {
          throw reg.getFirstException();
        }
      }
      finally {
        reg.dispose();
      }
    }
  }

  /**
   * {@link Statement} which closes all forms which were started during the test evaluation.
   */
  protected static class CleanupDesktopStatement extends Statement {

    private final Statement m_statement;

    public CleanupDesktopStatement(Statement statement) {
      m_statement = statement;
    }

    @Override
    public void evaluate() throws Throwable {
      final Set<IForm> initialOpenForms = getDisplayedForms();
      try {
        m_statement.evaluate();
      }
      finally {
        final IDesktop desktop = IDesktop.CURRENT.get();
        if (desktop != null) {
          Set<IForm> openForms = getDisplayedForms();
          openForms.removeAll(initialOpenForms);
          // Close all opened (including all depending forms display parent hierarchy) forms which were started during test evaluation.
          desktop.closeForms(openForms);
        }
      }
    }

    private Set<IForm> getDisplayedForms() {
      final IDesktop desktop = IDesktop.CURRENT.get();
      if (desktop == null || desktop instanceof VirtualDesktop) {
        return Collections.emptySet();
      }
      Set<IForm> forms = new HashSet<>();
      forms.addAll(desktop.getDialogs());
      forms.addAll(desktop.getViews());
      return forms;
    }
  }
}
