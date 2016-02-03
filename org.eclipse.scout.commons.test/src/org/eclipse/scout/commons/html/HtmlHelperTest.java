/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.html;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.StringUtility;
import org.junit.Test;

/**
 * @since 6.0 (backported)
 */
public class HtmlHelperTest {
  @Test
  public void testToPlainText() {
    // Note: Some of the expected results are not really what the user would expect,
    // but what the toPlainText() method currently returns. They are marked with "[?]"
    // below. Sometimes in the future, it should be considered to change them.

    assertEquals(null, HtmlHelper.toPlainText(null));
    assertEquals("", HtmlHelper.toPlainText(""));

    // Text only
    assertEquals("hello", HtmlHelper.toPlainText("hello"));
    assertEquals("one two", HtmlHelper.toPlainText("one\ntwo"));
    assertEquals("one two", HtmlHelper.toPlainText("one\r\ntwo"));
    assertEquals("onetwo", HtmlHelper.toPlainText("one\rtwo"));
    assertEquals("hell<", HtmlHelper.toPlainText("hell&lt;"));
    assertEquals("one   two", HtmlHelper.toPlainText("one&nbsp;&nbsp; two"));
    assertEquals("hell&ouml;", HtmlHelper.toPlainText("hell&ouml;")); // [?] not all entities are replaced
    assertEquals("one\ttwo", HtmlHelper.toPlainText("one&#9;two"));
    assertEquals("one \t two", HtmlHelper.toPlainText("one &#9; two"));
    assertEquals("one\ttwo", HtmlHelper.toPlainText("one" + StringUtility.HTML_ENCODED_TAB + "two"));

    // Simple documents
    assertEquals("", HtmlHelper.toPlainText("<html>"));
    assertEquals("", HtmlHelper.toPlainText("<html></html>"));
    assertEquals("", HtmlHelper.toPlainText("<html><head></html>"));
    assertEquals("one", HtmlHelper.toPlainText("<html><head>one</html>"));
    assertEquals("one & two", HtmlHelper.toPlainText("<html><head>one & two</html>"));
    assertEquals("one & two", HtmlHelper.toPlainText("<html><head>one &amp; two</html>"));
    assertEquals("one & two three", HtmlHelper.toPlainText("<html><head>one &amp; two</head><body>three</html>")); // [?] invalid <body>, has no end tag
    assertEquals("three", HtmlHelper.toPlainText("<html><head>one &amp; two</head><body>three</body></html>"));

    // Line breaks
    assertEquals("a\nb", HtmlHelper.toPlainText("a<br>b"));
    assertEquals("a\nb", HtmlHelper.toPlainText("a <br/> b"));
    assertEquals("a\nb", HtmlHelper.toPlainText("a    <br/> b"));
    assertEquals("a \nb", HtmlHelper.toPlainText("a&nbsp;<br/> b"));
    assertEquals("line", HtmlHelper.toPlainText("<br/>line")); // [?]
    assertEquals("line1\nx\nline2", HtmlHelper.toPlainText("<p>line1<br>\nx</p><p>line2</p>"));
    assertEquals("line1 x\nline2", HtmlHelper.toPlainText("<div>line1\nx</div><div>line2</div>")); // [?]
    assertEquals("line1\nline2", HtmlHelper.toPlainText("<div>line1<br/></div><div>line2<br/></div>"));

    // Tables
    assertEquals("one two\nthree four", HtmlHelper.toPlainText("<table><tr><td>one</td><td>two</td></tr><tr><td>three</td><td>four</td></tr></table>"));
  }

  @Test
  public void testEscape() {
    assertEquals(null, HtmlHelper.escape(null));
    assertEquals("", HtmlHelper.escape(""));
    assertEquals(" ", HtmlHelper.escape(" "));
    assertEquals("hello", HtmlHelper.escape("hello"));

    assertEquals("one &amp; two", HtmlHelper.escape("one & two"));
    assertEquals("one &lt; two", HtmlHelper.escape("one < two"));
    assertEquals("&gt;&lt;script&gt;alert(&#39;hacker attack&#39;);&lt;&#47;script&gt;&lt;", HtmlHelper.escape("><script>alert('hacker attack');</script><"));
    assertEquals("one&amp;nbsp;&amp;nbsp; two", HtmlHelper.escape("one&nbsp;&nbsp; two"));
    assertEquals("this is &quot;good&quot;", HtmlHelper.escape("this is \"good\""));
    assertEquals("http:&#47;&#47;www.example.com&#47;~myapp&#47;script?q=search%20query&amp;time=now&amp;x=17263.23", HtmlHelper.escape("http://www.example.com/~myapp/script?q=search%20query&time=now&x=17263.23"));
    assertEquals("&lt;div&gt;&lt;span style=&quot;color: red; content: &#39;\\u39&#39;;&quot;&gt;Alert!&lt;&#47;span&gt;&lt;br&#47;&gt;Line2&lt;&#47;div&gt;",
        HtmlHelper.escape("<div><span style=\"color: red; content: '\\u39';\">Alert!</span><br/>Line2</div>"));
    assertEquals("hell&amp;ouml;", HtmlHelper.escape("hell&ouml;"));
    assertEquals("one&#47;two&#47;end", HtmlHelper.escape("one/two/end"));

    // Things that should NOT be escaped
    assertEquals("one\ntwo  end", HtmlHelper.escape("one\ntwo  end"));
    assertEquals("key:\tvalue\r\nline2", HtmlHelper.escape("key:\tvalue\r\nline2"));
  }

  @Test
  public void testUnescape() {
    assertEquals(null, HtmlHelper.unescape(null));
    assertEquals("", HtmlHelper.unescape(""));
    assertEquals(" ", HtmlHelper.unescape(" "));
    assertEquals("hello", HtmlHelper.unescape("hello"));

    assertEquals("one & two", HtmlHelper.unescape("one &amp; two"));
    assertEquals("one < two", HtmlHelper.unescape("one &lt; two"));
    assertEquals("><script>alert('hacker attack');</script><", HtmlHelper.unescape("&gt;&lt;script&gt;alert(&#39;hacker attack&#39;);&lt;/script&gt;&lt;"));
    assertEquals("one&nbsp;&nbsp; two", HtmlHelper.unescape("one&amp;nbsp;&amp;nbsp; two"));
    assertEquals("this is \"good\"", HtmlHelper.unescape("this is &quot;good&quot;"));
    assertEquals("http://www.example.com/~myapp/script?q=search%20query&time=now&x=17263.23", HtmlHelper.unescape("http://www.example.com/~myapp/script?q=search%20query&amp;time=now&amp;x=17263.23"));
    assertEquals("<div><span style=\"color: red; content: '\\u39';\">Alert!</span><br/>Line2</div>",
        HtmlHelper.unescape("&lt;div&gt;&lt;span style=&quot;color: red; content: &#39;\\u39&#39;;&quot;&gt;Alert!&lt;/span&gt;&lt;br/&gt;Line2&lt;/div&gt;"));
    assertEquals("hell&ouml;", HtmlHelper.unescape("hell&amp;ouml;"));
    assertEquals("one/two/end", HtmlHelper.unescape("one&#47;two&#47;end"));

    // Things that should NOT be unescaped
    assertEquals("one\ntwo  end", HtmlHelper.unescape("one\ntwo  end"));
    assertEquals("key:\tvalue\r\nline2", HtmlHelper.unescape("key:\tvalue\r\nline2"));
    assertEquals("hell&ouml;", HtmlHelper.unescape("hell&ouml;"));
  }
}
