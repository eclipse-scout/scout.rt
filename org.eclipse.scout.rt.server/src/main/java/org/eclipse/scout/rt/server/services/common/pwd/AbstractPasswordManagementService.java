/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.pwd;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.security.PasswordPolicy;
import org.eclipse.scout.rt.shared.services.common.pwd.IPasswordManagementService;

/**
 * This is an abstract implementation of the {@link IPasswordManagementService} using a common password policy.
 * <p>
 * In order to use, register your subclass in the extension "org.eclipse.scout.rt.server.service"
 */
public abstract class AbstractPasswordManagementService implements IPasswordManagementService {
  private PasswordPolicy m_passwordPolicy;

  public AbstractPasswordManagementService() {
    setPasswordPolicy(BEANS.get(PasswordPolicy.class));
  }

  @Override
  public void changePassword(String userId, char[] oldPassword, char[] newPassword) {
    checkAccess(userId, oldPassword);
    resetPassword(userId, newPassword);
  }

  @Override
  public void resetPassword(String userId, char[] newPassword) {
    getPasswordPolicy().check(getUsernameFor(userId), newPassword, getHistoryIndexFor(userId, newPassword));
    resetPasswordInternal(userId, newPassword);
  }

  @Override
  public String getPasswordPolicyText() {
    return getPasswordPolicy().getText();
  }

  protected PasswordPolicy getPasswordPolicy() {
    return m_passwordPolicy;
  }

  protected void setPasswordPolicy(PasswordPolicy p) {
    m_passwordPolicy = p;
  }

  /**
   * @throws ProcessingException
   *     when the userId/password is invalid
   */
  protected abstract void checkAccess(String userId, char[] password);

  /**
   * @return the previous passwords of the user
   */
  protected abstract int getHistoryIndexFor(String userId, char[] password);

  /**
   * Reset the password, all checks and verifications have already been passed.
   */
  protected abstract void resetPasswordInternal(String userId, char[] newPassword);
}
