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

import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.testing.platform.ITestExecutionListener;
import org.eclipse.scout.rt.testing.platform.runner.statement.NotifyTestListenerStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.SubjectStatement;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require the platform to be started.
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

  public PlatformTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    // Ensure the platform to be started.
    if (Platform.get() == null) {
      Platform.setDefault();
      Platform.get().start();
      final ITestExecutionListener listener = OBJ.getOptional(ITestExecutionListener.class);
      if (listener != null) {
        listener.platformStarted();
      }
    }

    final Statement superStatement = PlatformTestRunner.super.classBlock(notifier);

    return interceptClassLevelStatement(superStatement, getTestClass().getJavaClass());
  }

  @Override
  protected Statement methodBlock(final FrameworkMethod method) {
    final Statement superStatement = PlatformTestRunner.super.methodBlock(method);

    return interceptMethodLevelStatement(superStatement, getTestClass().getJavaClass(), method.getMethod());
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
    Statement s2 = new SubjectStatement(next, testClass.getAnnotation(RunWithSubject.class));
    Statement s1 = new NotifyTestListenerStatement(s2, getDescription());

    return s1;
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
}
