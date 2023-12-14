/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

import java.security.Principal;
import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;

/**
 * Functionality to challenge the webservice client to provide credentials, like <i>Basic Access Authentication</i> or
 * <i>UsernameToken Authentication</i>.
 *
 * @since 5.1
 */
@ApplicationScoped
public interface IAuthenticationMethod {

  /**
   * Challenges the client for authentication and verifies credentials against the given {@link ICredentialVerifier}.
   *
   * @param messageContext
   *          to access the SOAP message or HTTP headers.
   * @param credentialVerifier
   *          used to verify a user's credentials.
   * @param principalProducer
   *          used to produce {@link Principal} objects for authenticated users.
   * @return authenticated {@link Principal}, or <code>null</code> if forbidden.
   */
  @SuppressWarnings("squid:S00112")
  Principal authenticate(SOAPMessageContext messageContext, ICredentialVerifier credentialVerifier, IPrincipalProducer principalProducer) throws Exception;

  /**
   * @return Headers supported by this authentication method, or an empty {@link Set} if not used.
   */
  Set<QName> getAuthenticationHeaders();
}
