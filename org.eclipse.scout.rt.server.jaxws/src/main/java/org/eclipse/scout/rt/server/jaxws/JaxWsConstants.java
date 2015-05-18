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
package org.eclipse.scout.rt.server.jaxws;

import java.util.Collections;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.jaxws.handler.LogHandler;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.ConfigIniAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.handler.HandlerProxy;

/**
 * Configuration constants used in Scout JAX-WS RT.
 */
public interface JaxWsConstants {

  /**
   * Technical user to validate user's credentials; used by {@link IAuthenticator}.
   */
  String CONFIG_USER_AUTHENTICATOR = "jaxws.user.authenticator";
  String USER_AUTHENTICATOR = ConfigIniUtility.getProperty(CONFIG_USER_AUTHENTICATOR, "jaxws-authenticator");
  Subject SUBJECT_AUTHENTICATOR = new Subject(true, Collections.singleton(new SimplePrincipal(USER_AUTHENTICATOR)), Collections.emptySet(), Collections.emptySet());

  /**
   * Technical user to invoke JAX-WS handlers; used by {@link HandlerProxy}.
   */
  String CONFIG_USER_ANONYMOUS = "jaxws.user.anonymous";
  String USER_ANONYMOUS = ConfigIniUtility.getProperty(CONFIG_USER_ANONYMOUS, "jaxws-anonymous");
  Subject SUBJECT_ANONYMOUS = new Subject(true, Collections.singleton(new SimplePrincipal(USER_ANONYMOUS)), Collections.emptySet(), Collections.emptySet());

  /**
   * Security Realm used for Basic Authentication; used by {@link BasicAuthenticationMethod}.
   */
  String CONFIG_PROP_BASIC_AUTH_REALM = "jaxws.authentication.basic.realm";

  /**
   * Users granted to access webservices; used by {@link ConfigIniAuthenticator}.
   */
  String CONFIG_PROP_AUTH_USERS = "jaxws.authentication.users";

  /**
   * Qualified name of the {@link JaxWsImplementorSpecifics} to use.
   */
  String CONFIG_PROP_JAXWS_IMPLEMENTOR = "jaxws.implementor";

  /**
   * Indicates whether to log SOAP messages in debug or info level; used by {@link LogHandler}.
   */
  String CONFIG_PROP_LOGHANDLER_DEBUG = "jaxws.loghandler.debug";
}
