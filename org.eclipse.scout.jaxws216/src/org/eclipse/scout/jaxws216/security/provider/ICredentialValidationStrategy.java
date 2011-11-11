/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws216.security.provider;

/**
 * Encapsulates logic to validate user's credentials against a strategy such as database, config.ini, LDAP or others.
 */
public interface ICredentialValidationStrategy {

  /**
   * Validates user's credentials
   * 
   * @param username
   *          the username to be validated
   * @param passwordPlainText
   *          the password to be validated
   * @return true if user's credentials are valid or false otherwise
   * @throws Exception
   *           throw exception if user's credentials cannot be validated.
   */
  boolean isValidUser(String username, String passwordPlainText) throws Exception;
}
