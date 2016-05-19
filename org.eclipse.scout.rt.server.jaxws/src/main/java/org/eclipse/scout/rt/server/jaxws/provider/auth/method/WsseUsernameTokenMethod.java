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
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

import java.security.Principal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

/**
 * Authentication method to apply <i>Message Level WS-Security with UsernameToken Authentication</i>. This requires
 * requests to provide a valid user name and password to access content. User's credentials are included in SOAP message
 * headers.
 * <p>
 * However, the disadvantage of WSSE UsernameToken Authentication is that it transmits unencrypted passwords across the
 * network. Therefore, you only should use this authentication when you know that the connection between the client and
 * the server is secure. The connection should be established either over a dedicated line or by using Secure Sockets
 * Layer (SSL) encryption and Transport Layer Security (TLS).
 */
public class WsseUsernameTokenMethod implements IAuthenticationMethod {

  protected static final String WSSE = "wsse";
  protected static final String WS_SEC = "Security";
  protected static final String USERNAME_TOKEN = "UsernameToken";
  protected static final String USERNAME = "Username";
  protected static final String PASSWORD = "Password";
  protected static final String NAME_SPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

  protected final JaxWsImplementorSpecifics m_implementorSpecifics;

  public WsseUsernameTokenMethod() {
    m_implementorSpecifics = BEANS.get(JaxWsImplementorSpecifics.class);
  }

  @Override
  public Principal authenticate(final SOAPMessageContext messageContext, final ICredentialVerifier credentialVerifier, final IPrincipalProducer principalProducer) throws Exception {
    final Entry<String, String> credentials = readWsseCredentials(messageContext);
    if (credentials == null) {
      m_implementorSpecifics.setHttpResponseCode(messageContext, HttpServletResponse.SC_UNAUTHORIZED);
      return null;
    }

    final String username = credentials.getKey();
    final int authStatus = credentialVerifier.verify(username, credentials.getValue().toCharArray());
    if (authStatus == ICredentialVerifier.AUTH_OK) {
      return principalProducer.produce(username);
    }
    else {
      m_implementorSpecifics.setHttpResponseCode(messageContext, HttpServletResponse.SC_FORBIDDEN);
      return null;
    }
  }

  @Override
  public Set<QName> getAuthenticationHeaders() {
    return Collections.singleton(new QName(NAME_SPACE_URI, WS_SEC));
  }

  /**
   * Method invoked to extract the WSSE-credentials from the {@link SOAPHeader}.
   *
   * @return {@link Entry} with username as key and password as value, or <code>null</code> if not found.
   */
  @Internal
  protected Entry<String, String> readWsseCredentials(final SOAPMessageContext messageContext) {
    SOAPHeader header;
    try {
      header = messageContext.getMessage().getSOAPPart().getEnvelope().getHeader();
    }
    catch (final SOAPException e) {
      throw new WebServiceException("Failed to read SOAP envelope", e);
    }

    if (header == null) {
      throw new WebServiceException("Missing WSSE-security header");
    }

    final Iterator iterator = header.getChildElements(new QName(NAME_SPACE_URI, WS_SEC, WSSE));
    while (iterator.hasNext()) {
      final SOAPElement security = (SOAPElement) iterator.next();
      final Iterator iteratorUserToken = security.getChildElements(new QName(NAME_SPACE_URI, USERNAME_TOKEN, WSSE));
      while (iteratorUserToken.hasNext()) {
        final SOAPElement userTokenElement = (SOAPElement) iteratorUserToken.next();

        final Iterator iteratorUsername = userTokenElement.getChildElements(new QName(NAME_SPACE_URI, USERNAME, WSSE));
        final Iterator iteratorPassword = userTokenElement.getChildElements(new QName(NAME_SPACE_URI, PASSWORD, WSSE));

        if (iteratorUsername.hasNext() && iteratorPassword.hasNext()) {
          final SOAPElement usernameElement = (SOAPElement) iteratorUsername.next();
          final SOAPElement passwordElement = (SOAPElement) iteratorPassword.next();

          if (usernameElement != null && passwordElement != null) {
            return new SimpleEntry<>(usernameElement.getValue(), passwordElement.getValue());
          }
        }
      }
    }
    return null;
  }
}
