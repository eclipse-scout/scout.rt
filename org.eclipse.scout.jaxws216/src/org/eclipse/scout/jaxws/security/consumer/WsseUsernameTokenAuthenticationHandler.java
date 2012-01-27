/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.security.consumer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.service.AbstractWebServiceClient;

/**
 * <p>
 * Handler to include user's credentials in webservice requests by using Message Level WS-Security with UsernameToken
 * Authentication. This requires requests to provide a valid user name and password to access content. User's
 * credentials are included in SOAP message headers which also works across firewalls and proxy servers.
 * </p>
 * <p>
 * User's credentials are configured in config.ini by specifying the properties <code>#username</code> and
 * <code>#password</code> of the respective {@link AbstractWebServiceClient}.
 * </p>
 * <p>
 * However, the disadvantage of WSSE UsernameToken Authentication is that it transmits unencrypted passwords across the
 * network. Therefore, you only should use this authentication when you know that the connection between the client and
 * the server is secure. The connection should be established either over a dedicated line or by using Secure Sockets
 * Layer (SSL) encryption and Transport Layer Security (TLS).
 * </p>
 */
@ScoutTransaction
public class WsseUsernameTokenAuthenticationHandler implements IAuthenticationHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WsseUsernameTokenAuthenticationHandler.class);

  private static final String WSSE = "wsse";
  private static final String WS_SEC = "Security";
  private static final String USERNAME_TOKEN = "UsernameToken";
  private static final String USERNAME = "Username";
  private static final String PASSWORD = "Password";
  private static final String NAME_SPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

  protected String m_username;
  protected String m_password;

  public WsseUsernameTokenAuthenticationHandler() {
  }

  @Override
  public final boolean handleMessage(SOAPMessageContext context) {
    boolean outbound = TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
    if (!outbound) {
      return true; // only outbound messages are of interest
    }

    if (m_username == null || m_password == null) {
      throw new WebServiceException("Invalid credentials configured.");
    }

    try {
      SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();

      SOAPHeader header = envelope.getHeader();
      if (header == null) {
        header = envelope.addHeader();
      }
      installAuthHeader(header);
      return true;
    }
    catch (SOAPException e) {
      LOG.error("Unable to add wss security token", e);
      return false;
    }
  }

  protected void installAuthHeader(SOAPHeader header) throws SOAPException {
    SOAPElement security = header.addChildElement(WS_SEC, WSSE, NAME_SPACE_URI);
    SOAPElement userToken = security.addChildElement(USERNAME_TOKEN, WSSE);

    userToken.addChildElement(USERNAME, WSSE).addTextNode(m_username);
    userToken.addChildElement(PASSWORD, WSSE).addTextNode(m_password);
  }

  @Override
  public Set<QName> getHeaders() {
    Set<QName> headers = new HashSet<QName>();
    headers.add(new QName(NAME_SPACE_URI, WS_SEC));
    return headers;
  }

  @Override
  public void close(MessageContext context) {
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return false;
  }

  @Override
  public void setPassword(String password) {
    m_password = password;
  }

  @Override
  public void setUsername(String username) {
    m_username = username;
  }
}
