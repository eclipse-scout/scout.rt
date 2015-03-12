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
package org.eclipse.scout.jaxws.internal;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler;
import org.eclipse.scout.jaxws.security.provider.IAuthenticator;

/**
 * Constants used in JAX-WS Scout RT.
 */
public interface JaxWsConstants {

  /**
   * User on behalf of which anonymous webservice requests are processed.
   *
   * @see IAuthenticationHandler.None
   */
  String USER_ANONYMOUS = "jaxws-anonymous";

  /**
   * User on behalf of which webservice requests are authenticated.
   *
   * @see IAuthenticator
   */
  String USER_AUTHENTICATOR = ConfigIniUtility.getProperty("org.eclipse.scout.jaxws.security.provider.Authenticator#user", "jaxws-authenticator");

  // === constants for BASIC authentication ===
  String AUTH_NONE_NAME = "NONE";
  String AUTH_BASIC_NAME = "BASIC";
  String AUTH_WSSE_NAME = "WSSE Username Token";

  String AUTH_BASIC_AUTHORIZATION = "authorization";
  String AUTH_BASIC_AUTHENTICATE = "WWW-Authenticate";
  String AUTH_BASIC_PREFIX = "Basic ";
}
