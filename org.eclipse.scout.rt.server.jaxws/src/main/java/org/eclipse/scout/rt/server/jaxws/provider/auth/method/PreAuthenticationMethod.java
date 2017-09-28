/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

import java.security.AccessController;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this authentication method when the request has already been reliably authenticated by some external system prior
 * to accessing the application, like authenticated by the application server, or a Servlet filter.
 *
 * @since 6.1
 */
public class PreAuthenticationMethod implements IAuthenticationMethod {

  private static final Logger LOG = LoggerFactory.getLogger(PreAuthenticationMethod.class);

  /**
   * Returns the {@link Subject} if already authenticated, e.g. by the application server or Servlet filter, or else
   * <code>null</code>.
   * <p>
   * If authenticated, {@link #authenticate(SOAPMessageContext, ICredentialVerifier, IPrincipalProducer)} will not be
   * invoked.
   *
   * @param servletRequest
   *          current servlet request.
   * @param principalProducer
   *          used to produce {@link Principal} objects for authenticated users.
   * @return authenticated {@link Subject}, or <code>null</code> if not authenticated yet.
   */
  public Subject getRequestSubject(final HttpServletRequest servletRequest, final IPrincipalProducer principalProducer) {
    // Check if already running within a Subject.
    if (BEANS.get(ServletFilterHelper.class).isRunningWithValidSubject(servletRequest)) {
      return Subject.getSubject(AccessController.getContext());
    }

    // Check if already authenticated.
    final Principal principalFound = BEANS.get(ServletFilterHelper.class).findPrincipal(servletRequest, principalProducer);
    if (principalFound != null) {
      return BEANS.get(ServletFilterHelper.class).createSubject(principalFound);
    }

    return null;
  }

  @Override
  public Principal authenticate(final SOAPMessageContext messageContext, final ICredentialVerifier credentialVerifier, final IPrincipalProducer principalProducer) throws Exception {
    LOG.warn("Request not authenticated. Pre-Authentication failed: missing remote user or not running in a JAAS context. Request rejected.");
    BEANS.get(JaxWsImplementorSpecifics.class).setHttpResponseCode(messageContext, HttpServletResponse.SC_FORBIDDEN);
    return null;
  }

  @Override
  public Set<QName> getAuthenticationHeaders() {
    return Collections.emptySet();
  }
}
