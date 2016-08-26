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
package org.eclipse.scout.rt.server.jaxws.provider.auth.handler;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.http.HTTPException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsAuthenticatorSubjectProperty;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.ClazzUtil;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.IAuthenticationMethod;
import org.eclipse.scout.rt.server.jaxws.provider.auth.method.PreAuthenticationMethod;

/**
 * <code>SOAPHandler</code> used to authenticate webservice requests based on the configured <i>Authentication
 * Method</i> and <i>Credential Verifier</i>. The <i>method</i> challenges the client to provide credentials, whereas
 * the <i>verifier</i> validates the provided credentials against a data source. This handler is installed as very first
 * handler in the handler chain.
 * <p>
 * If a request is already authenticated, e.g. by the container, the handler chain is continued. However, the
 * {@link RunContext} is still applied as configured by {@link Authentication}.
 * <p>
 * By annotating the {@link ICredentialVerifier} with {@link RunWithRunContext}, the credential verification is invoked
 * on behalf of a {@link RunContext} with the user set as configured in {@link JaxWsAuthenticatorSubjectProperty}. For
 * example, that allows authentication against a data source which requires a session.
 *
 * @since 5.1
 */
public class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

  protected static final Subject CREDENTIAL_AUTH_SUBJECT = CONFIG.getPropertyValue(JaxWsAuthenticatorSubjectProperty.class);

  protected IAuthenticationMethod m_authenticationMethod; // Strategy to do authentication.
  protected ICredentialVerifier m_credentialVerifier; // Strategy to verify user's credentials.
  protected IPrincipalProducer m_principalProducer; // Principal for authenticated users.

  protected RunContextProducer m_authRunContextProducer; // RunContext to run authentication.
  protected RunContextProducer m_runContextProducer; // RunContext to run subsequent handlers and the port type invocation.

  private final JaxWsImplementorSpecifics m_implementorSpecifics = BEANS.get(JaxWsImplementorSpecifics.class);

  /**
   * Use this constructor if installing an authentication handler yourself. That allows for dynamic instrumentation of
   * this handler, e.g. to use a different credential verifier depending on whether running in development or productive
   * environment.
   * <p>
   * {@link #setAuthenticationMethod(IAuthenticationMethod)}<br/>
   * {@link #setCredentialVerifier(ICredentialVerifier)}<br/>
   * {@link #setPrincipalProducer(IPrincipalProducer)}<br/>
   * {@link #setVerifierRunContextProducer(RunContextProducer)}<br/>
   * {@link #setRunContextProducer(RunContextProducer)}
   */
  public AuthenticationHandler() {
    // NOOP: The default implementation does nothing, and must be initialized by the implementing sub class.
  }

  /**
   * Used by generated JAX-WS authentication handler.
   */
  public AuthenticationHandler(final Authentication annotation) {
    setAuthenticationMethod(BEANS.get(ClazzUtil.resolve(annotation.method(), IAuthenticationMethod.class, "@Authentication.method")));
    setCredentialVerifier(BEANS.get(ClazzUtil.resolve(annotation.verifier(), ICredentialVerifier.class, "@Authentication.verifier")));
    setPrincipalProducer(BEANS.get(ClazzUtil.resolve(annotation.principalProducer(), IPrincipalProducer.class, "@Authentication.principalProducer")));
    setRunContextProducer(BEANS.get(ClazzUtil.resolve(annotation.runContextProducer(), RunContextProducer.class, "@Authentication.runContextProducer")));

    final RunWithRunContext runAuthWithRunContext = m_credentialVerifier.getClass().getAnnotation(RunWithRunContext.class);
    m_authRunContextProducer = (runAuthWithRunContext != null ? BEANS.get(runAuthWithRunContext.value()) : null);
  }

  @Override
  public boolean handleMessage(final SOAPMessageContext messageContext) {
    if (MessageContexts.isOutboundMessage(messageContext)) {
      return true;
    }

    try {
      final HttpServletRequest servletRequest = Assertions.assertNotNull(m_implementorSpecifics.getServletRequest(messageContext), "ServletRequest must not be null");

      // Check whether the request was pre-authenticated, e.g. by application server or Servlet filter.
      final Subject preAuthSubject = BEANS.get(PreAuthenticationMethod.class).getRequestSubject(servletRequest, m_principalProducer);
      if (preAuthSubject != null) {
        MessageContexts.putRunContext(messageContext, m_runContextProducer.produce(preAuthSubject));
        return true;
      }

      // Authenticate the request.
      final Principal principal = authenticateRequest(messageContext);
      if (principal != null) {
        final Subject subject = BEANS.get(ServletFilterHelper.class).createSubject(principal);
        BEANS.get(ServletFilterHelper.class).putPrincipalOnSession(servletRequest, principal);
        MessageContexts.putRunContext(messageContext, m_runContextProducer.produce(subject));
        return true;
      }

      // Ensure HTTP status code to be set.
      final int httpStatusCode;
      final Integer currentHttpResponseCode = m_implementorSpecifics.getHttpResponseCode(messageContext);
      if (currentHttpResponseCode != null) {
        httpStatusCode = currentHttpResponseCode.intValue();
      }
      else {
        httpStatusCode = HttpServletResponse.SC_FORBIDDEN;
        m_implementorSpecifics.setHttpResponseCode(messageContext, httpStatusCode);
      }

      m_implementorSpecifics.interceptWebServiceRequestRejected(messageContext, httpStatusCode);
      return false;
    }
    catch (final WebServiceRequestRejectedException e) { // NOSONAR
      throw new HTTPException(e.getHttpStatusCode());
    }
    catch (final Throwable t) {
      m_implementorSpecifics.setHttpResponseCode(messageContext, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

      // Log exception with information about the webservice request associated.
      BEANS.get(ExceptionHandler.class).handle(BEANS.get(PlatformExceptionTranslator.class).translate(t)
          .withContextInfo("service", messageContext.get(SOAPMessageContext.WSDL_SERVICE))
          .withContextInfo("port", messageContext.get(SOAPMessageContext.WSDL_PORT))
          .withContextInfo("operation", messageContext.get(SOAPMessageContext.WSDL_OPERATION)));

      try {
        m_implementorSpecifics.interceptWebServiceRequestRejected(messageContext, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      catch (final WebServiceRequestRejectedException e) { // NOSONAR
        throw new HTTPException(e.getHttpStatusCode()); // SECURITY: Do not propagate cause to the caller.
      }

      return false; // SECURITY: Do not propagate cause to the caller.
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
   * <p>
   * The default implementation delegates authentication to the configured {@link IAuthenticationMethod} and
   * {@link ICredentialVerifier}. Optionally, authentication is run on the configured {@link RunContext}.
   */
  protected Principal authenticateRequest(final SOAPMessageContext context) throws Exception {
    if (m_authRunContextProducer == null) {
      return m_authenticationMethod.authenticate(context, m_credentialVerifier, m_principalProducer);
    }
    else {
      return m_authRunContextProducer.produce(CREDENTIAL_AUTH_SUBJECT).call(new Callable<Principal>() {

        @Override
        public Principal call() throws Exception {
          return m_authenticationMethod.authenticate(context, m_credentialVerifier, m_principalProducer);
        }
      }, DefaultExceptionTranslator.class);
    }
  }

  protected void setAuthenticationMethod(final IAuthenticationMethod authenticationMethod) {
    m_authenticationMethod = authenticationMethod;
  }

  protected void setCredentialVerifier(final ICredentialVerifier credentialVerifier) {
    m_credentialVerifier = credentialVerifier;
  }

  protected void setPrincipalProducer(final IPrincipalProducer principalProducer) {
    m_principalProducer = principalProducer;
  }

  protected void setVerifierRunContextProducer(final RunContextProducer verifierRunContextProducer) {
    m_authRunContextProducer = verifierRunContextProducer;
  }

  protected void setRunContextProducer(final RunContextProducer runContextProducer) {
    m_runContextProducer = runContextProducer;
  }
}
