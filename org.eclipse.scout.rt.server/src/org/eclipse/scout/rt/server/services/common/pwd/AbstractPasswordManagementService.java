/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.pwd;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.pwd.IPasswordManagementService;
import org.eclipse.scout.service.AbstractService;

/**
 * This is an abstract implementation of the {@link IPasswordManagementService} using a common password policy.
 * <p>
 * In order to use, register your subclass in the extension "org.eclipse.scout.rt.server.service"
 */
public abstract class AbstractPasswordManagementService extends AbstractService implements IPasswordManagementService {
  private IPasswordPolicy m_passwordPolicy;

  public AbstractPasswordManagementService() {
    setPasswordPolicy(new DefaultPasswordPolicy());
  }

  @Override
  public void changePassword(String userId, String oldPassword, String newPassword) throws ProcessingException {
    checkAccess(userId, oldPassword);
    resetPassword(userId, newPassword);
  }

  @Override
  public void resetPassword(String userId, String newPassword) throws ProcessingException {
    getPasswordPolicy().check(userId, newPassword, getUsernameFor(userId), getHistoryIndexFor(userId, newPassword));
    resetPasswordInternal(userId, newPassword);
  }

  @Override
  public String getPasswordPolicyText() {
    return getPasswordPolicy().getText();
  }

  protected IPasswordPolicy getPasswordPolicy() {
    return m_passwordPolicy;
  }

  protected void setPasswordPolicy(IPasswordPolicy p) {
    m_passwordPolicy = p;
  }

  /**
   * @throws ProcessingException
   *           when the userId/password is invalid
   */
  protected abstract void checkAccess(String userId, String password) throws ProcessingException;

  protected abstract String getUsernameFor(String userId) throws ProcessingException;

  /**
   * @return the previous passwords of the user
   */
  protected abstract int getHistoryIndexFor(String userId, String password) throws ProcessingException;

  /**
   * Reset the password, all checks and verifications have already been passed.
   */
  protected abstract void resetPasswordInternal(String userId, String newPassword) throws ProcessingException;

}
