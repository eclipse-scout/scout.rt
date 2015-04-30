/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.scout.jaxws.security.provider;

import java.security.AccessController;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.internal.JaxWsConstants;
import org.eclipse.scout.jaxws.internal.JaxWsHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Handler to protect your webservice with Message Level WS-Security with UsernameToken Authentication. This requires
 * requests to provide a valid user name and password to access content. User's credentials are included in SOAP message
 * headers.
 * <p/>
 * However, the disadvantage of WSSE UsernameToken Authentication is that it transmits unencrypted passwords across the
 * network. Therefore, you only should use this authentication when you know that the connection between the client and
 * the server is secure. The connection should be established either over a dedicated line or by using Secure Sockets
 * Layer (SSL) encryption and Transport Layer Security (TLS).
 */
public class WsseUsernameTokenAuthenticationHandler implements IAuthenticationHandler {

  private static final String WSSE = "wsse";
  private static final String WS_SEC = "Security";
  private static final String USERNAME_TOKEN = "UsernameToken";
  private static final String USERNAME = "Username";
  private static final String PASSWORD = "Password";
  private static final String NAME_SPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

  private final IAuthenticator m_authenticator;
  private final boolean m_transactionalAuthenticator;

  public WsseUsernameTokenAuthenticationHandler(final IAuthenticator authenticator) {
    m_authenticator = Assertions.assertNotNull(authenticator, "authenticator must not be null");
    m_transactionalAuthenticator = m_authenticator.getClass().getAnnotation(ScoutTransaction.class) != null;
  }

  @Override
  public final boolean handleMessage(final SOAPMessageContext context) {
    if (JaxWsHelper.isOutboundMessage(context)) {
      return true;
    }

    if (JaxWsHelper.isAuthenticated()) {
      return true;
    }

    final Entry<String, String> credentials = extractWsseCredentials(context);
    if (credentials == null) {
      return JaxWsHelper.reject401(context);
    }

    try {
      final String username = credentials.getKey();
      if (authenticateRequest(username, credentials.getValue(), m_authenticator, m_transactionalAuthenticator)) {
        final Subject subject = JaxWsHelper.assertValidAuthSubject(Subject.getSubject(AccessController.getContext()));
        subject.getPrincipals().add(new SimplePrincipal(username));
        subject.setReadOnly(); // seal the subject.
        JaxWsHelper.setContextSession(context, lookupServerSession(subject));
        return true;
      }
      else {
        return JaxWsHelper.reject401(context);
      }
    }
    catch (final Exception e) {
      return JaxWsHelper.reject500(context, e);
    }
  }

  @Override
  public Set<QName> getHeaders() {
    final Set<QName> headers = new HashSet<QName>();
    headers.add(new QName(NAME_SPACE_URI, WS_SEC));
    return headers;
  }

  @Override
  public void close(final MessageContext context) {
  }

  @Override
  public boolean handleFault(final SOAPMessageContext context) {
    return false;
  }

  /**
   * Method invoked to validate the given user's credentials.
   */
  protected boolean authenticateRequest(final String username, final String password, final IAuthenticator authenticator, final boolean transactional) throws Exception {
    if (transactional) {
      final Subject authSubject = createAuthenticatorSubject();

      final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent();
      serverRunContext.transactionScope(TransactionScope.REQUIRES_NEW);
      serverRunContext.subject(authSubject);
      serverRunContext.session(lookupServerSession(authSubject));

      return serverRunContext.call(new Callable<Boolean>() {

        @Override
        public Boolean call() throws Exception {
          return authenticator.authenticate(username, password);
        }
      });
    }
    else {
      return authenticator.authenticate(username, password);
    }
  }

  /**
   * Method invoked to lookup the server session for the given {@link Subject}, or to create a new server session if not
   * available.
   */
  @Internal
  protected IServerSession lookupServerSession(final Subject subject) throws ProcessingException {
    return BEANS.get(ServerSessionProviderWithCache.class).provide(ServerRunContexts.copyCurrent().subject(subject));
  }

  /**
   * Method invoked to create the Subject used for transactional authenticators to authenticate user's credentials.
   */
  @Internal
  protected Subject createAuthenticatorSubject() {
    final Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(JaxWsConstants.USER_AUTHENTICATOR));
    subject.setReadOnly();
    return subject;
  }

  /**
   * Method invoked to extract the WSSE-credentials from the {@link SOAPHeader}.
   *
   * @return {@link Entry} with username as key and password as value, or <code>null</code> if not found.
   */
  @Internal
  protected Entry<String, String> extractWsseCredentials(final SOAPMessageContext messageContext) {
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

  @Override
  public String getName() {
    return JaxWsConstants.AUTH_WSSE_NAME;
  }
}
