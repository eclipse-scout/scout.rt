/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.ProcessingRuntimeExceptionUnwrappingStatement;
import org.eclipse.scout.testing.client.DefaultTestClientSessionProvider;
import org.eclipse.scout.testing.client.ITestClientSessionProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit test runner that runs the annotated test class within a Scout client job. Test cases executed by this runner
 * requires also a {@link ClientSessionClass} annotation.
 */
public class ScoutClientTestRunner extends BlockJUnit4ClassRunner {

  private final IClientSession m_clientSession;
  private static ITestClientSessionProvider s_clientSessionProvider = new DefaultTestClientSessionProvider();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface ClientSessionClass {
    /**
     * @return the classes to be run
     */
    public Class<? extends IClientSession> value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  public @interface RunAs {
    public String value();

    public Class<? extends ITestClientSessionProvider> sessionProvider() default NullTestClientSessionProvider.class;

    public boolean forceNewSession() default false;
  }

  private interface NullTestClientSessionProvider extends ITestClientSessionProvider {
  }

  private static Class<? extends IClientSession> getClientSessionClass(Class<?> klass) throws InitializationError {
    ClientSessionClass annotation = klass.getAnnotation(ClientSessionClass.class);
    if (annotation == null) {
      throw new InitializationError(String.format("class '%s' must have a ClientSessionClass annotation", klass.getName()));
    }
    return annotation.value();
  }

  /**
   * @param klass
   * @throws InitializationError
   */
  public ScoutClientTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    try {
      m_clientSession = getOrCreateClientSession(getClientSessionClass(klass), klass.getAnnotation(RunAs.class));
    }
    catch (Exception e) {
      throw new InitializationError(e);
    }
  }

  @Override
  protected Statement withBeforeClasses(Statement statement) {
    // run all methods annotated with @BeforeClass in a separate ServerSession
    List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforeClass.class);
    return befores.isEmpty() ? statement : new RunBeforesInSeparateScoutClientSession(m_clientSession, statement, befores, null);
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    // run all methods annotated with @AfterClass in a separate ServerSession
    List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterClass.class);
    return afters.isEmpty() ? statement : new RunAftersInSeparateScoutClientSession(m_clientSession, statement, afters, null);
  }

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    IClientSession clientSession = m_clientSession;
    RunAs runAs = method.getAnnotation(RunAs.class);
    if (runAs != null) {
      try {
        clientSession = getOrCreateClientSession(getClientSessionClass(getTestClass().getJavaClass()), runAs);
      }
      catch (final Throwable e) {
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
            throw e;
          }
        };
      }
    }
    // run each test method in a separate ClientSession
    return new ScoutClientJobWrapperStatement(clientSession, super.methodBlock(method));
  }

  @Override
  @SuppressWarnings("deprecation")
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // unpack wrapped ProcessingExceptions and rethrow them
    return super.possiblyExpectingExceptions(method, test, new ProcessingRuntimeExceptionUnwrappingStatement(next));
  }

  protected IClientSession getOrCreateClientSession(Class<? extends IClientSession> clientSessionClass, RunAs runAs) throws Exception {
    ITestClientSessionProvider sessionProvider = s_clientSessionProvider;
    String runAsUser = null;
    boolean runAsforceNewSession = false;
    if (runAs != null) {
      runAsUser = runAs.value();
      runAsforceNewSession = runAs.forceNewSession();
      if (runAs.sessionProvider() != null && runAs.sessionProvider() != NullTestClientSessionProvider.class) {
        sessionProvider = runAs.sessionProvider().newInstance();
      }
    }
    return sessionProvider.getOrCreateClientSession(clientSessionClass, runAsUser, runAsforceNewSession);
  }
}
