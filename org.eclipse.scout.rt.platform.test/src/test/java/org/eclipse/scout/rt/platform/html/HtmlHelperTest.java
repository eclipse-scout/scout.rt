package org.eclipse.scout.rt.platform.html;

import static org.junit.Assert.assertEquals;

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

    assertEquals(null, helper.toPlainText(null));
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

    // Simple documents
    assertEquals("", helper.toPlainText("<html>"));
    assertEquals("", helper.toPlainText("<html></html>"));
    assertEquals("", helper.toPlainText("<html><head></html>"));
    assertEquals("one", helper.toPlainText("<html><head>one</html>"));
    assertEquals("one & two", helper.toPlainText("<html><head>one & two</html>"));
    assertEquals("one & two", helper.toPlainText("<html><head>one &amp; two</html>"));
    assertEquals("one & two three", helper.toPlainText("<html><head>one &amp; two</head><body>three</html>")); // [?] invalid <body>, has no end tag
    assertEquals("three", helper.toPlainText("<html><head>one &amp; two</head><body>three</body></html>"));

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
  }

  @Test
  public void testEscape() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    assertEquals(null, helper.escape(null));
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

    assertEquals(null, helper.unescape(null));
    assertEquals("", helper.unescape(""));
    assertEquals(" ", helper.unescape(" "));
    assertEquals("hello", helper.unescape("hello"));

    assertEquals("one & two", helper.unescape("one &amp; two"));
    assertEquals("one < two", helper.unescape("one &lt; two"));
    assertEquals("><script>alert('hacker attack');</script><", helper.unescape("&gt;&lt;script&gt;alert(&#39;hacker attack&#39;);&lt;/script&gt;&lt;"));
    assertEquals("one&nbsp;&nbsp; two", helper.unescape("one&amp;nbsp;&amp;nbsp; two"));
    assertEquals("this is \"good\"", helper.unescape("this is &quot;good&quot;"));
    assertEquals("http://www.example.com/~myapp/script?q=search%20query&time=now&x=17263.23", helper.unescape("http://www.example.com/~myapp/script?q=search%20query&amp;time=now&amp;x=17263.23"));
    assertEquals("<div><span style=\"color: red; content: '\\u39';\">Alert!</span><br/>Line2</div>",
        helper.unescape("&lt;div&gt;&lt;span style=&quot;color: red; content: &#39;\\u39&#39;;&quot;&gt;Alert!&lt;/span&gt;&lt;br/&gt;Line2&lt;/div&gt;"));
    assertEquals("hell&ouml;", helper.unescape("hell&amp;ouml;"));
    assertEquals("one/two/end", helper.unescape("one&#47;two&#47;end"));

    // Things that should NOT be unescaped
    assertEquals("one\ntwo  end", helper.unescape("one\ntwo  end"));
    assertEquals("key:\tvalue\r\nline2", helper.unescape("key:\tvalue\r\nline2"));
    assertEquals("hell&ouml;", helper.unescape("hell&ouml;"));
  }

  @Test
  public void testNewLineToBr() {
    HtmlHelper helper = BEANS.get(HtmlHelper.class);

    assertEquals(null, helper.newLineToBr(null));
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
}
