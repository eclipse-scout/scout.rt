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

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.server.jaxws.handler.LogHandler;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.ConfigFileAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.handler.HandlerProxy;

/**
 *
 */
public final class JaxWsConfigProperties {

  private JaxWsConfigProperties() {
  }

  /**
   * Technical user to validate user's credentials; used by {@link IAuthenticator}.
   */
  public static class JaxWsAuthenticatorUserProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "jaxws-authenticator";
    }

    @Override
    public String getKey() {
      return "jaxws.user.authenticator";
    }
  }

  /**
   * Indicates whether to log SOAP messages in debug or info level; used by {@link LogHandler}.
   */
  public static class JaxWsLogHandlerDebugProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jaxws.loghandler.debug";
    }
  }

  /**
   * Qualified name of the {@link JaxWsImplementorSpecifics} to use.
   */
  public static class JaxWsImplementorProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jaxws.implementor";
    }
  }

  /**
   * Users granted to access webservices; used by {@link ConfigFileAuthenticator}.
   */
  public static class JaxWsAuthUsersProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return null;
    }

    @Override
    public String getKey() {
      return "jaxws.authentication.users";
    }
  }

  /**
   * Security Realm used for Basic Authentication; used by {@link BasicAuthenticationMethod}.
   */
  public static class JaxWsBasicAuthRealmProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "JAX-WS";
    }

    @Override
    public String getKey() {
      return "jaxws.authentication.basic.realm";
    }
  }

  /**
   * Technical user to invoke JAX-WS handlers; used by {@link HandlerProxy}.
   */
  public static class JaxWsAnonymousUserProperty extends AbstractStringConfigProperty {

    @Override
    public String getDefaultValue() {
      return "jaxws-anonymous";
    }

    @Override
    public String getKey() {
      return "jaxws.user.anonymous";
    }
  }
}
