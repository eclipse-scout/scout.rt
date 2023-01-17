/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public final class TokenUtility {

  private TokenUtility() {
  }

  public static char[] toChars(byte[] bytes) {
    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
    char[] chars = new char[charBuffer.limit()];
    charBuffer.get(chars, 0, chars.length);
    return chars;
  }

  public static byte[] toBytes(char[] chars) {
    ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
    byte[] bytes = new byte[byteBuffer.limit()];
    byteBuffer.get(bytes, 0, bytes.length);
    return bytes;
  }
}
