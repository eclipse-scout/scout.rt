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
package org.eclipse.scout.rt.server.jaxws.provider.auth.handler;

import java.security.AccessController;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsAuthenticatorSubjectProperty;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.ClazzUtil;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.RunWithRunContext;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.context.RunContextProvider;

/**
 * <code>SOAPHandler</code> used to authenticate webservice requests based on the configured <i>Authentication
 * Method</i> and <i>Authenticator</i>. The <i>Authentication Method</i> challenges the client to provide credentials,
 * whereas the <i>Authenticator</i> validates the provided credentials against a data source. This handler is installed
 * as very first handler in the handler chain.
 *
 * @since 5.1
 */
public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AuthenticationHandler.class);
  protected static final Subject SUBJECT_AUTHENTICATOR = CONFIG.getPropertyValue(JaxWsAuthenticatorSubjectProperty.class);

  protected final IAuthenticationMethod m_authenticationMethod;
  protected final IAuthenticator m_authenticator;
  protected final RunContextProvider m_authenticatorRunContextProvider;
  protected final RunContextProvider m_requestRunContextProvider;

  public AuthenticationHandler(final Authentication authenticationAnnotation) {
    m_authenticationMethod = BEANS.get(ClazzUtil.resolve(authenticationAnnotation.method(), IAuthenticationMethod.class, "@Authentication.method"));
    m_authenticator = BEANS.get(ClazzUtil.resolve(authenticationAnnotation.authenticator(), IAuthenticator.class, "@Authentication.authenticator"));
    m_requestRunContextProvider = BEANS.get(ClazzUtil.resolve(authenticationAnnotation.runContextProvider(), RunContextProvider.class, "@Authentication.runContextProvider"));

    final RunWithRunContext authenticateWithRunContext = m_authenticator.getClass().getAnnotation(RunWithRunContext.class);
    m_authenticatorRunContextProvider = (authenticateWithRunContext != null ? BEANS.get(authenticateWithRunContext.provider()) : null);
  }

  @Override
  public boolean handleMessage(final SOAPMessageContext messageContext) {
    if (MessageContexts.isOutboundMessage(messageContext)) {
      return true;
    }

    try {
      // Check whether the request is already authenticated, e.g. by the container.
      final Subject currentSubject = Subject.getSubject(AccessController.getContext());
      if (currentSubject != null && !currentSubject.getPrincipals().isEmpty()) {
        MessageContexts.setRunContext(messageContext, m_requestRunContextProvider.provide(currentSubject));
        return true;
      }

      // Authenticate the request.
      final Subject subject = authenticateRequest(messageContext);
      if (subject != null) {
        MessageContexts.setRunContext(messageContext, m_requestRunContextProvider.provide(subject));
        return true;
      }
      else {
        return false;
      }
    }
    catch (final Exception e) {
      LOG.error("Unexpected error during webservice request authentication.", e);
      BEANS.get(JaxWsImplementorSpecifics.class).setHttpStatusCode(messageContext, HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // do not send cause to the client
      return false;
    }
  }

  @Override
  public boolean handleFault(final SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(final MessageContext context) {
    // NOOP
  }

  @Override
  public Set<QName> getHeaders() {
    return m_authenticationMethod.getAuthenticationHeaders();
  }

  /**
   * Method invoked to authenticate the current request.
   */
  protected Subject authenticateRequest(final SOAPMessageContext context) throws Exception {
    if (m_authenticatorRunContextProvider == null) {
      return m_authenticationMethod.authenticate(context, m_authenticator);
    }
    else {
      return m_authenticatorRunContextProvider.provide(SUBJECT_AUTHENTICATOR).call(new Callable<Subject>() {

        @Override
        public Subject call() throws Exception {
          return m_authenticationMethod.authenticate(context, m_authenticator);
        }
      });
    }
  }
}
