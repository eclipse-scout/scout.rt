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
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.jaxws.ServerRunContextProvider;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.ConfigFileAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;

/**
 * Indicates what authentication mechanism to install on a webservice endpoint, and in which {@link RunContext} to run
 * authenticated webservice requests.
 * <p>
 * The mechanism consists of the {@link IAuthenticationMethod} to challenge the client to provide credentials, and the
 * {@link IAuthenticator} to validate provided credentials against a data source.
 * <p>
 * If <code>enabled</code>, an {@link AuthenticationHandler} is generated at compile time (APT) and registered in the
 * handler chain as very first handler.
 *
 * @see JaxWsPortTypeProxy
 * @since 5.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
@Inherited
public @interface Authentication {

  /**
   * Indicates whether to enable authentication and to generate an {@link AuthenticationHandler} at compile-time by APT.
   * By default, authentication is <code>disabled</code>.
   */
  boolean enabled() default false;

  /**
   * Indicates the authentication method to be used to challenge the client to provide credentials. By default,
   * {@link BasicAuthenticationMethod} is used.
   */
  Clazz method() default @Clazz(value = BasicAuthenticationMethod.class);

  /**
   * Indicates against which data source credentials are to be validated. By default, {@link ConfigFileAuthenticator} is
   * used.
   */
  Clazz authenticator() default @Clazz(value = ConfigFileAuthenticator.class);

  /**
   * Indicates which {@link ServerRunContextProvider} to use to run authenticated requests. By
   * default, {@link ServerRunContextProvider} is used which is based on a LRU cache to provide a {@link IServerSession}
   */
  Clazz runContextProvider() default @Clazz(value = ServerRunContextProvider.class);
}
