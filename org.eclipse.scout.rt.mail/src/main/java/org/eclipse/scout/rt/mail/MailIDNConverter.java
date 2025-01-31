/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail;

import java.net.IDN;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ApplicationScoped
public class MailIDNConverter {

  /**
   * Converts a email address with non-ASCII characters in the domain part into a ASCII compatible form using the
   * punycode encoding. <br>
   * Examples: <br>
   * someone@exämple.com -> someone@xn--exmple-cua.com <br>
   * sömeöne@example.com -> sömeöne@example.com
   *
   * @param email
   *     email address to convert.
   * @return punycode encoded email address.
   */
  public String toASCII(String email) {
    return convertToCharset(email, IDN::toASCII);
  }

  /**
   * Converts a punycode encoded email address into a Unicode String. <br>
   * Examples: <br>
   * someone@xn--exmple-cua.com -> someone@exämple.com
   *
   * @param email
   *     punycode encoded email address.
   * @return email address as Unicode String.
   */
  public String toUnicode(String email) {
    return convertToCharset(email, IDN::toUnicode);
  }

  /**
   * Helper-method to convert the email address into an IDN conform way using a function as the conversion method to
   * reduce boilerplate code.
   *
   * @param email
   *     email address
   * @param conversionMethod
   *     convert function
   * @return converted email address
   */
  protected String convertToCharset(String email, Function<String, String> conversionMethod) {
    int index = getSplitIndex(email);

    if (index < 0) {
      return email;
    }

    return getLocalPart(email, index) + "@" + conversionMethod.apply(getDomainPart(email, index));
  }

  /**
   * Helper method to extract the local part of the email. <br>
   * Example: <a href="mailto:someone@example.com">someone@example.com</a> - Local-part: someone; domain-part:
   * example.com <br>
   * Note: If the length of the local part is above 64, a {@link ProcessingException} is thrown.
   *
   * @param email
   *     full email address.
   * @param index
   *     index of the '@' symbol.
   * @return local-part of the email.
   * @throws IllegalArgumentException
   *     if the length of the local part is above 64 characters.
   */
  protected String getLocalPart(String email, int index) {
    String localPart = email.substring(0, index);

    if (localPart.length() > 64) {
      throw new IllegalArgumentException(
          String.format("The maximum length of a local part in a email address can not be above 64 characters,"
              + " the length of %s is %d", localPart, localPart.length()));
    }

    return localPart;
  }

  /**
   * Helper method to extract the domain part of the email. <br>
   * Example: <a href="mailto:someone@example.com">someone@example.com</a> - Local-part: someone; domain-part:
   * example.com
   *
   * @param email
   *     full email address.
   * @param index
   *     index of the '@' symbol.
   * @return domain-part of the email.
   * @throws IllegalArgumentException
   *     if the length of the local part is above 255 characters.
   */
  protected String getDomainPart(String email, int index) {
    String domainPart = email.substring(index + 1);

    if (domainPart.length() > 255) {
      throw new IllegalArgumentException(
          String.format("The maximum length of a domain part in a email address can not be above 255 characters,"
              + " the length of %s is %d", domainPart, domainPart.length()));
    }

    return domainPart;
  }

  /**
   * Finds the null-safe index of the '@' symbol in a given email address.
   *
   * @param email
   *     email address.
   * @return first index of the '@' symbol or {@code -1} if there is no occurrence or if the email is null or empty.
   */
  protected int getSplitIndex(String email) {
    if (StringUtility.isNullOrEmpty(email)) {
      return -1;
    }
    return email.indexOf("@");
  }
}
