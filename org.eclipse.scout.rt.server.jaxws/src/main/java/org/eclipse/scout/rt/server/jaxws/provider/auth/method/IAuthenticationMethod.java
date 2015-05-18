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
package org.eclipse.scout.rt.server.jaxws.provider.auth.method;

import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator.IAuthenticator;

/**
 * Functionality to challenge the webservice client to provide credentials, like <i>Basic Access Authentication</i> or
 * <i>UsernameToken Authentication</i>.
 *
 * @since 5.1
 */
@ApplicationScoped
public interface IAuthenticationMethod {

  /**
   * Challenges the client for authentication and authenticates credentials against the given {@link IAuthenticator}.
   *
   * @param messageContext
   *          <code>MessageContext</code> to access the SOAP message or HTTP headers.
   * @param authenticator
   *          <code>Authenticator</code> to be used to validate credentials
   * @return Authenticated {@link Subject}, or <code>null</code> if authentication failed.
   */
  Subject authenticate(SOAPMessageContext messageContext, IAuthenticator authenticator) throws Exception;

  /**
   * @return Headers supported by this authentication method, or an empty {@link Set} if not used.
   */
  Set<QName> getAuthenticationHeaders();
}
