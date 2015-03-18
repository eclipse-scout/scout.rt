/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.scout.jaxws.security.provider;

import java.security.AccessController;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.jaxws.annotation.ScoutTransaction;
import org.eclipse.scout.jaxws.internal.JaxWsConstants;
import org.eclipse.scout.jaxws.internal.JaxWsHelper;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.service.ServiceUtility;

/**
 * Handler to protect your webservice with Basic Access Authentication. This requires requests to provide a valid user
 * name and password to access content. User's credentials are transported in HTTP headers. Basic authentication also
 * works across firewalls and proxy servers.
 * <p/>
 * However, the disadvantage of Basic authentication is that it transmits unencrypted base64-encoded passwords across
 * the network. Therefore, you only should use this authentication when you know that the connection between the client
 * and the server is secure. The connection should be established either over a dedicated line or by using Secure
 * Sockets Layer (SSL) encryption and Transport Layer Security (TLS).
 */
public class BasicAuthenticationHandler implements IAuthenticationHandler {

  private final IAuthenticator m_authenticator;
  private final boolean m_transactionalAuthenticator;

  public BasicAuthenticationHandler(final IAuthenticator authenticator) {
    m_authenticator = Assertions.assertNotNull(authenticator, "authenticator must not be null");
    m_transactionalAuthenticator = m_authenticator.getClass().getAnnotation(ScoutTransaction.class) != null;

    ServiceUtility.injectConfigProperties(this);
  }

  @Override
  public final boolean handleMessage(final SOAPMessageContext context) {
    if (JaxWsHelper.isOutboundMessage(context)) {
      return true;
    }

    if (JaxWsHelper.isAuthenticated()) {
      return true;
    }

    final String[] authorizationHeader = getAuthorizationHeader(context);
    if (authorizationHeader.length == 0) {
      installAuthHeader(context); // force consumer to include authentication information.
      return JaxWsHelper.reject401(context);
    }

    for (final String headerValue : authorizationHeader) {
      if (headerValue.startsWith(JaxWsConstants.AUTH_BASIC_PREFIX)) {
        try {
          final String[] credentials = new String(Base64Utility.decode(headerValue.substring(JaxWsConstants.AUTH_BASIC_PREFIX.length())), "ISO-8859-1").split(":", 2);
          final String username = credentials[0];

          if (authenticateRequest(username, credentials[1], m_authenticator, m_transactionalAuthenticator)) {
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
    }

    return JaxWsHelper.reject401(context);
  }

  @Override
  public final Set<QName> getHeaders() {
    return new HashSet<QName>();
  }

  @Override
  public final void close(final MessageContext context) {
  }

  @Override
  public final boolean handleFault(final SOAPMessageContext context) {
    return false;
  }

  /**
   * Method invoked to validate the given user's credentials.
   */
  protected boolean authenticateRequest(final String username, final String password, final IAuthenticator authenticator, final boolean transactional) throws Exception {
    if (transactional) {
      final Subject authSubject = createAuthenticatorSubject();

      final ServerJobInput input = ServerJobInput.defaults();
      input.setName("JAX-WS authentication");
      input.setSubject(authSubject);
      input.setSession(lookupServerSession(authSubject));

      return ServerJobs.runNow(new ICallable<Boolean>() {

        @Override
        public Boolean call() throws Exception {
          return authenticator.authenticate(username, password);
        }
      }, input);
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
    final ServerJobInput input = ServerJobInput.defaults().setName("JAX-WS Session").setSubject(subject);
    return OBJ.get(ServerSessionProviderWithCache.class).provide(input);
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
   * Method invoked to install the 'BASIC' authentication header to force authentication.
   */
  @Internal
  protected void installAuthHeader(final SOAPMessageContext context) {
    Map<String, List<String>> httpResponseHeaders = getHttpResponseHeaders(context);
    if (httpResponseHeaders == null) {
      httpResponseHeaders = new HashMap<String, List<String>>();
    }

    final List<String> basicAuthToken = new LinkedList<String>();
    basicAuthToken.add("Basic realm=\"" + getRealm() + "\"");
    httpResponseHeaders.put(JaxWsConstants.AUTH_BASIC_AUTHENTICATE, basicAuthToken);

    context.put(MessageContext.HTTP_RESPONSE_CODE, HttpServletResponse.SC_UNAUTHORIZED);
    context.put(MessageContext.HTTP_RESPONSE_HEADERS, httpResponseHeaders);
  }

  protected String getRealm() {
    return "Secure Area";
  }

  /**
   * Method invoked to read the 'authorization' headers from the HTTP-request.
   */
  @Internal
  protected String[] getAuthorizationHeader(final SOAPMessageContext context) {
    final Map<String, List<String>> httpRequestHeaderMap = getHttpRequestHeaders(context);
    if (httpRequestHeaderMap == null || httpRequestHeaderMap.size() == 0) {
      return new String[0];
    }

    // According to RFC 2616 header names are case-insensitive
    for (final String headerName : httpRequestHeaderMap.keySet()) {
      if (JaxWsConstants.AUTH_BASIC_AUTHORIZATION.equalsIgnoreCase(headerName)) {
        final List<String> headerValues = httpRequestHeaderMap.get(headerName);
        if (headerValues != null) {
          return headerValues.toArray(new String[headerValues.size()]);
        }
        return new String[0];
      }
    }
    return new String[0];
  }

  @Override
  public String getName() {
    return JaxWsConstants.AUTH_BASIC_NAME;
  }

  @Internal
  @SuppressWarnings("unchecked")
  protected Map<String, List<String>> getHttpRequestHeaders(final SOAPMessageContext context) {
    return (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
  }

  @Internal
  @SuppressWarnings("unchecked")
  protected Map<String, List<String>> getHttpResponseHeaders(final SOAPMessageContext context) {
    return (Map<String, List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS);
  }
}
