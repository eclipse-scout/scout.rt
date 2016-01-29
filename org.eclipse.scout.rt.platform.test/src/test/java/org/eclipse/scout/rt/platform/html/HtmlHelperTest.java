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
}
