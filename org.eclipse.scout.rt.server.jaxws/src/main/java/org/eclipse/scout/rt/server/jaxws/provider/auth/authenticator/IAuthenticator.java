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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.jaxws.JaxWsConstants;

/**
 * Authenticator to authenticate webservice requests against a database, config.ini, LDAP or others. By annotating the
 * authenticator with <code>&#064;RunWithRunContext</code> annotation, this authenticator is invoked on behalf of a
 * {@link RunContext} with the user {@link JaxWsConstants#USER_AUTHENTICATOR}.
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
