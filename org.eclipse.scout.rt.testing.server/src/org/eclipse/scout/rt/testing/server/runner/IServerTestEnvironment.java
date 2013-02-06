/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner;

import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner.ServerTest;

/**
 * A custom test environment (e.g. for running tests in Maven Tycho) implementing this interface will be used by
 * {@link ScoutServerTestRunner} if located on {@link SerializationUtility#getClassLoader()} classpath. This environment
 * will be instantiated statically by {@link ScoutServerTestRunner} before any tests are executed.
 * <p/>
 * The custom {@link IServerTestEnvironment} class must use the following
 * <b>fully qualified</b> class name:
 * <p/>
 * <code>org.eclipse.scout.testing.server.runner.CustomServerTestEnvironment</code>
 * <p/>
 * 
 * @author Adrian Moser
 */
public interface IServerTestEnvironment {

  /**
   * General setup for the test environment.
   */
  void setup();

  /**
   * @return {@link IServerSession} implementation used by default for running tests when
   *         {@link ServerTest#serverSessionClass()} is not set.
   */
  Class<? extends IServerSession> getDefaultServerSessionClass();

  /**
   * @return the default user which is used for running the tests. This can be overriden by setting the
   *         {@link ServerTest#runAs()} annotation on your test class.
   */
  String getDefaultPrincipalName();

}
