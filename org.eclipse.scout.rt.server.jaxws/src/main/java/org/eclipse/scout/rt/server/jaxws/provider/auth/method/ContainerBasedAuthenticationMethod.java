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
import org.eclipse.scout.rt.server.commons.authentication.ICredentialVerifier;
import org.eclipse.scout.rt.server.commons.authentication.IPrincipalProducer;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this authentication method when using container based authentication, meaning that webservice requests are
 * authenticated by the application server, or a Servlet filter.
 *
 * @since 5.2
 */
public class ContainerBasedAuthenticationMethod implements IAuthenticationMethod {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerBasedAuthenticationMethod.class);

  @Override
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
    LOG.warn("Unauthenticated request despite container based authentication. Request rejected.");
    BEANS.get(JaxWsImplementorSpecifics.class).setHttpResponseCode(messageContext, HttpServletResponse.SC_FORBIDDEN);
    return null;
  }

  @Override
  public Set<QName> getAuthenticationHeaders() {
    return Collections.emptySet();
  }
}
