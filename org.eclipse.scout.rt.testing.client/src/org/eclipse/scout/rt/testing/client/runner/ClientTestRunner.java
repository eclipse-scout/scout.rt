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
package org.eclipse.scout.rt.testing.client.runner;

import java.lang.reflect.Method;

import org.eclipse.scout.commons.ReflectionUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.rt.client.job.IClientJobManager;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.testing.client.runner.statement.ProvideClientSessionStatement;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.statement.RegisterBeanStatement;
import org.eclipse.scout.rt.testing.platform.runner.statement.RunNowStatement;
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
    final RunWithClientSession sessionAnnotation = testClass.getAnnotation(RunWithClientSession.class);

    final Statement s2 = interceptClientSessionStatement(next, sessionAnnotation, 1000);
    final Statement s1 = super.interceptClassLevelStatement(s2, testClass);

    return s1;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final RunWithClientSession sessionAnnotation = ReflectionUtility.getAnnotation(RunWithClientSession.class, testMethod, testClass);

    final Statement s2 = interceptClientSessionStatement(next, sessionAnnotation, 1001);
    final Statement s1 = super.interceptMethodLevelStatement(s2, testClass, testMethod);

    return s1;
  }

  /**
   * Method invoked to create the session context.
   */
  @Internal
  protected Statement interceptClientSessionStatement(final Statement next, final RunWithClientSession annotation, final int priority) {
    if (annotation == null) {
      return next;
    }
    final Statement s3 = new RunNowStatement(next, OBJ.get(IClientJobManager.class));
    final Statement s2 = new ProvideClientSessionStatement(s3, annotation.provider());
    final Statement s1 = new RegisterBeanStatement(s2, annotation.value(), priority);

    return s1;
  }
}
