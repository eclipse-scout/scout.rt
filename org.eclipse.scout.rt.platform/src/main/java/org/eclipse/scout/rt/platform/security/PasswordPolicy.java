/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.security;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.text.TEXTS;

/**
 * This {@link Bean} replaces the former IPasswordPolicy and DefaultPasswordPolicy
 *
 * @since 11.0
 */
@Bean
public class PasswordPolicy {
  public static final int MIN_PASSWORD_LENGTH = 12;

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

  public String getText() {
    return TEXTS.get("DefaultPasswordPolicyText");
  }

  public void check(String userName, char[] newPassword, int historyIndex) {
    if (newPassword == null || newPassword.length < MIN_PASSWORD_LENGTH) {
      throw new VetoException(TEXTS.get("PasswordMin8Chars"));
    }
    if (historyIndex >= 0) {
      throw new VetoException(TEXTS.get("PasswordNotSameAsLasts"));
    }

    char[] charsInPasswordSorted = Arrays.copyOf(newPassword, newPassword.length);
    Arrays.sort(charsInPasswordSorted);
    if (!containsOneOf(charsInPasswordSorted, "0123456789")) {
      throw new VetoException(TEXTS.get("PasswordMinOneDigit"));
    }
    if (!containsOneOf(charsInPasswordSorted, "abcdefghijklmnopqrstuvwxyz")) {
      throw new VetoException(TEXTS.get("PasswordMinOneChar", "a-z"));
    }
    if (!containsOneOf(charsInPasswordSorted, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) {
      throw new VetoException(TEXTS.get("PasswordMinOneChar", "A-Z"));
    }
    if (!containsOneOf(charsInPasswordSorted, "!@#$%^&*()_+|~-=\\`{}[]:\";'<>?,./")) {
      throw new VetoException(TEXTS.get("PasswordMinOnNonStdChar"));
    }
    if (containsUsername(newPassword, userName)) {
      throw new VetoException(TEXTS.get("PasswordUsernameNotPartOfPass"));
    }
  }
}
