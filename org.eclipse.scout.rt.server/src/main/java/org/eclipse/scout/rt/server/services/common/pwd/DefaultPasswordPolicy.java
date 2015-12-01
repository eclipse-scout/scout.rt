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

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Title: BSI Scout V3 Copyright: Copyright (c) 2001,2009 BSI AG
 * 
 * @version 3.x
 */

public class DefaultPasswordPolicy implements IPasswordPolicy {

  private static final int MIN_PASSWORD_LENGTH = 8;

  @Override
  public String getText() {
    return ScoutTexts.get("DefaultPasswordPolicyText");
  }

  @Override
  @SuppressWarnings("null")
  public void check(String userId, String newPassword, String userName, int historyIndex) {
    if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
      throwFailure("PasswordMin8Chars");
    }
    if (!newPassword.matches(".*[0-9]+.*")) {
      throwFailure("PasswordMinOneDigit");
    }
    if (!newPassword.matches(".*[[a-z]|[A-Z]]+.*")) {
      throwFailure("PasswordMinOneChar");
    }
    if (!newPassword.matches(".*[!|@|#|\\$|%|\\^|&|\\*|\\(|\\)|_|\\+|\\||~|\\-|=|\\\\|`|\\{|\\}|\\[|\\]|:|\"|;|'|<|>|?|,|.|/]+.*")) {
      throwFailure("PasswordMinOnNonStdChar");
    }
    if (userName != null && newPassword.toUpperCase().indexOf(userName.toUpperCase()) >= 0) {
      throwFailure("PasswordUsernameNotPartOfPass");
    }
    if (historyIndex >= 0) {
      throwFailure("PasswordNotSameAsLasts");
    }
  }

  protected void throwFailure(String msgId) {
    throw new VetoException(ScoutTexts.get(msgId));
  }
}
