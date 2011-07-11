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

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.ProcessingRuntimeExceptionUnwrappingStatement;
import org.eclipse.scout.service.SERVICES;
import org.junit.runner.notification.RunNotifier;
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

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface ClientSessionClass {
    /**
     * @return the classes to be run
     */
    public Class<? extends IClientSession> value();
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
    m_clientSession = SERVICES.getService(IClientSessionRegistryService.class).getClientSession(getClientSessionClass(klass));
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    // wrap the methods of a class into a statements, that creates a Scout client job.
    return new ScoutClientJobWrapperStatement(m_clientSession, super.classBlock(notifier));
  }

  @SuppressWarnings("deprecation")
  @Override
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // unpack wrapped ProcessingExceptions and rethrow them
    return super.possiblyExpectingExceptions(method, test, new ProcessingRuntimeExceptionUnwrappingStatement(next));
  }
}
