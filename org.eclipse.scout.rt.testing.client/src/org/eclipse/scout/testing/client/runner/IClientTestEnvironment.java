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
package org.eclipse.scout.testing.client.runner;

import java.net.Authenticator;
import java.net.CookieManager;

import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner.ClientTest;
import org.eclipse.scout.testing.client.servicetunnel.http.MultiClientAuthenticator;
import org.eclipse.scout.testing.client.servicetunnel.http.MultiClientSessionCookieStore;

/**
 * A custom test environment (e.g. for running tests in Maven Tycho) implementing this interface will be used by
 * {@link ScoutClientTestRunner} if located on {@link SerializationUtility#getClassLoader()} classpath. This environment
 * will be instantiated statically by {@link ScoutClientTestRunner} before any tests are executed.
 * <p/>
 * The custom {@link IClientTestEnvironment} class must use the following
 * <b>fully qualified</b> class name:
 * <p/>
 * <code>org.eclipse.scout.testing.client.runner.CustomClientTestEnvironment</code>
 * 
 * @author Adrian Moser
 */
public interface IClientTestEnvironment {

  /**
   * Typically required to set the default {@link CookieManager} to a custom manager based on
   * {@link MultiClientSessionCookieStore} to support tests with different users.
   */
  void installCookieStore();

  /**
   * Typically used to install an {@link Authenticator} based on {@link MultiClientAuthenticator} to support tests with
   * different users. The default user can be set using {@link MultiClientAuthenticator#setDefaultUser()}, as opposed to
   * the {@link IServerTestEnvironment} where the default user can be directly set on the test environment. The default
   * user can be overriden by
   * using the {@link ClientTest#runAs()} annotation on your test class.
   */
  void installNetAuthenticator();

  /**
   * @return {@link IClientSession} implementation used for running tests when {@link ClientTest#clientSessionClass()}
   *         is not set.
   */
  Class<? extends IClientSession> getDefaultClientSessionClass();

}
