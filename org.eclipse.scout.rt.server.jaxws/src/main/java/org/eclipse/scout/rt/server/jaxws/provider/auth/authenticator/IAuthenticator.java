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
package org.eclipse.scout.rt.server.jaxws.provider.auth.authenticator;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsAuthenticatorSubjectProperty;
import org.eclipse.scout.rt.server.jaxws.RunWithServerRunContext;

/**
 * Authenticator to authenticate webservice requests against a database, config.properties, LDAP, or others.
 * <p>
 * To authenticate credentials on behalf of a {@link RunContext}, annotate this class with
 * {@link RunWithServerRunContext}. That way, {@link #authenticate(String, String)} is invoked with the
 * {@link RunContext} provided and with the {@link Subject} as configured in {@link JaxWsAuthenticatorSubjectProperty}.
 *
 * @since 5.1
 */
@ApplicationScoped
public interface IAuthenticator {

  /**
   * Authenticates the given credentials.
   *
   * @return <code>true</code> if authenticated, <code>false</code> otherwise.
   */
  boolean authenticate(String username, String passwordPlainText) throws Exception;
}
