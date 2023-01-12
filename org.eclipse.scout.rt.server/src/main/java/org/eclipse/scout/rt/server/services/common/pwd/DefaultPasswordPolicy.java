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
import org.eclipse.scout.rt.platform.security.PasswordPolicy;

/**
 * @deprecated in 11.0, moved to {@link PasswordPolicy}
 */
@Deprecated
public class DefaultPasswordPolicy implements IPasswordPolicy {

  @Override
  public String getText() {
    return BEANS.get(PasswordPolicy.class).getText();
  }

  @Override
  public void check(String userId, char[] newPassword, String userName, int historyIndex) {
    BEANS.get(PasswordPolicy.class).check(userName, newPassword, historyIndex);
  }
}
