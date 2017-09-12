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
package org.eclipse.scout.rt.platform.util;

import java.util.Base64;

/**
 * Base64 encoding/decoding utility. The difference to {@link Base64} is that this utility is able to decode base64
 * strings that contain newline characters. These are removed before parsing.
 */
public final class Base64Utility {

  private Base64Utility() {
  }

  /**
   * Calls {@link #encode(byte[], boolean)} with false
   */
  public static String encode(byte[] bytes) {
    return encode(bytes, false);
  }

  /**
   * Calls {@link #encode(byte[], boolean)} with true
   */
  public static String encodeUrlSafe(byte[] bytes) {
    return encode(bytes, true);
  }

  /**
   * Base-64 encodes the supplied block of data. Line wrapping is not applied on output.
   *
   * @param bytes
   *          The block of data that is to be Base-64 encoded.
   * @param urlSafe
   *          The alphabet used for the regular base 64 encoding contains '/' and '+' which are not allowed in URLs.
   *          With this property set to true, these chars are replaced with '-' and '_' so that the generated string may
   *          be used for URLs. See also
   *          <a href="https://tools.ietf.org/html/rfc4648#page-7">https://tools.ietf.org/html/rfc4648</a>.
   * @return A <code>String</code> containing the encoded data.
   */
  public static String encode(byte[] bytes, boolean urlSafe) {
    if (urlSafe) {
      return Base64.getUrlEncoder().encodeToString(bytes);
    }
    return Base64.getEncoder().encodeToString(bytes);
  }

  /**
   * Calls {@link #decode(String, boolean)} with false.
   */
  public static byte[] decode(String string) {
    return decode(string, false);
  }

  /**
   * Calls {@link #decode(String, boolean)} with true.
   */
  public static byte[] decodeUrlSafe(String string) {
    return decode(string, true);
  }

  /**
   * Decodes the supplied Base-64 encoded string.
   *
   * @param string
   *          The Base-64 encoded string that is to be decoded.
   * @param urlSafe
   *          The alphabet used for the regular base 64 encoding contains '/' and '+' which are not allowed in URLs.
   *          With this property set to true, these chars are replaced with '-' and '_' so that the generated string may
   *          be used for URLs. See also
   *          <a href="https://tools.ietf.org/html/rfc4648#page-7">https://tools.ietf.org/html/rfc4648</a>.
   * @return A <code>byte[]</code> containing the decoded data block.
   */
  public static byte[] decode(String string, boolean urlSafe) {
    string = StringUtility.replaceNewLines(string, "");
    if (urlSafe) {
      return Base64.getUrlDecoder().decode(string);
    }
    return Base64.getDecoder().decode(string);
  }
}
