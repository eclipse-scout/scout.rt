/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * @since 5.2
 */
@ApplicationScoped
public class HtmlHelper {

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  private static final Pattern HTML_PARAGRAPH_END_TAGS = Pattern.compile("<br/?></div>|</div>|<br/?>|</p>|<p/>|</tr>|</h[1-6]>|</dt>|</dd>|</dl>|</table>|</li>|</head>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_SPACE_END_TAGS = Pattern.compile("</td>|</th>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>", Pattern.DOTALL);
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
    //newlines
    s = StringUtility.replace(s, "\r", "");
    s = StringUtility.replace(s, "\n", " ");
    Matcher matcher = HTML_PARAGRAPH_END_TAGS.matcher(s);
    s = matcher.replaceAll("\n");
    //tabs
    s = StringUtility.replace(s, StringUtility.HTML_ENCODED_TAB, "\t");
    //remove tags
    matcher = HTML_SPACE_END_TAGS.matcher(s);
    s = matcher.replaceAll(" ");
    matcher = HTML_TAGS.matcher(s);
    s = matcher.replaceAll("");
    //remove multiple spaces
    matcher = MULTIPLE_SPACES.matcher(s);
    s = matcher.replaceAll(" ");
    //remove spaces at the beginning and end of each line
    matcher = SPACES_ADJACENT_LINEBREAKS.matcher(s);
    s = matcher.replaceAll("\n");
    s = unescape(s);

    // space
    s = StringUtility.replace(s, "&nbsp;", " ");
    s = StringUtility.replace(s, "&#160;", " ");
    s = StringUtility.replaceNoCase(s, "&#xa0;", " ");

    // tab
    s = StringUtility.replace(s, "&#9;", "\t");
    s = StringUtility.replaceNoCase(s, "&#x9;", "\t");

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
}
