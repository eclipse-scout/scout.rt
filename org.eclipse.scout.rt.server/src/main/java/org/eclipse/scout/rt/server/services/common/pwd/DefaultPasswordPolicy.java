/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.pwd;

import java.util.Arrays;

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
  public void check(String userId, char[] newPassword, String userName, int historyIndex) {
    if (newPassword == null || newPassword.length < MIN_PASSWORD_LENGTH) {
      throw new VetoException(ScoutTexts.get("PasswordMin8Chars"));
    }
    if (historyIndex >= 0) {
      throw new VetoException(ScoutTexts.get("PasswordNotSameAsLasts"));
    }

    char[] charsInPasswordSorted = Arrays.copyOf(newPassword, newPassword.length);
    Arrays.sort(charsInPasswordSorted);
    if (!containsOneOf(charsInPasswordSorted, "0123456789")) {
      throw new VetoException(ScoutTexts.get("PasswordMinOneDigit"));
    }
    if (!containsOneOf(charsInPasswordSorted, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")) {
      throw new VetoException(ScoutTexts.get("PasswordMinOneChar"));
    }
    if (!containsOneOf(charsInPasswordSorted, "!@#$%^&*()_+|~-=\\`{}[]:\";'<>?,./")) {
      throw new VetoException(ScoutTexts.get("PasswordMinOnNonStdChar"));
    }
    if (containsUsername(newPassword, userName)) {
      throw new VetoException(ScoutTexts.get("PasswordUsernameNotPartOfPass"));
    }
  }

  private static boolean containsUsername(char[] newPassword, String userName) {
    if (userName == null) {
      return false;
    }
    StringBuilder b = new StringBuilder(newPassword.length);
    for (char c : newPassword) {
      b.append(Character.toUpperCase(c));
    }
    return b.indexOf(userName.toUpperCase()) >= 0;
  }

  private static boolean containsOneOf(char[] charsOfPasswordSorted, String charsToSearch) {
    for (char toFind : charsToSearch.toCharArray()) {
      if (Arrays.binarySearch(charsOfPasswordSorted, toFind) >= 0) {
        return true;
      }
    }
    return false;
  }
}
