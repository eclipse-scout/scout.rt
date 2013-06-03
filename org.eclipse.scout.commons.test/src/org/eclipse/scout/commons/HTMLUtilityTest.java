/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.HTMLUtility.DefaultFont;
import org.junit.Test;

/**
 * JUnit for {@link HTMLUtility}
 */
public class HTMLUtilityTest {

  @Test
  public void testStyleHtmlTextNullAndEmptyValues() throws Exception {
    // null values
    String output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "</body>" +
        "</html>";
    assertEquals(output, HTMLUtility.cleanupHtml(null, true, false, null));

    // empty html
    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "</body>" +
        "</html>";
    assertEquals(output, HTMLUtility.cleanupHtml("", true, false, null));
  }

  @Test
  public void testStyleHtmlTextCompleteHtml() throws Exception {
    String input = "" +
        "<html>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    String output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";

    // without encoding information
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, null));

    // with encoding information
    input = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("HTML with encoding information", output, HTMLUtility.cleanupHtml(input, true, false, null));

    input = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("different encoding than UTF-8", output, HTMLUtility.cleanupHtml(input, true, false, null));

    input = "" +
        "<html>" +
        "<head>" +
        "<meta  http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("different encoding with additional space", output, HTMLUtility.cleanupHtml(input, true, false, null));

    input = "" +
        "<html>" +
        "<head>" +
        "<meta \n http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("different encoding with additional new line", output, HTMLUtility.cleanupHtml(input, true, false, null));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<meta http-equiv=\"description\" content=\"This page provides test content\"/>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>" +
        "<meta http-equiv=\"description\" content=\"This page provides test content\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("complete html document with multiple meta tags (including Content-Type)", output, HTMLUtility.cleanupHtml(input, true, false, null));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"description\" content=\"This page provides test content\"/>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"description\" content=\"This page provides test content\"/>" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("complete html document with multiple meta tags in different order (including Content-Type)", output, HTMLUtility.cleanupHtml(input, true, false, null));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<meta http-equiv=\"description\" content=\"This page provides test content\"/>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"description\" content=\"This page provides test content\"/>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    assertEquals("complete html document with meta tag different than Content-Type", output, HTMLUtility.cleanupHtml(input, true, false, null));
  }

  @Test
  public void testStyleHtmlDefaultFont() throws Exception {
    String output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "body{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "td{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    String input = "" +
        "<html>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    DefaultFont defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "body{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "td{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial", "Times New Roman", "sans-serif");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "body{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "td{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial,'Times New Roman',sans-serif;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial", "Times New Roman", "sans-serif");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "body { font:bold .9em Times; }" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "td{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "body { font:bold .9em Times; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "body { font:bold .9em Times; }" +
        "td { font:italic 1cm Helvetica; }" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "body { font:bold .9em Times; }" +
        "td { font:italic 1cm Helvetica; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "td { font:italic 1cm Helvetica; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "td { font:italic 1cm Helvetica; }" +
        "body{}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif;font-size:20em;color:#a0ff00;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif;font-size:20em}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif;font-size:20em;color:#FFFFFF;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif;font-size:20em;color:#FFFFFF;}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    output = "" +
        "<html>" +
        "<head>" +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\"/>" +
        "<style type=\"text/css\">" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif;font-size:10pt;color:#a0ff00;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "Test content" +
        "</body>" +
        "</html>";
    input = "" +
        "<html>" +
        "<head>" +
        "<style>" +
        "td { font:italic 1cm Helvetica; }" +
        "body{font-family:sans-serif}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "Test content" +
        "</body>" +
        "</html>";
    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, true, false, defaultFont));

    input = "" +
        "<html>" +
        "<head>" +
        "<style type=\"text/css\">" +
        "li {font-weight:bold;font-size:18;text-align:center;color:white;}\n" +
        "ul {font-weight:bold;font-size:18;text-align:center;color:white;}" +
        "body, td {font-family:arial;font-size:12;background-color:#EBF4F8;margin:0;}\n" +
        "a {color: #67A8CE;}\n" +
        "td {vertical-align: top;}\n" +
        "td.bullet {font-weight:bold;font-size:18;text-align:center;color:white;}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "</body>" +
        "</html>";

    output = "" +
        "<html>" +
        "<head>" +
        "<style type=\"text/css\">" +
        "li {font-weight:bold;font-size:18;text-align:center;color:white;}\n" +
        "ul {font-weight:bold;font-size:18;text-align:center;color:white;}" +
        "body, td {font-family:arial;font-size:12;background-color:#EBF4F8;margin:0;color:#a0ff00;}\n" +
        "a {color: #67A8CE;}\n" +
        "td {vertical-align: top;}\n" +
        "td.bullet {font-weight:bold;font-size:18;text-align:center;color:white;font-family:Arial;}" +
        "table{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "tr{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "th{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "span{font-family:Arial;font-size:10pt;color:#a0ff00;}" +
        "</style>" +
        "</head>" +
        "<body style=\"overflow:auto;\">" +
        "</body>" +
        "</html>";

    defaultFont = createDefaultFont(10, 0xA0FF00, "Arial");
    assertEquals(output, HTMLUtility.cleanupHtml(input, false, false, defaultFont));
  }

  @Test
  public void testHtmlEncodeAndDecode() throws Exception {
    String value = "a\nb\tc   d<br/><p>e</p>";
    String htmlEncoded = StringUtility.htmlEncode(value, true);

    assertEquals("a<br/>b<span style=\"white-space:pre\">&#9;</span>c&nbsp;&nbsp;&nbsp;d&lt;br/&gt;&lt;p&gt;e&lt;/p&gt;", StringUtility.htmlEncode(value, true));
    assertEquals("a\nb\tc   d\n<p>e</p>", StringUtility.htmlDecode(htmlEncoded));
  }

  private DefaultFont createDefaultFont(int size, int color, String... fontFamilies) {
    DefaultFont defaultFont = new DefaultFont();
    defaultFont.setSize(size);
    defaultFont.setForegroundColor(color);
    defaultFont.setSizeUnit("pt");
    defaultFont.setFamilies(fontFamilies);
    return defaultFont;
  }
}
