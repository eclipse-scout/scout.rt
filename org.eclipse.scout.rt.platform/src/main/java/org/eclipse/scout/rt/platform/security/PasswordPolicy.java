/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.text.TEXTS;

/**
 * Default implementation of a password policy checking a password against a set of rules.
 *
 * @since 11.0
 */
@Bean
public class PasswordPolicy {

  public static final int MIN_PASSWORD_LENGTH = 12;

  protected boolean containsUsername(char[] newPassword, String userName) {
    if (userName == null) {
      return false;
    }
    StringBuilder b = new StringBuilder(newPassword.length);
    for (char c : newPassword) {
      b.append(Character.toUpperCase(c));
    }
    return b.indexOf(userName.toUpperCase()) >= 0;
  }

  protected boolean containsOneOf(char[] charsOfPasswordSorted, String charsToSearch) {
    for (char toFind : charsToSearch.toCharArray()) {
      if (Arrays.binarySearch(charsOfPasswordSorted, toFind) >= 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return a localized text that describes the policy to the user (may contain new lines)
   */
  public String getText() {
    return TEXTS.get("DefaultPasswordPolicyText");
  }

  /**
   * Checks password against a set of rules using given {@code userName} and {@code historyIndex} as hints for certain
   * rules (e.g. password may not contain the username and may not be reused).
   *
   * @throws VetoException
   *     if the password violates a rule. The exception message contains a human-readable text in the current locale.
   */
  public void check(String userName, char[] newPassword, int historyIndex) {
    if (newPassword == null || newPassword.length < MIN_PASSWORD_LENGTH) {
      throw new VetoException(TEXTS.get("PasswordMinLength"));
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
      throw new VetoException(TEXTS.get("PasswordMinOneLowercaseChar"));
    }
    if (!containsOneOf(charsInPasswordSorted, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) {
      throw new VetoException(TEXTS.get("PasswordMinOneUppercaseChar"));
    }
    if (!containsOneOf(charsInPasswordSorted, "!@#$%^&*()_+|~-=\\`{}[]:\";'<>?,./")) {
      throw new VetoException(TEXTS.get("PasswordMinOnNonStdChar"));
    }
    if (containsUsername(newPassword, userName)) {
      throw new VetoException(TEXTS.get("PasswordUsernameNotPartOfPass"));
    }
  }
}
