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
package org.eclipse.scout.rt.testing.server.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.services.common.security.SimplePrincipal;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.ProcessingRuntimeExceptionUnwrappingStatement;
import org.eclipse.scout.service.SERVICES;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit test runner that runs the annotated test class within a Scout server job. Test cases executed by this runner
 * requires also a {@link ServerSessionClass} and a {@link PrincipalName} annotation.
 */
public class ScoutServerTestRunner extends BlockJUnit4ClassRunner {

  private IServerSession m_serverSession;
  private Subject m_subject;

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface ServerSessionClass {
    /**
     * @return the classes to be run
     */
    public Class<? extends IServerSession> value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface PrincipalName {
    /**
     * @return the principal name under which the test is to run
     */
    public String value();
  }

  private static Class<? extends IServerSession> getServerSessionClass(Class<?> klass) throws InitializationError {
    ServerSessionClass annotation = klass.getAnnotation(ServerSessionClass.class);
    if (annotation == null) {
      throw new InitializationError(String.format("class '%s' must have a ServerSessionClass annotation", klass.getName()));
    }
    return annotation.value();
  }

  private static String getPrincipalName(Class<?> klass) throws InitializationError {
    PrincipalName annotation = klass.getAnnotation(PrincipalName.class);
    if (annotation == null) {
      throw new InitializationError(String.format("class '%s' must have a PrincipalName annotation", klass.getName()));
    }
    return annotation.value();
  }

  /**
   * @param klass
   * @throws InitializationError
   */
  public ScoutServerTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    try {
      Subject subject = new Subject();
      subject.getPrincipals().add(new SimplePrincipal(getPrincipalName(klass)));
      m_serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(getServerSessionClass(klass), subject);
      m_subject = subject;
    }
    catch (ProcessingException e) {
      ArrayList<Throwable> exceptions = new ArrayList<Throwable>(1);
      exceptions.add(e);
      throw new InitializationError(exceptions);
    }
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    // wrap the methods of a class into a statements, that creates a Scout client job.
    return new ScoutServerJobWrapperStatement(m_serverSession, m_subject, super.classBlock(notifier));
  }

  @Override
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // unpack wrapped ProcessingExceptions and rethrow them
    return super.possiblyExpectingExceptions(method, test, new ProcessingRuntimeExceptionUnwrappingStatement(next));
  }
}
