/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.json.JSONObject;

@ApplicationScoped
public class HtmlEntities {

  /**
   * Key = Escaped character name (e.g. {@code "&ouml;"})
   * <br>
   * Value = Unescaped character (e.g. {@code "ö"})
   */
  private final Map<String, String> m_namedCharacterReferenceMap = new HashMap<>();

  public HtmlEntities() {
    loadNamedCharacterReferenceMap();
  }

  protected void loadNamedCharacterReferenceMap() {
    String content;
    try (InputStream is = HtmlEntities.class.getResourceAsStream("entities.json")) {
      content = IOUtility.readStringUTF8(is);
    }
    catch (Exception e) {
      throw new ProcessingException("Unable to read entities.json", e);
    }
    JSONObject json = new JSONObject(content);
    for (String key : json.keySet()) {
      m_namedCharacterReferenceMap.put(key, json.getJSONObject(key).getString("characters"));
    }
  }

  protected Map<String, String> getNamedCharacterReferenceMap() {
    return m_namedCharacterReferenceMap;
  }

  /**
   * Converts every valid <a href="https://html.spec.whatwg.org/multipage/syntax.html#character-references">character reference</a>
   * (named or numeric) in the given string to its corresponding Unicode character and returns the result. Invalid or incomplete
   * references are left unchanged. If the given string is {@code null}, {@code null} is returned.
   */
  public String unescapeAll(String s) {
    if (s == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder(s);
    int start = 0;

    while (start < sb.length()) {
      start = sb.indexOf("&", start);
      if (start == -1) {
        break;
      }
      int end = sb.indexOf(";", start);
      if (end == -1) {
        break;
      }

      String encoded = sb.substring(start, end + 1);
      String decoded = decodeNamedCharacterReference(encoded); // Named character reference
      if (decoded == null) {
        decoded = decodeNumericCharacterReference(encoded); // Numeric character reference
      }
      if (decoded == null) {
        start = end + 1;
      }
      else {
        sb.replace(start, end + 1, decoded);
        start = start + decoded.length();
      }
    }

    return sb.toString();
  }

  /**
   * Converts a single named character reference of the form <tt>"&<i>name</i>;"</tt> to its corresponding Unicode character.
   * If no mapping exists for the given string, {@code null} is returned.
   */
  public String decodeNamedCharacterReference(String ref) {
    return m_namedCharacterReferenceMap.get(ref);
  }

  /**
   * Converts a single <a href="https://en.wikipedia.org/wiki/Numeric_character_reference">numeric character reference</a> of
   * the form <tt>"&#<i>decimalValue</i>;"</tt> or <tt>"&#x<i>hexValue</i>;"</tt> to its corresponding Unicode character.
   * If the given string does not match the format or the value is not a valid code point, {@code null} is returned.
   */
  public String decodeNumericCharacterReference(String ref) {
    try {
      if (ref.startsWith("&#x") || ref.startsWith("&#X")) {
        String hex = ref.substring(3, ref.length() - 1);
        return Character.toString(Integer.parseInt(hex, 16));
      }
      if (ref.startsWith("&#")) {
        String decimal = ref.substring(2, ref.length() - 1);
        return Character.toString(Integer.parseInt(decimal));
      }
    }
    catch (Exception e) {
      // invalid character reference -> ignore (don't replace entity)
    }
    return null;
  }
}
