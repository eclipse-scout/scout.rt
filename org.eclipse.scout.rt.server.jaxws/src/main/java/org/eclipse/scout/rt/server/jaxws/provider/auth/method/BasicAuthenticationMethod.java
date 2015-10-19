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
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsBasicAuthRealmProperty;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;

/**
 * Authentication method to apply <i>Basic Access Authentication</i>. This requires requests to provide a valid user
 * name and password to access content. User's credentials are transported in HTTP headers. Basic authentication also
 * works across firewalls and proxy servers.
 * <p>
 * However, the disadvantage of Basic authentication is that it transmits unencrypted base64-encoded passwords across
 * the network. Therefore, you only should use this authentication when you know that the connection between the client
 * and the server is secure. The connection should be established either over a dedicated line or by using Secure
 * Sockets Layer (SSL) encryption and Transport Layer Security (TLS).
 */
public class BasicAuthenticationMethod implements IAuthenticationMethod {

  protected static final String AUTH_BASIC_AUTHORIZATION = "authorization";
  protected static final String AUTH_BASIC_AUTHENTICATE = "WWW-Authenticate";
  protected static final String AUTH_BASIC_PREFIX = "Basic ";
  protected static final String BASIC_ENCODING = "ISO-8859-1";

  protected final JaxWsImplementorSpecifics m_implementorSpecifics;
  protected final String m_realm;

  public BasicAuthenticationMethod() {
    m_implementorSpecifics = BEANS.get(JaxWsImplementorSpecifics.class);
    m_realm = CONFIG.getPropertyValue(JaxWsBasicAuthRealmProperty.class);
  }

  @Override
  public Subject authenticate(final SOAPMessageContext context, final IAuthenticator authenticator) throws Exception {
    final List<String> authHeaders = BEANS.get(JaxWsImplementorSpecifics.class).getHttpRequestHeader(context, AUTH_BASIC_AUTHORIZATION);

    if (authHeaders.isEmpty()) {
      // Challenge the client to provide credentials.
      m_implementorSpecifics.setHttpResponseHeader(context, AUTH_BASIC_AUTHENTICATE, "Basic realm=\"" + m_realm + "\"");
      m_implementorSpecifics.setHttpResponseCode(context, HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }

    for (final String authHeader : authHeaders) {
      if (authHeader.startsWith(AUTH_BASIC_PREFIX)) {
        final String[] credentials = new String(Base64Utility.decode(authHeader.substring(AUTH_BASIC_PREFIX.length())), BASIC_ENCODING).split(":", 2);
        final String username = credentials[0];
        if (authenticator.authenticate(username, credentials[1])) {
          return new Subject(true, Collections.singleton(new SimplePrincipal(username)), Collections.emptySet(), Collections.emptySet());
        }
      }
    }

    m_implementorSpecifics.setHttpResponseCode(context, HttpServletResponse.SC_UNAUTHORIZED);
    return null;
  }

  @Override
  public Set<QName> getAuthenticationHeaders() {
    return Collections.emptySet();
  }
}
