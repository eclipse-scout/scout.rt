/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.scout.jaxws.security.provider;

import org.eclipse.scout.jaxws.annotation.ScoutTransaction;

/**
 * Authenticator to validate user's credentials against a database, config.ini, LDAP or others.<br/>
 * If the authenticator requires a transaction or session, mark it with the annotation {@link ScoutTransaction}.
 */
public interface IAuthenticator {

  /**
   * Method invoked to authenticate the given credentials.
   *
   * @param username
   *          the username to be validated
   * @param passwordPlainText
   *          the password to be validated
   * @return <code>true</code> if user's credentials are valid, or <code>false</code> otherwise.
   * @throws Exception
   *           throw exception if user's credentials cannot be validated.
   */
  boolean authenticate(String username, String passwordPlainText) throws Exception;

  /**
   * Authenticator that always evaluates to <code>true</code>.
   */
  static final class AcceptAny implements IAuthenticator {

    public static final IAuthenticator INSTANCE = new AcceptAny();

    private AcceptAny() {
    }

    @Override
    public boolean authenticate(String username, String passwordPlainText) throws Exception {
      return true;
    }
  }
}
