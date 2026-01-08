/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.HexUtility;

public class MagicBytePattern {

  protected final int m_pos;
  protected final byte[] m_bytes;
  protected final boolean[] m_skips;

  /**
   * Creates a new instance with a position offset of 0.
   *
   * @param hexMagic
   *     The byte pattern to check.
   *     <p>
   *     Supply a hex-coded string to define the pattern to check for. Bytes that should be skipped when checking the pattern can be defined by providing two consecutive 'x' chars.
   *     <p>
   *     The pattern is not case-sensitive, so use lower or upper case characters as your desire; you may also use spaces to structure longer patterns.
   *     <p>
   *     Examples:
   *     <ul>
   *       <li>"aa": The first checked byte must be -86</li>
   *       <li>"aabb": The first two checked bytes must be -86 and -69</li>
   *       <li>"aaxxbb": The first checked byte must be -86 followed by any byte followed by -69</li>
   *     </ul>
   *
   */
  public MagicBytePattern(String hexMagic) {
    this(0, hexMagic);
  }

  /**
   * Creates a new instance.
   *
   * @param pos
   *     The position offset in the content from which the supplied pattern is checked.
   * @param hexMagic
   *     The byte pattern to check.
   *     <p>
   *     Supply a hex-coded string to define the pattern to check for. Bytes that should be skipped when checking the pattern can be defined by providing two consecutive 'x' chars.
   *     <p>
   *     The pattern is not case-sensitive, so use lower or upper case characters as your desire; you may also use spaces to structure longer patterns.
   *     <p>
   *     Examples:
   *     <ul>
   *       <li>"aa": The first checked byte must be -86</li>
   *       <li>"aabb": The first two checked bytes must be -86 and -69</li>
   *       <li>"aaxxbb": The first checked byte must be -86 followed by any byte followed by -69</li>
   *     </ul>
   *
   */
  public MagicBytePattern(int pos, String hexMagic) {
    Assertions.assertNotNullOrEmpty(hexMagic, "hexMagic must not be empty");
    hexMagic = hexMagic.replaceAll(" ", "");
    Assertions.assertEqual(hexMagic.length() % 2, 0, "hexMagic must contain an even number of chars");

    hexMagic = hexMagic.toLowerCase();

    m_pos = pos;
    m_bytes = HexUtility.decode(hexMagic.replaceAll("xx", "00"));
    m_skips = new boolean[m_bytes.length];

    int patternPos = 0;
    int skipPos = hexMagic.indexOf('x', patternPos);

    while (skipPos > -1) {
      Assertions.assertEqual(skipPos % 2, 0, "Expect finding skip char at even char indexes");
      Assertions.assertEqual(hexMagic.charAt(skipPos + 1), 'x', "Skip chars must come in pairs");
      m_skips[skipPos / 2] = true;

      patternPos = skipPos + 2;
      skipPos = hexMagic.indexOf('x', patternPos);
    }
  }

  public int getPos() {
    return m_pos;
  }

  public int getLength() {
    return m_bytes.length;
  }

  public boolean matches(byte[] content) {
    for (int i = 0; i < m_bytes.length; i++) {
      if (m_skips[i]) {
        continue;
      }
      if (content[m_pos + i] != m_bytes[i]) {
        return false;
      }
    }
    return true;
  }
}
