/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsBasicAuthRealmProperty;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

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
  public Principal authenticate(final SOAPMessageContext messageContext, final ICredentialVerifier credentialVerifier, final IPrincipalProducer principalProducer) throws Exception {
    final List<String> authHeaders = BEANS.get(JaxWsImplementorSpecifics.class).getHttpRequestHeader(messageContext, AUTH_BASIC_AUTHORIZATION);

    if (authHeaders.isEmpty()) {
      // Challenge the client to provide credentials.
      m_implementorSpecifics.setHttpResponseHeader(messageContext, AUTH_BASIC_AUTHENTICATE, "Basic realm=\"" + m_realm + "\"");
      m_implementorSpecifics.setHttpResponseCode(messageContext, HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }

    for (final String authHeader : authHeaders) {
      if (authHeader.startsWith(AUTH_BASIC_PREFIX)) {
        final String[] credentials = new String(Base64Utility.decode(authHeader.substring(AUTH_BASIC_PREFIX.length())), BASIC_ENCODING).split(":", 2);
        final String username = credentials[0];
        final int authStatus = credentialVerifier.verify(username, credentials[1].toCharArray());
        if (authStatus == ICredentialVerifier.AUTH_OK) {
          return principalProducer.produce(username);
        }
      }
    }

    m_implementorSpecifics.setHttpResponseCode(messageContext, HttpServletResponse.SC_FORBIDDEN);
    return null;
  }

  @Override
  public Set<QName> getAuthenticationHeaders() {
    return Collections.emptySet();
  }
}
