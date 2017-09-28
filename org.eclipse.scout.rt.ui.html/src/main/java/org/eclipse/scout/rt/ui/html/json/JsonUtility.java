/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

/**
 * Utility methods for working with JSON data.
 */
public final class JsonUtility {

  private JsonUtility() {
    // static access only
  }

  /**
   * Strips any comments from the given JSON string. Additionally, line-endings are normalized to <code>'\n'</code>,
   * unnecessary whitespace at the end of a line and empty lines are removed.
   * <p>
   * If an <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC-4627</a>-compliant JSON was enriched with comments, this
   * method can be used to convert it back to a compliant document. (It will not fix any invalid JSON document, tough).
   * <p>
   * The following types of comments are supported:
   *
   * <pre>
   * {
   *   "attribute": "value", <font color=green><b>// single line comment</b></font>
   *   "count": <font color=green><b>/* inline comment *&#47;</b></font> 1,
   *   <font color=green><b>/* multiline
   *      comment *&#47;</b></font> "empty": false
   * }
   * </pre>
   * <p>
   * See <a href="https://plus.google.com/+DouglasCrockfordEsq/posts/RK8qyGVaGSr">Douglas Crockford's comment</a> on the
   * topic: <blockquote><i>"I removed comments from JSON because I saw people were using them to hold parsing
   * directives, a practice which would have destroyed interoperability. I know that the lack of comments makes some
   * people sad, but it shouldn't.<br>
   * <br>
   * Suppose you are using JSON to keep configuration files, which you would like to annotate. Go ahead and insert all
   * the comments you like. Then pipe it through JSMin before handing it to your JSON parser.﻿"</i> </blockquote>
   */
  @SuppressWarnings("squid:ForLoopCounterChangedCheck")
  public static String stripCommentsFromJson(String input) {
    if (input == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    StringBuilder whitespaceBuffer = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char previousCharacter = (i == 0 ? 0 : input.charAt(i - 1));
      char currentCharacter = input.charAt(i);
      char nextCharacter = (i == (input.length() - 1) ? 0 : input.charAt(i + 1));

      // Add whitespace to a buffer (because me might want to ignore it at the end of a line)
      if (currentCharacter == ' ' || currentCharacter == '\t') {
        whitespaceBuffer.append(currentCharacter);
        continue;
      }
      // Handle end of line
      if (currentCharacter == '\r') {
        if (nextCharacter == '\n') {
          // Handle \r\n as \n
          continue;
        }
        // Handle \r as \n
        currentCharacter = '\n';
      }
      if (currentCharacter == '\n') {
        whitespaceBuffer = new StringBuilder(); // discard whitespace
        // Add line break (but not at the begin and not after another line break)
        if (result.length() != 0 && result.charAt(result.length() - 1) != '\n') {
          result.append(currentCharacter);
        }
        continue;
      }

      // Handle strings
      if (currentCharacter == '"' && previousCharacter != '\\') {
        // Flush whitespace to result
        result.append(whitespaceBuffer);
        whitespaceBuffer = new StringBuilder();
        result.append(currentCharacter);
        for (i++; i < input.length(); i++) {
          previousCharacter = input.charAt(i - 1);
          currentCharacter = input.charAt(i);
          nextCharacter = (i == (input.length() - 1) ? 0 : input.charAt(i + 1));
          result.append(currentCharacter);
          if (currentCharacter == '"' && previousCharacter != '\\') {
            break; // end of string
          }
        }
      }
      // Handle multi-line comments
      else if (currentCharacter == '/' && nextCharacter == '*') {
        for (i++; i < input.length(); i++) {
          previousCharacter = input.charAt(i - 1);
          currentCharacter = input.charAt(i);
          nextCharacter = (i == (input.length() - 1) ? 0 : input.charAt(i + 1));
          if (currentCharacter == '/' && previousCharacter == '*') {
            break; // end of multi-line comment
          }
        }
      }
      // Handle single-line comment
      else if (currentCharacter == '/' && nextCharacter == '/') {
        for (i++; i < input.length(); i++) {
          previousCharacter = input.charAt(i - 1);
          currentCharacter = input.charAt(i);
          nextCharacter = (i == (input.length() - 1) ? 0 : input.charAt(i + 1));
          if (nextCharacter == '\n' || nextCharacter == '\r') {
            break; // end of single-line comment
          }
        }
      }
      // regular character
      else {
        // Flush whitespace to result
        result.append(whitespaceBuffer);
        whitespaceBuffer = new StringBuilder();
        result.append(currentCharacter);
      }
    }
    return result.toString();
  }
}
