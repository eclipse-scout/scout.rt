package org.eclipse.scout.rt.ui.rap.util;

import java.util.regex.Pattern;

/**
 * Utility class that provides a number of useful static methods to support the
 * implementation of widget life cycle adapters with regard to supporting html markup in table headers, cells, etc.
 * 
 * @since 1.0
 */
public final class HtmlTextUtility {

  private static final Pattern htmlMarkupIdentifierPattern = Pattern.compile("<html[^>]*>(.*)</html>.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern htmlBracketPattern = Pattern.compile("<html[^>]*>(.*)</html>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern headBracketPattern = Pattern.compile("<head[^>]*>(.*)</head>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern bodyBracketPattern = Pattern.compile("<body[^>]*>(.*)</body>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern breakingTagsPattern = Pattern.compile("(<br>|<br/>|<p[^>]*>|<p[^>]*/>|</p>|</tr>|<ul[^>]*>|</ul>|<ol[^>]*>|</ol>|</li>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern allTagsPattern = Pattern.compile("(<[^>]+>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  /**
   * Support for styled content (inline html) in table column header cell.
   * In Web 2.0 it is common to use html for styling, this property is used to enable pass-through mode in TableItemLCA
   * and TableColumnLCA so that
   * the text is not html encoded. This allows for example using the <b> and <i> tag inside table headers and table
   * cells.
   * <p>
   * The convention (also used in swing) to mark html text is to encapsulate it into <html>...</html> tags. This method
   * checks if the passed text is wrapped into html start and end tag.
   * <p>
   * The developer using this feature is responsible to preprocess the html text passed in order not to break outside
   * ajax styles and contexts.
   */
  public static boolean isTextWithHtmlMarkup(String text) {
    return text != null && htmlMarkupIdentifierPattern.matcher(text).matches();
  }

  /**
   * @param markupText
   *          is text enclosed in "html" begin and end tag.
   * @returns styled html text without "html", "head" and "body" tags. Newlines are preserved.
   */
  public static String transformMarkupTextToHtml(String markupText) {
    if (markupText == null) {
      return null;
    }
    String s = markupText.trim();
    boolean replaceNewlines = !isTextWithHtmlMarkup(s);
    s = htmlBracketPattern.matcher(s).replaceAll("$1");
    s = headBracketPattern.matcher(s).replaceAll("");
    s = bodyBracketPattern.matcher(s).replaceAll("$1");
    s = s.trim();
    if (replaceNewlines) {
      s = s.replaceAll("[\\n]", "<br/>");
    }
    return s;
  }

  /**
   * @param plainText
   *          is text not containing any html tags.
   * @returns styled html text without "html", "head" and "body" tags. Newlines are replaced by br tags, spaces are
   *          preserved using &nbsp;.
   */
  public static String transformPlainTextToHtml(String plainText) {
    return transformPlainTextToHtml(plainText, true);
  }

  /**
   * @param plainText
   *          is text not containing any html tags.
   * @param replaceBreakableChars
   *          defines if breakable characters (spaces and hyphens) should be replaced with non-breakable ones.
   * @returns styled html text without "html", "head" and "body" tags. Newlines are replaced by br tags, spaces are
   *          preserved using &nbsp;.
   * @since 3.10.0-M3 (backported)
   */
  public static String transformPlainTextToHtml(String plainText, boolean replaceBreakableChars) {
    if (plainText == null) {
      return null;
    }
    String s = plainText;
    s = s.replaceAll("[&]", "&amp;");
    s = s.replaceAll("[\"]", "&quot;");
    s = s.replaceAll("[']", "&#39;");
    s = s.replaceAll("[<]", "&lt;");
    s = s.replaceAll("[>]", "&gt;");
    s = s.replaceAll("[\\n]", "<br/>");
    if (replaceBreakableChars) {
      s = s.replaceAll("[\\s]", "&nbsp;");
      s = s.replaceAll("[-]", "&#8209;");
    }
    return s;
  }

  /**
   * @returns plain text from html. This method takes the best effort (this is not perfect) to transform partial html
   *          styled text to plain text.
   *          Strategy:
   *          <ul>
   *          <li>Replace newline by space</li>
   *          <li>Replace br, p and tr tags by newline</li>
   *          <li>Replace all other tags by space</li>
   *          <li>Replace multiple spaces by single space</li>
   *          <li>Trim text</li>
   *          </ul>
   */
  public static String transformHtmlToPlainText(String htmlText) {
    if (htmlText == null) {
      return null;
    }
    String s = htmlText;
    s = s.replaceAll("[\\n\\r]+", " ");
    s = breakingTagsPattern.matcher(s).replaceAll("\n");
    s = allTagsPattern.matcher(s).replaceAll(" ");
    s = s.replaceAll("[ ][ ]+", " ");
    s = s.trim();
    return s;
  }

  public static int countLineBreaks(String htmlText) {
    if (htmlText == null || htmlText.length() == 0) {
      return 0;
    }
    int l = 1;
    int pos = 0;
    while ((pos = htmlText.indexOf('\n', pos)) >= 0) {
      l++;
      // next
      pos++;
    }
    pos = 0;
    while ((pos = htmlText.indexOf("<br/>", pos)) >= 0) {
      l++;
      // next
      pos++;
    }
    return l;
  }

  public static int countHtmlTableRows(String htmlText) {
    if (htmlText == null || htmlText.length() == 0) {
      return 0;
    }
    int r = 0;
    int pos = 0;
    while ((pos = htmlText.indexOf("<tr>", pos)) >= 0) {
      r++;
      // next
      pos++;
    }
    return r;
  }

}
