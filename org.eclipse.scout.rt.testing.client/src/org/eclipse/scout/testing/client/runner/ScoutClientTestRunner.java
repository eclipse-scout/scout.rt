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

import java.security.AccessController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.RunWithSession;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.ProcessingRuntimeExceptionUnwrappingStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * TODO imo new javadoc
 * JUnit test runner that runs the annotated test class within a Scout client job. Test cases executed by this runner
 * may be configured with a {@link ClientTest} annotation.
 * <p/>
 * <h3>Custom Test Environments</h3> A custom test environment (e.g. required for running Tests with Maven Tycho) can be
 * set up using an implementation of {@link IClientTestEnvironment} which must be on the classpath of
 * {@link SerializationUtility#getClassLoader()}. The custom {@link IClientTestEnvironment} class must use the following
 * <b>fully qualified</b> class name:
 * <p/>
 * <code>org.eclipse.scout.testing.client.runner.CustomClientTestEnvironment</code>
 */
public class ScoutClientTestRunner extends PlatformTestRunner {
  private static final Map<String, IClientSession> GLOBAL_CACHE = new HashMap<String, IClientSession>();
  private static final Object GLOBAL_CACHE_LOCK = new Object();

  private IClientSession m_clientSession;

  @SuppressWarnings("unchecked")
  public ScoutClientTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    RunWithSession runWithSessionAnnotation = klass.getAnnotation(RunWithSession.class);
    if (runWithSessionAnnotation == null) {
      throw new InitializationError("@" + ScoutClientTestRunner.class.getSimpleName() + " must be used together with @" + RunWithSession.class.getSimpleName() + " and @" + RunWithSubject.class.getSimpleName());
    }
    if (runWithSessionAnnotation.useCache()) {
      m_clientSession = getGlobalClientSession((Class<? extends IClientSession>) runWithSessionAnnotation.value());
    }
    if (m_clientSession == null) {
      try {
        m_clientSession = (IClientSession) runWithSessionAnnotation.value().newInstance();
      }
      catch (Exception e) {
        throw new InitializationError(e);
      }
    }
    if (runWithSessionAnnotation.useCache()) {
      putGlobalClientSession(m_clientSession);
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // unpack wrapped ProcessingExceptions and rethrow them
    return super.possiblyExpectingExceptions(method, test, new ProcessingRuntimeExceptionUnwrappingStatement(next));
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
  protected Statement createWrappedStatement(final FrameworkMethod method, final Statement inner) {
    return super.createWrappedStatement(method, new ScoutClientJobWrapperStatement(m_clientSession, inner));
  }

  @Internal
  protected IClientSession getGlobalClientSession(Class<? extends IClientSession> clazz) {
    final String userId = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().getName();
    synchronized (GLOBAL_CACHE_LOCK) {
      String cacheKey = createSessionCacheKey(clazz, userId);
      IClientSession clientSession = GLOBAL_CACHE.get(cacheKey);
      if (clientSession == null || !clientSession.isActive()) {
        return null;
      }
      return clientSession;
    }
  }

  @Internal
  protected void putGlobalClientSession(IClientSession clientSession) {
    final String userId = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next().getName();
    synchronized (GLOBAL_CACHE_LOCK) {
      String cacheKey = createSessionCacheKey(clientSession.getClass(), userId);
      GLOBAL_CACHE.put(cacheKey, clientSession);
    }
  }

  /**
   * Creates a cache key for the given session class and the name of the user
   *
   * @param sessionClass
   * @param userId
   * @return
   */
  @Internal
  protected String createSessionCacheKey(Class<? extends IClientSession> sessionClass, String userId) {
    return StringUtility.join("-", sessionClass.getName(), userId);
  }

}
