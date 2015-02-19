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
package org.eclipse.scout.jaxws.security;

import java.security.AccessController;

import javax.security.auth.Subject;
import javax.xml.ws.WebServiceException;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.jaxws.security.provider.ICredentialValidationStrategy;

/**
 * Helper to authenticate a request.
 * <p>
 * A request is considered authenticated, if it run's in a subject context with one principal set at minimum.
 * </p>
 */
public class Authenticator {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Authenticator.class);

  /**
   * Checks whether the call runs in a doAs Subject context with one principal set at minimum
   * 
   * @return true if the calls runs in a doAs context with one principal set at minimum
   */
  public static boolean isSubjectAuthenticated() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    return (subject != null && subject.getPrincipals().size() > 0);
  }

  /**
   * Authenticates a request with the given user's credential. If credentials are accepted by the given credential
   * validation strategy, a respective user principal is created and added to the subject. Finally, the subject is
   * marked readonly.
   * 
   * @param strategy
   *          credential validation strategy to validate a user's credential
   * @param username
   *          the username to be validated
   * @param passwordPlainText
   *          the password to be validated
   * @return true if authentication succeeds or false otherwise
   */
  public static boolean authenticateRequest(ICredentialValidationStrategy strategy, String username, String passwordPlainText) {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      throw new WebServiceException("Webservice request was blocked due to security reasons: request must run on behalf of a subject context.");
    }

    if (subject.getPrincipals().size() > 0) {
      return true; // request already authenticated
    }
    if (subject.isReadOnly()) {
      throw new WebServiceException("Unexpected. Authenticated principal cannot be added to subject as it is readonly.");
    }
    if (strategy == null) {
      LOG.warn("No credential validation strategy configured.");
      return false;
    }
    if (!StringUtility.hasText(username) || !StringUtility.hasText(passwordPlainText)) {
      return false;
    }
    try {
      if (strategy.isValidUser(username, passwordPlainText)) {
        subject.getPrincipals().add(new SimplePrincipal(username));
        subject.setReadOnly();
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("user credential validation failed", e);
    }
    return false;
  }
}
