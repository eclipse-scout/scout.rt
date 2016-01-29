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

  private static final Pattern TABLE_CELL_END_TAGS = Pattern.compile("</td>|<td/>", Pattern.CASE_INSENSITIVE);
  private static final Pattern HTML_PARAGRAPH_END_TAGS = Pattern.compile("<br/?></div>|</div>|<br/?>|</p>|<p/>|</tr>|</table>", Pattern.CASE_INSENSITIVE);

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
   * <li><code>&lt;br/&gt;</code>
   * <li><code>&lt;/p&gt;</code>
   * <li><code>&lt;p/&gt;</code>
   * <li><code>&lt;/tr&gt;</code>
   * <li><code>&lt;/table&gt;</code>
   * </ul>
   * <li>The following tags are considered "end of table cell" and are converted to <code>" "</code> (space):
   * <ul>
   * <li><code>&lt;/td&gt;</code>
   * <li><code>&lt;td/&gt;</code>
   * </ul>
   * <li>Multiple spaces are removed.
   * <li>Leading and trailing whitespace line is removed from each line.
   * <li>{@link StringUtility#htmlDecode(String)} is applied to the result, so not all HTML entities are replaced!
   * </ul>
   * <p>
   * <i>Note: This method originated from the now-deprecated HTMLUtility. It is possible that it will be enhanced in
   * future Scout releases.</i>
   */
  public String toPlainText(String html) {
    if (html == null || html.length() == 0) {
      return html;
    }
    String s = StringUtility.getTag(html, "body", true);
    if (s == null) {
      // <body> not found, use entire input
      s = html;
    }
    // Table cells
    Matcher matcher = TABLE_CELL_END_TAGS.matcher(s);
    s = matcher.replaceAll(" ");
    //newlines
    s = s.replaceAll("\r", "").replaceAll("\n", " ");
    matcher = HTML_PARAGRAPH_END_TAGS.matcher(s);
    s = matcher.replaceAll("\n");
    // tabs
    s = s.replace(StringUtility.HTML_ENCODED_TAB, "\t");
    //remove tags
    s = Pattern.compile("<[^>]+>", Pattern.DOTALL).matcher(s).replaceAll("");
    //remove multiple spaces
    s = s.replaceAll("[ ]+", " ");
    //remove spaces at the beginning and end of each line
    s = s.replaceAll("[ ]+\n", "\n");
    s = s.replaceAll("\n[ ]+", "\n");
    s = StringUtility.htmlDecode(s);
    s = s.trim();
    return s;
  }
}
