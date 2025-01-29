/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.pwd;

import java.util.Date;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * Common definition of a password management service that may be used by projects to manage a private set of users in
 * an application.
 * <p>
 * However, the use of external existing user administration for single-sign-in and password management is highly
 * recommended. <br>
 * This common definition solely is used when no on-site solution is available.
 */
@TunnelToServer
public interface IPasswordManagementService extends IService {

  /**
   * @return a localized text that describes the policy to the user (may contain new lines)
   */
  String getPasswordPolicyText();

  /**
   * @return when the password of the user will expire
   */
  Date getPasswordExpirationDate(String userId);

  /**
   * change the password of a user
   */
  void changePassword(String userId, char[] oldPassword, char[] newPassword);

  /**
   * reset the password of a user <br>
   * In the implementation make sure to use sufficient permission rights to enable this method.
   */
  void resetPassword(String userId, char[] newPassword);

  String getUsernameFor(String userId);
}
