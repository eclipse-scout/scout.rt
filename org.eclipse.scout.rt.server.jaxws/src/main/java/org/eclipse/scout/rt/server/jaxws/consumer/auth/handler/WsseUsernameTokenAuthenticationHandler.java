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
package org.eclipse.scout.rt.server.jaxws.consumer.auth.handler;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.consumer.InvocationContext;

/**
 * Handler to include user's credentials in webservice requests by using Message Level WS-Security with UsernameToken
 * Authentication. This requires requests to provide a valid user name and password to access content. User's
 * credentials are included in SOAP message headers which also works across firewalls and proxy servers.
 * <p>
 * However, the disadvantage of WSSE UsernameToken Authentication is that it transmits unencrypted passwords across the
 * network. Therefore, you only should use this authentication when you know that the connection between the client and
 * the server is secure. The connection should be established either over a dedicated line or by using Secure Sockets
 * Layer (SSL) encryption and Transport Layer Security (TLS).
 */
public class WsseUsernameTokenAuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

  private static final String WSSE = "wsse";
  private static final String WS_SEC = "Security";
  private static final String USERNAME_TOKEN = "UsernameToken";
  private static final String USERNAME = "Username";
  private static final String PASSWORD = "Password";
  private static final String NAME_SPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

  @Override
  public final boolean handleMessage(final SOAPMessageContext messageContext) {
    if (MessageContexts.isInboundMessage(messageContext)) {
      return true;
    }

    SOAPEnvelope envelope;
    SOAPHeader header;
    try {
      envelope = messageContext.getMessage().getSOAPPart().getEnvelope();
      header = envelope.getHeader();

      if (header == null) {
        header = envelope.addHeader();
      }

      final SOAPElement security = header.addChildElement(WS_SEC, WSSE, NAME_SPACE_URI);
      final SOAPElement userToken = security.addChildElement(USERNAME_TOKEN, WSSE);

      userToken.addChildElement(USERNAME, WSSE).addTextNode(StringUtility.valueOf(messageContext.get(InvocationContext.PROP_USERNAME)));
      userToken.addChildElement(PASSWORD, WSSE).addTextNode(StringUtility.valueOf(messageContext.get(InvocationContext.PROP_PASSWORD)));

      return true;
    }
    catch (final SOAPException e) {
      throw new WebServiceException("Failed to set SOAP header for WsseUsernameTokenAuthentication", e);
    }
  }

  @Override
  public Set<QName> getHeaders() {
    return Collections.singleton(new QName(NAME_SPACE_URI, WS_SEC));
  }

  @Override
  public void close(final MessageContext context) {
    // NOOP
  }

  @Override
  public boolean handleFault(final SOAPMessageContext context) {
    return true;
  }
}
