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
package org.eclipse.scout.rt.testing.platform;

import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * This Runner ensures the {@link Platform} to be started and notifies {@link ITestExecutionListener} upon test
 * execution.
 *
 * @since 5.1
 */
public class PlatformTestRunner extends BlockJUnit4ClassRunner {

  private static final AtomicBoolean INIT = new AtomicBoolean();

  public PlatformTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    final Statement superStatement = super.classBlock(notifier);

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        ((Platform) Platform.get()).ensureStarted();
        notifyInit();
        superStatement.evaluate();
      }
    };
  }

  @Override
  protected Statement withBeforeClasses(final Statement statement) {
    final Statement superStatement = super.withBeforeClasses(statement);

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        notifyBeforeClass();
        superStatement.evaluate();
      }
    };
  }

  @Override
  protected Statement withAfterClasses(final Statement statement) {
    final Statement superStatement = super.withBeforeClasses(statement);

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          superStatement.evaluate();
        }
        finally {
          notifyAfterClass();
        }
      }
    };
  }

  @Internal
  protected void notifyInit() {
    if (INIT.compareAndSet(false, true)) {
      final ITestExecutionListener listener = OBJ.oneOrNull(ITestExecutionListener.class);
      if (listener != null) {
        listener.init();
      }
    }
  }

  @Internal
  protected void notifyBeforeClass() {
    final ITestExecutionListener listener = OBJ.oneOrNull(ITestExecutionListener.class);
    if (listener != null) {
      listener.beforeTestClass(getDescription());
    }
  }

  @Internal
  protected void notifyAfterClass() {
    final ITestExecutionListener listener = OBJ.oneOrNull(ITestExecutionListener.class);
    if (listener != null) {
      listener.afterTestClass(getDescription());
    }
  }

  @Override
  protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
    ICallable<Statement> callable = new ICallable<Statement>() {
      @Override
      public Statement call() throws Exception {
        return PlatformTestRunner.super.methodInvoker(method, test);
      }
    };
    callable = wrapMethodInvocation(callable, method, test);
    try {
      return callable.call();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Internal
  protected ICallable<Statement> wrapMethodInvocation(final ICallable<Statement> callable, final FrameworkMethod method, final Object test) {
    // 1. Get 'runAs' from current invocation context, e.g. before, test-method, after, ...
    RunWithSubject a = method.getAnnotation(RunWithSubject.class);
    if (a == null && test != null) {
      // 2. Get 'runAs' as defined on test-class.
      a = test.getClass().getAnnotation(RunWithSubject.class);
    }
    if (a == null) {
      return callable;
    }

    final Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(a.value()));
    subject.setReadOnly();
    return new ICallable<Statement>() {
      @Override
      public Statement call() throws Exception {
        return Subject.doAs(subject, new PrivilegedExceptionAction<Statement>() {
          @Override
          public Statement run() throws Exception {
            return callable.call();
          }
        });
      }
    };
  }
}
