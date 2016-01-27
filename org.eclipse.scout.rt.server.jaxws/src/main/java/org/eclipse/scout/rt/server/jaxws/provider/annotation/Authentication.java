/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.commons.authentication.ICredentialVerifier;
import org.eclipse.scout.rt.server.commons.authentication.SimplePrincipalProducer;
import org.eclipse.scout.rt.server.context.ServerRunContextProducer;
import org.eclipse.scout.rt.server.jaxws.provider.auth.handler.AuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

/**
 * Indicates what authentication mechanism to install on a webservice endpoint, and in which {@link RunContext} to run
 * authenticated webservice requests.
 * <p>
 * The mechanism consists of the {@link IAuthenticationMethod} to challenge the client to provide credentials, and the
 * {@link ICredentialVerifier} to verify request's credentials against a data source.
 * <p>
 * If {@link IAuthenticationMethod} is set, an {@link AuthenticationHandler} is generated at compile time (APT) and
 * registered in the handler chain as very first handler.
 *
 * @see WebServiceEntryPoint
 * @since 5.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
@Inherited
public @interface Authentication {

  /**
   * Indicates the authentication method to be used to challenge the client to provide credentials. By default,
   * {@link NullAuthenticationMethod} is used to disable authentication.
   *
   * @see IAuthenticationMethod
   */
  Clazz method() default @Clazz(value = NullAuthenticationMethod.class)
  ;

  /**
   * Indicates against which data source credentials are to be verified. By default, {@link ForbiddenCredentialVerifier}
   * is used to reject any webservice request.
   *
   * @see ICredentialVerifier
   */
  Clazz verifier() default @Clazz(value = ForbiddenCredentialVerifier.class)
  ;

  /**
   * Specifies the position where to register the authentication handler in the handler chain. By default, it is
   * registered as the very first handler. The order is 0-based.
   */
  int order() default 0;

  /**
   * Indicates the principal producer to use to create principals to represent authenticated users. By default,
   * {@link SimplePrincipalProducer} is used.
   */
  Clazz principalProducer() default @Clazz(value = SimplePrincipalProducer.class)
  ;

  /**
   * Indicates which {@link RunContext} to use to run authenticated webservice requests. By default,
   * {@link ServerRunContextProducer} is used, which is based on a {@link ServerSessionProviderWithCache session cache},
   * and enforces to run in a new transaction.
   */
  Clazz runContextProducer() default @Clazz(value = ServerRunContextProducer.class)
  ;

  /**
   * Represents no authentication handler to be installed.
   */
  @Internal
  public interface NullAuthenticationMethod extends IAuthenticationMethod {
  }

  /**
   * Credential verifier which always returns <code>forbidden</code>.
   */
  @Internal
  public static final class ForbiddenCredentialVerifier implements ICredentialVerifier {

    @Override
    public int verify(final String username, final char[] password) throws IOException {
      return ICredentialVerifier.AUTH_FORBIDDEN;
    }
  }
}
