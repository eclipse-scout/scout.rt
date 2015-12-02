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
package org.eclipse.scout.rt.testing.platform.runner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.eclipse.scout.rt.testing.platform.runner.statement.BeanAnnotationsCleanupStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.BeanAnnotationsInitStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.PlatformStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.RegisterBeanStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.ReplaceJobManagerStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.ReplaceJobManagerStatement.JUnitJobManager;
import org.eclipse.scout.rt.testing.platform.runner.statement.SubjectStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.ThrowHandledExceptionStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.TimesStatement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require the Scout {@link IPlatform}
 * <p>
 * Use {@link RunWithNewPlatform} to run the test with a new platform.
 * <p>
 * Use <code>RunWithSubject</code> annotation to specify the user to run the test. This annotation can be defined on
 * class or method-level. If defining the user on class-level, all test-methods inherit that user.
 * <p>
 * A new dedicated {@link JobManager} is installed for every JUnit test class, which replaces {@link JobManager}. This
 * prevents job interferences among test classes using a shared platform.<br/>
 * If you require to replace the job manager in your test case, register a subclass of {@link JUnitJobManager} as a
 * replacement of {@link JUnitJobManager} in the bean manager.
 * <p>
 * Example:
 *
 * <pre>
 * &#064;RunWith(PlatformTestRunner.class)
 * &#064;RunWithSubject(&quot;anna&quot;)
 * public class YourTest {
 *    ...
 * }
 * </pre>
 *
 * @see RunWithSubject
 * @since 5.1
 */
public class PlatformTestRunner extends BlockJUnit4ClassRunner {

  public PlatformTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    final Statement s3 = super.classBlock(notifier);
    final Statement s2 = new ReplaceJobManagerStatement(s3);
    final Statement s1 = new PlatformStatement(s2, ReflectionUtility.getAnnotation(RunWithNewPlatform.class, getTestClass().getJavaClass()));

    return s1;
  }

  @Override
  protected Statement withBeforeClasses(final Statement statement) {
    final List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforeClass.class);
    if (befores.isEmpty()) {
      return statement;
    }

    Statement beforeClassStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for (FrameworkMethod each : befores) {
          each.invokeExplosively(null);
        }
      }
    };

    final Statement interceptedBeforeClassStatement = interceptBeforeClassStatement(beforeClassStatement, getTestClass().getJavaClass());
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        interceptedBeforeClassStatement.evaluate();
        statement.evaluate();
      }
    };
  }

  @Override
  protected Statement withAfterClasses(final Statement statement) {
    final List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterClass.class);
    if (afters.isEmpty()) {
      return statement;
    }

    final List<Throwable> errors = new ArrayList<>();
    final Statement afterClassStatement = new RunAftersStatement(afters, null, errors);
    final Statement interceptedAfterClassStatement = interceptAfterClassStatement(afterClassStatement, getTestClass().getJavaClass());
    return new InterceptedAfterStatement(statement, interceptedAfterClassStatement, errors);
  }

  @Override
  protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement statement) {
    final List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
    if (befores.isEmpty()) {
      return new BeanAnnotationsInitStatement(statement, target);
    }

    Statement beforeStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for (FrameworkMethod each : befores) {
          each.invokeExplosively(target);
        }
      }
    };

    final Statement interceptedBeforeStatement = interceptBeforeStatement(beforeStatement, getTestClass().getJavaClass(), method.getMethod());
    return new BeanAnnotationsInitStatement(new InterceptedBeforeStatement(statement, interceptedBeforeStatement), target);
  }

  @Override
  protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement statement) {
    final List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class);
    if (afters.isEmpty()) {
      return new BeanAnnotationsCleanupStatement(statement);
    }

    final List<Throwable> errors = new ArrayList<>();
    final Statement afterStatement = new RunAftersStatement(afters, target, errors);
    final Statement interceptedAfterStatement = interceptAfterStatement(afterStatement, getTestClass().getJavaClass(), method.getMethod());
    InterceptedAfterStatement s1 = new InterceptedAfterStatement(statement, interceptedAfterStatement, errors);

    return new BeanAnnotationsCleanupStatement(s1);
  }

  @Override
  protected Statement methodBlock(final FrameworkMethod method) {
    final Statement superStatement = PlatformTestRunner.super.methodBlock(method);

    return interceptMethodLevelStatement(superStatement, getTestClass().getJavaClass(), method.getMethod());
  }

  protected Statement interceptBeforeClassStatement(Statement beforeClassStatement, Class<?> javaClass) {
    return interceptClassLevelStatement(beforeClassStatement, javaClass);
  }

  protected Statement interceptAfterClassStatement(Statement beforeClassStatement, Class<?> javaClass) {
    return interceptClassLevelStatement(beforeClassStatement, javaClass);
  }

  protected Statement interceptBeforeStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    return next;
  }

  protected Statement interceptAfterStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    return next;
  }

  /**
   * Overwrite this method to contribute some 'class-level' behavior to this Runner.
   * <p>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p>
   * <code>
   *   Statement s2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Statement s1 = new YourInterceptor1(s2); // executed 2nd<br/>
   *   Statement head = <i>super.interceptClassLevelStatement(s1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p>
   * <code>
   *   Statement s2 = <i>super.interceptClassLevelStatement(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Statement s1 = new YourInterceptor2(s2); // executed 2nd<br/>
   *   Statement head = new YourInterceptor1(s1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent {@link Statement}.
   * @return the head of the chain to be invoked first.
   */
  protected Statement interceptClassLevelStatement(final Statement next, final Class<?> testClass) {
    final Statement s2 = new SubjectStatement(next, testClass.getAnnotation(RunWithSubject.class));
    final Statement s1 = new RegisterBeanStatement(s2, new BeanMetaData(JUnitExceptionHandler.class).withReplace(true).withOrder(-1000)); // exception handler to not silently swallow handled exceptions.

    return s1;
  }

  /**
   * Overwrite this method to contribute some 'method-level' behavior to this Runner.
   * <p>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p>
   * <code>
   *   Statement s2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Statement s1 = new YourInterceptor1(s2); // executed 2nd<br/>
   *   Statement head = <i>super.interceptMethodLevelStatement(s1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p>
   * <code>
   *   Statement s2 = <i>super.interceptMethodLevelStatement(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Statement s1 = new YourInterceptor2(s2); // executed 2nd<br/>
   *   Statement head = new YourInterceptor1(s1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent {@link Statement}.
   * @return the head of the chain to be invoked first.
   */
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s3 = new SubjectStatement(next, ReflectionUtility.getAnnotation(RunWithSubject.class, testMethod, testClass));
    final Statement s2 = new RegisterBeanStatement(s3, new BeanMetaData(JUnitExceptionHandler.class).withReplace(true).withOrder(-1000)); // exception handler to not silently swallow handled exceptions.
    final Statement s1 = new TimesStatement(s2, ReflectionUtility.getAnnotation(Times.class, testMethod, testClass));

    return s1;
  }

  @Override
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // install statement to re-throw the first exception handled by JUnitExceptionHandler.
    return super.possiblyExpectingExceptions(method, test, new ThrowHandledExceptionStatement(next));
  }

  protected long getTimeoutMillis(Method method) {
    Test annotation = ReflectionUtility.getAnnotation(Test.class, method);
    return annotation == null ? 0 : annotation.timeout();
  }

  protected boolean hasNoTimeout(Method method) {
    return getTimeoutMillis(method) <= 0;
  }

  protected static class RunAftersStatement extends Statement {

    private final List<FrameworkMethod> m_afters;
    private final Object m_target;
    private final List<Throwable> m_errors;

    public RunAftersStatement(List<FrameworkMethod> afters, Object target, List<Throwable> errors) {
      m_afters = afters;
      m_target = target;
      m_errors = errors;
    }

    @Override
    public void evaluate() throws Throwable {
      for (FrameworkMethod each : m_afters) {
        try {
          each.invokeExplosively(m_target);
        }
        catch (Throwable e) {
          m_errors.add(e);
        }
      }
    }
  }

  protected static class InterceptedBeforeStatement extends Statement {

    private final Statement m_statement;
    private final Statement m_interceptedBeforeStatement;

    public InterceptedBeforeStatement(Statement statement, Statement interceptedBeforeStatement) {
      m_statement = statement;
      m_interceptedBeforeStatement = interceptedBeforeStatement;
    }

    @Override
    public void evaluate() throws Throwable {
      m_interceptedBeforeStatement.evaluate();
      m_statement.evaluate();
    }

  }

  protected static class InterceptedAfterStatement extends Statement {

    private final Statement m_statement;
    private final Statement m_interceptedAfterStatement;
    private final List<Throwable> m_errors;

    public InterceptedAfterStatement(Statement statement, Statement interceptedAfterStatement, List<Throwable> errors) {
      super();
      m_statement = statement;
      m_interceptedAfterStatement = interceptedAfterStatement;
      m_errors = errors;
    }

    @Override
    public void evaluate() throws Throwable {
      try {
        m_statement.evaluate();
      }
      catch (Throwable e) {
        m_errors.add(e);
      }
      finally {
        try {
          m_interceptedAfterStatement.evaluate();
        }
        catch (Throwable e) {
          m_errors.add(e);
        }
      }
      MultipleFailureException.assertEmpty(m_errors);
    }
  }
}
