/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.html;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(PlatformTestRunner.class)
public class HtmlHelperTest {

  @Test
  public void testToPlainText() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    // Note: Some of the expected results are not really what the user would expect,
    // but what the toPlainText() method currently returns. They are marked with "[?]"
    // below. Sometimes in the future, it should be considered to change them.

    assertNull(helper.toPlainText(null));
    assertEquals("", helper.toPlainText(""));

    // Text only
    assertEquals("hello", helper.toPlainText("hello"));
    assertEquals("one two", helper.toPlainText("one\ntwo"));
    assertEquals("one two", helper.toPlainText("one\r\ntwo"));
    assertEquals("onetwo", helper.toPlainText("one\rtwo"));
    assertEquals("hell<", helper.toPlainText("hell&lt;"));
    assertEquals("one   two", helper.toPlainText("one&nbsp;&nbsp; two"));
    assertEquals("hell&ouml;", helper.toPlainText("hell&ouml;")); // [?] not all entities are replaced
    assertEquals("one\ttwo", helper.toPlainText("one&#9;two"));
    assertEquals("one \t two", helper.toPlainText("one &#9; two"));
    assertEquals("one\ttwo", helper.toPlainText("one" + StringUtility.HTML_ENCODED_TAB + "two"));
    assertEquals("one    two", helper.toPlainText("one&#160;&#xa0;&#Xa0;&#xA0;two")); // HTML5 spec allows for mixed case hex values.
    assertEquals("one\t\ttwo", helper.toPlainText("one&#x9;&#X9;two")); // HTML5 spec allows for mixed case hex values.
    assertEquals("Unterraschungsfeier", helper.toPlainText("<div class=\"rte-line\">Unter<u>rasch</u>u<span class=\"rte-highlight\" style=\"background-color: rgb(255, 219, 157)\">ngs</span>feier<br></div>")); // Formating tags within a single word.
    assertEquals("Header 1\nHeader 2", helper.toPlainText("<h1>Header 1</h1><h1>Header 2</h1>")); // Headers
    assertEquals("List 1\nList 2\nTableHeader 1 TableHeader 2\nData 1 Data 2",
        helper.toPlainText("<ul><li>List 1</li><li>List 2</li></ul><table><tr><th>TableHeader 1</th><th>TableHeader 2</th></tr><tr><td>Data 1</td><td>Data 2</td></tr></table>")); // List and tables

    // Simple documents
    assertEquals("", helper.toPlainText("<html>"));
    assertEquals("", helper.toPlainText("<html></html>"));
    assertEquals("", helper.toPlainText("<html><head></html>"));
    assertEquals("one", helper.toPlainText("<html><head>one</html>"));
    assertEquals("one & two", helper.toPlainText("<html><head>one & two</html>"));
    assertEquals("one & two", helper.toPlainText("<html><head>one &amp; two</html>"));
    assertEquals("one & two\nthree", helper.toPlainText("<html><head>one &amp; two</head><body>three</html>")); // [?] invalid <body>, has no end tag
    assertEquals("three", helper.toPlainText("<html><head>one &amp; two</head><body>three</body></html>"));
    assertEquals("Unterraschungsfeier",
        helper.toPlainText("<html><body><div class=\"rte-line\">Unter<u>rasch</u>u<span class=\"rte-highlight\" style=\"background-color: rgb(255, 219, 157)\">ngs</span>feier<br></div></body></html>"));

    // Line breaks
    assertEquals("a\nb", helper.toPlainText("a<br>b"));
    assertEquals("a\nb", helper.toPlainText("a <br/> b"));
    assertEquals("a\nb", helper.toPlainText("a    <br/> b"));
    assertEquals("a \nb", helper.toPlainText("a&nbsp;<br/> b"));
    assertEquals("line", helper.toPlainText("<br/>line")); // [?]
    assertEquals("line1\nx\nline2", helper.toPlainText("<p>line1<br>\nx</p><p>line2</p>"));
    assertEquals("line1 x\nline2", helper.toPlainText("<div>line1\nx</div><div>line2</div>")); // [?]
    assertEquals("line1\nline2", helper.toPlainText("<div>line1<br/></div><div>line2<br/></div>"));

    // Tables
    assertEquals("one two\nthree four", helper.toPlainText("<table><tr><td>one</td><td>two</td></tr><tr><td>three</td><td>four</td></tr></table>"));

    // Styles and Scripts
    assertEquals(
        "Lorem ipsum dolor\n"
            + "Donec mattis metus lorem. Aenean posuere tincidunt enim.\n"
            + "Pellentesque eu euismod eros, in ullamcorper erat.",
        helper.toPlainText(
            "<h1>Lorem ipsum dolor</h1>\n"
                + "<style>\n"
                + "p {\n"
                + "  color: #26b72b;\n"
                + "}\n"
                + "</style>"
                + "<p style=\"color: blue\">Donec mattis metus lorem. Aenean posuere tincidunt enim.</p>\n"
                + "<script>alert('Hello World!');</script>"
                + "<p style=\"color: green\">Pellentesque eu euismod eros, "
                + "<script>alert('Hello World 2!');</script><script>alert('Hello World 3!');</script>"
                + "<script type='text/javascript'>\n"
                + "  document.write(123);\n"
                + "</script>"
                + "<style type='text/css'>\n"
                + "p {\n"
                + "  color: #26b72b;\n"
                + "}\n"
                + "</style>"
                + "in ullamcorper erat.</p>"));

    //Emojis
    assertEquals(""
        + "Emojis\n"
        + "Face with Tears of Joy Emoji: \uD83D\uDE02\n"
        + "Party Popper Emoji: \uD83C\uDF89\n"
        + "Man Technologist: Medium-light Skin Tone: \uD83D\uDC68\uD83C\uDFFC\u200D\uD83D\uDCBB",
        helper.toPlainText(""
            + "<h1>Emojis</h1>\n"
            + "<p>Face with Tears of Joy Emoji: &#128514;</p>\n"
            + "<p>Party Popper Emoji: &#127881;</p>\n"
            + "<p>Man Technologist: Medium-light Skin Tone: &#128104;&#127996;&zwj;&#128187;</p>"));
  }

  @Test
  public void testEscape() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    assertNull(helper.escape(null));
    assertEquals("", helper.escape(""));
    assertEquals(" ", helper.escape(" "));
    assertEquals("hello", helper.escape("hello"));

    assertEquals("one &amp; two", helper.escape("one & two"));
    assertEquals("one &lt; two", helper.escape("one < two"));
    assertEquals("&gt;&lt;script&gt;alert(&#39;hacker attack&#39;);&lt;&#47;script&gt;&lt;", helper.escape("><script>alert('hacker attack');</script><"));
    assertEquals("one&amp;nbsp;&amp;nbsp; two", helper.escape("one&nbsp;&nbsp; two"));
    assertEquals("this is &quot;good&quot;", helper.escape("this is \"good\""));
    assertEquals("http:&#47;&#47;www.example.com&#47;~myapp&#47;script?q=search%20query&amp;time=now&amp;x=17263.23", helper.escape("http://www.example.com/~myapp/script?q=search%20query&time=now&x=17263.23"));
    assertEquals("&lt;div&gt;&lt;span style=&quot;color: red; content: &#39;\\u39&#39;;&quot;&gt;Alert!&lt;&#47;span&gt;&lt;br&#47;&gt;Line2&lt;&#47;div&gt;",
        helper.escape("<div><span style=\"color: red; content: '\\u39';\">Alert!</span><br/>Line2</div>"));
    assertEquals("hell&amp;ouml;", helper.escape("hell&ouml;"));
    assertEquals("one&#47;two&#47;end", helper.escape("one/two/end"));

    // Things that should NOT be escaped
    assertEquals("one\ntwo  end", helper.escape("one\ntwo  end"));
    assertEquals("key:\tvalue\r\nline2", helper.escape("key:\tvalue\r\nline2"));
  }

  @Test
  public void testUnescape() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    assertNull(helper.unescape(null));
    assertEquals("", helper.unescape(""));
    assertEquals(" ", helper.unescape(" "));
    assertEquals("hello", helper.unescape("hello"));

    assertEquals("one & two", helper.unescape("one &amp; two"));
    assertEquals("one < two", helper.unescape("one &lt; two"));
    assertEquals("><script>alert('hacker attack');</script><", helper.unescape("&gt;&lt;script&gt;alert(&#39;hacker attack&#39;);&lt;/script&gt;&lt;"));
    assertEquals("one&nbsp;&nbsp; two", helper.unescape("one&amp;nbsp;&amp;nbsp; two"));
    assertEquals("this is \"good\"", helper.unescape("this is &quot;good&quot;"));
    assertEquals("http://www.example.com/~myapp/script?q=search%20query&time=now&x=17263.23",
        helper.unescape("http://www.example.com/~myapp/script?q=search%20query&amp;time=now&amp;x=17263.23"));
    assertEquals("<div><span style=\"color: red; content: '\\u39';\">Alert!</span><br/>Line2</div>",
        helper.unescape("&lt;div&gt;&lt;span style=&quot;color: red; content: &#39;\\u39&#39;;&quot;&gt;Alert!&lt;/span&gt;&lt;br/&gt;Line2&lt;/div&gt;"));
    assertEquals("hell&ouml;", helper.unescape("hell&amp;ouml;"));
    assertEquals("one/two/end", helper.unescape("one&#47;two&#47;end"));
    assertEquals("one&two<three>four\"five/six'seven'eight&nine>ten", // HTML5 spec allows for mixed case hex values.
        helper.unescape("one&#x26;two&#X3c;three&#x3E;four&#x22;five&#X2F;six&#x27;seven&#X27;eight&#X26;nine&#X3E;ten"));

    // Things that should NOT be unescaped
    assertEquals("one\ntwo  end", helper.unescape("one\ntwo  end"));
    assertEquals("key:\tvalue\r\nline2", helper.unescape("key:\tvalue\r\nline2"));
    assertEquals("hell&ouml;", helper.unescape("hell&ouml;"));
  }

  @Test
  public void testNewLineToBr() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    assertNull(helper.newLineToBr(null));
    assertEquals("", helper.newLineToBr(""));
    assertEquals(" ", helper.newLineToBr(" "));
    assertEquals("hello", helper.newLineToBr("hello"));
    assertEquals("<br>", helper.newLineToBr("<br>"));

    assertEquals("foo\rbar", helper.newLineToBr("foo\rbar"));
    assertEquals("foo<br>bar", helper.newLineToBr("foo\r\nbar"));
    assertEquals("foo<br>bar", helper.newLineToBr("foo\nbar"));
    assertEquals("foo<br>bar", helper.newLineToBr("foo\r\nbar"));
    assertEquals("foo<br>bar<br>foo", helper.newLineToBr("foo\nbar\r\nfoo"));
  }

  @Test
  public void testEscapeAndNewLineToBr() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    assertNull(helper.escapeAndNewLineToBr(null));
    assertEquals("", helper.escapeAndNewLineToBr(""));
    assertEquals(" ", helper.escapeAndNewLineToBr(" "));

    assertEquals("one &amp; two<br>three &amp; four", helper.escapeAndNewLineToBr("one & two\nthree & four"));
    assertEquals("&gt;&lt;script&gt;alert(&#39;hacker<br>attack&#39;);&lt;&#47;script&gt;&lt;", helper.escapeAndNewLineToBr("><script>alert('hacker\r\nattack');</script><"));
  }
}
