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

import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.testing.platform.runner.statement.SubjectStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.ThrowExceptionHandlerCauseStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require the platform to be started.
 * The {@link Platform#setDefault()} is started {@link IPlatform#start(Class)} without an application.
 * <p/>
 * Use <code>RunWithSubject</code> annotation to specify the user to run the test. This annotation can be defined on
 * class or method-level. If defining the user on class-level, all test-methods inherit that user.
 * <p/>
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

  private IPlatform m_platformBackup;

  public PlatformTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          m_platformBackup = Platform.get();
          Platform.setDefault();
          try {
            Platform.get().start(null);
            //
            Statement inner = PlatformTestRunner.super.classBlock(notifier);
            if (inner != null) {
              inner.evaluate();
            }
          }
          finally {
            Platform.get().stop();
          }
        }
        finally {
          Platform.set(m_platformBackup);
        }
      }
    };
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

    final List<Throwable> errors = new ArrayList<Throwable>();
    Statement afterClassStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        for (FrameworkMethod each : afters) {
          try {
            each.invokeExplosively(null);
          }
          catch (Throwable e) {
            errors.add(e);
          }
        }
      }
    };

    final Statement interceptedAfterClassStatement = interceptAfterClassStatement(afterClassStatement, getTestClass().getJavaClass());
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          statement.evaluate();
        }
        catch (Throwable e) {
          errors.add(e);
        }
        finally {
          try {
            interceptedAfterClassStatement.evaluate();
          }
          catch (Throwable e) {
            errors.add(e);
          }
        }
        MultipleFailureException.assertEmpty(errors);
      }
    };
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

  /**
   * Overwrite this method to contribute some 'class-level' behavior to this Runner.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Statement s2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Statement s1 = new YourInterceptor1(s2); // executed 2nd<br/>
   *   Statement head = <i>super.interceptClassLevelStatement(s1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
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
    return new SubjectStatement(next, testClass.getAnnotation(RunWithSubject.class));
  }

  /**
   * Overwrite this method to contribute some 'method-level' behavior to this Runner.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Statement s2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Statement s1 = new YourInterceptor1(s2); // executed 2nd<br/>
   *   Statement head = <i>super.interceptMethodLevelStatement(s1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
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
    return new SubjectStatement(next, ReflectionUtility.getAnnotation(RunWithSubject.class, testMethod, testClass));
  }

  @Override
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // install statement to re-throw exceptions caught by JUnitExceptionHandler.
    return super.possiblyExpectingExceptions(method, test, new ThrowExceptionHandlerCauseStatement(next));
  }
}
