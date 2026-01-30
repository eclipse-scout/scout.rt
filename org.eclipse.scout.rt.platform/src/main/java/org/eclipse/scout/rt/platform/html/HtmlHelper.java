/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * @since 5.2
 */
@ApplicationScoped
public class HtmlHelper {

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  private static final Pattern HTML_PARAGRAPH_END_TAGS = Pattern.compile("<br/?></div>|</div>|<br/?>|</p>|<p/>|</tr>|</h[1-6]>|</dt>|</dd>|</dl>|</li>|</head>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_SPACE_END_TAGS = Pattern.compile("</td>|</th>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_TAGS = Pattern.compile("<[^\\s>][^>]*>", Pattern.DOTALL);
  private static final Pattern HTML_SCRIPTS = Pattern.compile("<script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_STYLES = Pattern.compile("<style\\b[^<]*(?:(?!</style>)<[^<]*)*</style>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?--!?>", Pattern.DOTALL);
  private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ ]+");
  private static final Pattern SPACES_ADJACENT_LINEBREAKS = Pattern.compile("[ ]+\n[ ]?|[ ]?\n[ ]+");

  /**
   * Very basic HTML to plain text conversion, without parsing and building a model.
   * <p>
   * The following rules are applied:
   * <ul>
   * <li>If the string contains a valid body tag (something between <code>&lt;body&gt;</code> and
   * <code>&lt;/body&gt;</code>), only plain text of the body's content is returned. Otherwise, the plain text of the
   * entire string is returned.
   * <li><code>null</code> is only returned if the input is <code>null</code>. If no plain text is contained, the empty
   * string (<code>""</code>) is returned.
   * <li>The following tags are considered "end of paragraph" and are converted to <code>\n</code>:
   * <ul>
   * <li><code>&lt;br&gt;&lt;/div&gt;</code>
   * <li><code>&lt;br/&gt;&lt;/div&gt;</code>
   * <li><code>&lt;/div&gt;</code>
   * <li><code>&lt;br&gt;</code>
   * <li><code>&lt;br/&gt;</code> *
   * <li><code>&lt;/p&gt;</code>
   * <li><code>&lt;p/&gt;</code>
   * <li><code>&lt;/tr&gt;</code>
   * <li><code>&lt;/h1&gt;</code>
   * <li><code>&lt;/h2&gt;</code>
   * <li><code>&lt;/h3&gt;</code>
   * <li><code>&lt;/h4&gt;</code>
   * <li><code>&lt;/h5&gt;</code>
   * <li><code>&lt;/h6&gt;</code>
   * <li><code>&lt;/dt&gt;</code>
   * <li><code>&lt;/dd&gt;</code>
   * <li><code>&lt;/dl&gt;</code>
   * <li><code>&lt;/table&gt;</code>
   * <li><code>&lt;/li&gt;</code>
   * <li><code>&lt;/head&gt;</code>
   * </ul>
   * <li>All other tags are removed.
   * <li>Multiple consecutive spaces are merged to one space.
   * <li>Leading and trailing whitespace line is removed from each line.
   * </ul>
   * <p>
   */
  public String toPlainText(String html) {
    String s = toPlainTextNoTrim(html);
    if (s != null) {
      s = s.trim();
    }
    return s;
  }

  /**
   * Same as <code>toPlainText(String)</code>, except that leading and trailing whitespace is preserved.
   */
  public String toPlainTextNoTrim(String html) {
    if (html == null || html.isEmpty()) {
      return html;
    }
    String s = StringUtility.getTag(html, "body", true);
    if (s == null) {
      // <body> not found, use entire input
      s = html;
    }
    // remove comments
    s = HTML_COMMENT.matcher(s).replaceAll("");
    // remove attribute values since they could contain special characters like >
    s = removeAttributeValues(s);
    // whitespace
    s = StringUtility.replace(s, "\r", "");
    s = StringUtility.replace(s, "\n", " ");
    s = HTML_SPACE_END_TAGS.matcher(s).replaceAll(" ");
    // newlines
    s = HTML_PARAGRAPH_END_TAGS.matcher(s).replaceAll("\n");
    // remove script and style contents
    s = HTML_SCRIPTS.matcher(s).replaceAll("");
    s = HTML_STYLES.matcher(s).replaceAll("");
    // remove tags
    s = HTML_TAGS.matcher(s).replaceAll("");
    // remove multiple spaces
    s = MULTIPLE_SPACES.matcher(s).replaceAll(" ");
    // remove spaces at the beginning and end of each line
    s = SPACES_ADJACENT_LINEBREAKS.matcher(s).replaceAll("\n");

    // character references
    s = BEANS.get(HtmlEntities.class).unescapeAll(s);

    // convert non-breaking spaces to normal spaces
    s = s.replaceAll("\u00A0", " ");

    return s;
  }

  /**
   * Escapes the given string for use in HTML code. Useful when inserting data from an untrusted source directly inside
   * HTML. This method does not alter whitespace.
   * <p>
   * According to <a href=
   * "https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into_HTML_Element_Content">
   * OWASP recommendations</a>, the following characters are replaced:
   * <ul>
   * <li><code>&amp;</code> --> <code>&amp;amp;</code>
   * <li><code>&lt;</code> --> <code>&amp;lt;</code>
   * <li><code>&gt;</code> --> <code>&amp;gt;</code>
   * <li><code>&quot;</code> --> <code>&amp;quot;</code>
   * <li><code>&#39;</code> --> <code>&amp;#39;</code>
   * <li><code>&#47;</code> --> <code>&amp;#47;</code>
   * </ul>
   *
   * @see "https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet"
   */
  public String escape(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    text = StringUtility.replace(text, "&", "&amp;");
    text = StringUtility.replace(text, "<", "&lt;");
    text = StringUtility.replace(text, ">", "&gt;");
    text = StringUtility.replace(text, "\"", "&quot;");
    text = StringUtility.replace(text, "/", "&#47;");
    text = StringUtility.replace(text, "'", "&#39;");
    return text;
  }

  /**
   * Reverse operation of {@link #escape(String)}. This method does not alter whitespace.
   */
  public String unescape(String html) {
    if (html == null || html.isEmpty()) {
      return html;
    }

    String decoded = StringUtility.replace(html, "&amp;", "&");
    decoded = StringUtility.replace(decoded, "&#38;", "&");
    decoded = StringUtility.replaceNoCase(decoded, "&#x26;", "&");

    decoded = StringUtility.replace(decoded, "&lt;", "<");
    decoded = StringUtility.replace(decoded, "&#60;", "<");
    decoded = StringUtility.replaceNoCase(decoded, "&#x3c;", "<");

    decoded = StringUtility.replace(decoded, "&gt;", ">");
    decoded = StringUtility.replace(decoded, "&#62;", ">");
    decoded = StringUtility.replaceNoCase(decoded, "&#x3e;", ">");

    decoded = StringUtility.replace(decoded, "&quot;", "\"");
    decoded = StringUtility.replace(decoded, "&#34;", "\"");
    decoded = StringUtility.replaceNoCase(decoded, "&#x22;", "\"");

    decoded = StringUtility.replace(decoded, "&#47;", "/"); // no named entity for the slash
    decoded = StringUtility.replaceNoCase(decoded, "&#x2f;", "/");

    decoded = StringUtility.replace(decoded, "&apos;", "'");
    decoded = StringUtility.replace(decoded, "&#39;", "'");
    decoded = StringUtility.replaceNoCase(decoded, "&#x27;", "'");
    return decoded;
  }

  /**
   * Replaces all new lines with a HTML line break (&lt;br&gt; tag). In some cases used after an {@link #escape(String)}
   * operation.
   */
  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  public String newLineToBr(String input) {
    String result = StringUtility.replace(input, "\r\n", "<br>");
    return StringUtility.replace(result, "\n", "<br>");
  }

  /**
   * Combined called, first {@link #escape(String)} and then {@link #newLineToBr(String)}.
   */
  public String escapeAndNewLineToBr(String text) {
    return newLineToBr(escape(text));
  }

  public String removeAttributeValues(String html) {
    // Keep in sync with PlainTextEncoder.removeAttributeValues

    Character lastAttributeQuote = null;
    boolean insideTag = false;
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < html.length(); i++) {
      char c = html.charAt(i);
      if (lastAttributeQuote != null) {
        // inside quoted attribute value
        if (c == lastAttributeQuote) {
          // end of quoted attribute value
          lastAttributeQuote = null;
        }
        else {
          // ignore all characters beside closing attribute value quote
          continue;
        }
      }
      else if (insideTag && (c == '\'' || c == '"')) {
        // start of quoted attribute value
        lastAttributeQuote = c;
      }
      else if (c == '<' && html.length() > i + 1 && !Character.isWhitespace(html.charAt(i + 1))) {
        // start of tag
        insideTag = true;
      }
      else if (c == '>') {
        // end of tag
        insideTag = false;
      }
      result.append(c);
    }
    return result.toString();
  }

  /**
   * Truncates the text content of the given html using {@link #truncate(String, Integer, boolean)} without adding an ellipsis.
   */
  public String truncate(String html, Integer maxLength) {
    return truncate(html, maxLength, false);
  }

  /**
   * Truncates the text content of the given html without removing the html tags surrounding the remaining text.
   * The html tags that follow the remaining text and don't close a tag opened before the text may be removed.
   *
   * @param html
   *     the html content to truncate
   * @param maxLength
   *     the maximum length the truncated text will have
   * @param addEllipsis
   *     true to add an ellipsis character at the position where the text was truncated
   */
  public String truncate(String html, Integer maxLength, boolean addEllipsis) {
    if (StringUtility.isNullOrEmpty(html)) {
      return html;
    }
    if (maxLength <= 0) {
      return addEllipsis ? "…" : "";
    }
    int size = html.length();
    if (size <= maxLength) {
      // Content fits completely with all tags, just return it as it is
      return html;
    }

    boolean inTag = false;
    boolean inEntity = false;
    boolean inAttr = false;
    boolean inComment = false;
    char attrChar = '"';
    int numOpenTags = 0;
    int numTextChars = 0;
    int ellipsisPos = -1;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < size; i++) {
      char character = html.charAt(i);
      if (numTextChars >= maxLength && !inTag && !inEntity && numOpenTags == 0) {
        insertEllipsis(result, ellipsisPos);
        // Abort if max length is reached but not in the middle of a tag or entity and only if all open tags are closed
        break;
      }
      char prevChar = i > 1 ? html.charAt(i - 1) : '\0';
      char nextChar = i < size - 1 ? html.charAt(i + 1) : '\0';
      if (!inComment && character == '<' && i + 4 < size && "<!--".equals(html.substring(i, i + 4))) {
        // Comment found <!--
        inComment = true;
        result.append(character);
        continue;
      }
      if (inComment && i - 2 >= 0 && "-->".equals(html.substring(i - 2, i + 1))) {
        // Closing comment found -->
        inComment = false;
        result.append(character);
        continue;
      }
      if (!inComment && !inTag && character == '<') {
        inTag = true;
        if (nextChar == '/') {
          // Closing tag found, e.g. </div>
          numOpenTags = Math.max(numOpenTags - 1, 0);
        }
        else {
          // Opening tag found e.g. <div>
          numOpenTags++;
        }
        result.append(character);
        continue;
      }
      if (inTag && !inAttr && character == '>') {
        inTag = false;
        if (prevChar == '/') {
          // Self-closing tag found, e.g. <br/>
          numOpenTags = Math.max(numOpenTags - 1, 0);
        }
        result.append(character);
        continue;
      }
      if (inTag && !inAttr && ObjectUtility.isOneOf(character, '"', '\'')) {
        // Attribute found with " or '
        inAttr = true;
        attrChar = character;
        result.append(character);
        continue;
      }
      if (inAttr && character == attrChar) {
        // Closing attribute found
        inAttr = false;
        result.append(character);
        continue;
      }
      if (!inComment && !inEntity && !inAttr && character == '&') {
        // HTML entity (character reference) found, e.g. &gt;
        inEntity = true;
        result.append(character);
        continue;
      }
      if (inEntity && character == ';') {
        // Closing HTML entity found -> counts as one character
        inEntity = false;
        numTextChars++;
        result.append(character);
        ellipsisPos = updateEllipsisPos(result, maxLength, addEllipsis, ellipsisPos, numTextChars);
        continue;
      }
      if (inTag || inEntity || inComment) {
        result.append(character);
        continue;
      }
      numTextChars++;
      if (numTextChars <= maxLength) {
        result.append(character);
        ellipsisPos = updateEllipsisPos(result, maxLength, addEllipsis, ellipsisPos, numTextChars);
      }
      else {
        insertEllipsis(result, ellipsisPos);
        ellipsisPos = -1;
      }
    }
    return result.toString();
  }

  protected int updateEllipsisPos(StringBuilder result, int maxLength, boolean addEllipsis, int ellipsisPos, int numChars) {
    if (addEllipsis && numChars == maxLength) {
      ellipsisPos = result.length();
    }
    return ellipsisPos;
  }

  protected void insertEllipsis(StringBuilder text, int position) {
    if (position > -1) {
      text.insert(position, '…');
    }
  }
}
