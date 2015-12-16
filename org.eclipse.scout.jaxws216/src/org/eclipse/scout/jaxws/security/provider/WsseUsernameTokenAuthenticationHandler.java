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
package org.eclipse.scout.jaxws.security.provider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.http.HTTPException;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.internal.ContextHelper;
import org.eclipse.scout.jaxws.internal.SessionHelper;
import org.eclipse.scout.jaxws.security.Authenticator;
import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.service.ServiceUtility;

/**
 * <p>
 * Handler to protect your webservice with Message Level WS-Security with UsernameToken Authentication. This requires
 * requests to provide a valid user name and password to access content. User's credentials are included in SOAP message
 * headers.
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

  private ICredentialValidationStrategy m_credentialValidationStrategy;

  public WsseUsernameTokenAuthenticationHandler() {
    ServiceUtility.injectConfigProperties(this);
  }

  @Override
  public final boolean handleMessage(SOAPMessageContext context) {
    boolean outbound = TypeCastUtility.castValue(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY), boolean.class);
    if (outbound) {
      return true; // only inbound messages are of interest
    }

    if (Authenticator.isSubjectAuthenticated()) {
      return true;
    }

    try {
      SOAPHeader header = context.getMessage().getSOAPPart().getEnvelope().getHeader();
      if (header == null) {
        throw new WebServiceException("Authentication failed as no WSSE-Security header found.");
      }
      if (authenticateRequest(header)) {
        // create and cache a new server session on behalf of the authenticated user by using the session factory configured on the port type.
        // In turn, this session is used by subsequent handlers and the port type resolver.
        IServerSessionFactory portTypeSessionFactory = ContextHelper.getPortTypeSessionFactory(context);
        IServerSession serverSession = SessionHelper.createNewServerSession(portTypeSessionFactory);
        if (serverSession != null) {
          // cache session to be used by subsequent handlers and port type resolver
          ContextHelper.setContextSession(context, portTypeSessionFactory, serverSession);
        }
        return true;
      }
      return breakHandlerChain(context, HttpServletResponse.SC_UNAUTHORIZED);
    }
    catch (HTTPException e) {
      throw e;
    }
    catch (Exception e) {
      return breakHandlerChainWithException(context, e);
    }
  }

  @Override
  public void injectCredentialValidationStrategy(ICredentialValidationStrategy strategy) {
    m_credentialValidationStrategy = strategy;
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

  protected boolean authenticateRequest(SOAPHeader header) throws Exception {
    Iterator iteratorSecurity = header.getChildElements(new QName(NAME_SPACE_URI, WS_SEC, WSSE));
    if (!iteratorSecurity.hasNext()) {
      throw new WebServiceException("Authentication failed as no WSSE-Security header found.");
    }

    while (iteratorSecurity.hasNext()) {
      SOAPElement security = (SOAPElement) iteratorSecurity.next();
      Iterator iteratorUserToken = security.getChildElements(new QName(NAME_SPACE_URI, USERNAME_TOKEN, WSSE));
      while (iteratorUserToken.hasNext()) {
        SOAPElement userTokenElement = (SOAPElement) iteratorUserToken.next();

        Iterator iteratorUsername = userTokenElement.getChildElements(new QName(NAME_SPACE_URI, USERNAME, WSSE));
        Iterator iteratorPassword = userTokenElement.getChildElements(new QName(NAME_SPACE_URI, PASSWORD, WSSE));

        if (iteratorUsername.hasNext() && iteratorPassword.hasNext()) {
          SOAPElement usernameElement = (SOAPElement) iteratorUsername.next();
          SOAPElement passwordElement = (SOAPElement) iteratorPassword.next();

          if (usernameElement != null && passwordElement != null) {
            String username = usernameElement.getValue();
            String passwordPlainText = passwordElement.getValue();
            return Authenticator.authenticateRequest(m_credentialValidationStrategy, username, passwordPlainText);
          }
        }
      }
    }
    return false;
  }

  protected boolean breakHandlerChain(SOAPMessageContext context, int httpStatusCode) {
    context.put(MessageContext.HTTP_RESPONSE_CODE, httpStatusCode);

    // JAX-WS METRO v2.2.10 does not exit the call chain if the Handler returns with 'false'.
    // That happens for one-way communication requests. As a result, the endpoint operation is still invoked.
    throw new HTTPException(httpStatusCode);
  }

  protected boolean breakHandlerChainWithException(SOAPMessageContext context, Exception exception) {
    context.put(MessageContext.HTTP_RESPONSE_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    LOG.error("Internal server error (Message Level WS-Security with UsernameToken authentication)", exception);

    if (exception instanceof WebServiceException) {
      throw (WebServiceException) exception;
    }
    throw new WebServiceException("Internal server error");
  }

  @Override
  public String getName() {
    return "WSSE Username Token";
  }
}
