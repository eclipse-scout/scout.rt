/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
 * <p>
 * In order to use, register it in the extension "org.eclipse.scout.rt.client.serviceProxy"
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

}
