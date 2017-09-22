package org.eclipse.scout.rt.server.commons.servlet;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ContentSecurityPolicy}
 */
public class ContentSecurityPolicyTest {

  private ContentSecurityPolicy m_csp = new ContentSecurityPolicy();

  @Before
  public void setUp() {
    m_csp.empty();
  }

  @Test
  public void testBaseUri() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_BASE_URI, m_csp::appendBaseUri, m_csp::withBaseUri);
  }

  @Test
  public void testStyleSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_STYLE_SRC, m_csp::appendStyleSrc, m_csp::withStyleSrc);
  }

  @Test
  public void testScriptSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, m_csp::appendScriptSrc, m_csp::withScriptSrc);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testFrameSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_FRAME_SRC, m_csp::appendFrameSrc, m_csp::withFrameSrc);
  }

  @Test
  public void testChildSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, m_csp::appendChildSrc, m_csp::withChildSrc);
  }

  @Test
  public void testReportUri() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_REPORT_URI, m_csp::appendReportUri, m_csp::withReportUri);
  }

  @Test
  public void testFontSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_FONT_SRC, m_csp::appendFontSrc, m_csp::withFontSrc);
  }

  @Test
  public void testImgSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_IMG_SRC, m_csp::appendImgSrc, m_csp::withImgSrc);
  }

  @Test
  public void testMediaSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_MEDIA_SRC, m_csp::appendMediaSrc, m_csp::withMediaSrc);
  }

  @Test
  public void testObjectSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_OBJECT_SRC, m_csp::appendObjectSrc, m_csp::withObjectSrc);
  }

  @Test
  public void testSandbox() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_SANDBOX, m_csp::appendSandbox, m_csp::withSandbox);
  }

  @Test
  public void testDefaultSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_DEFAULT_SRC, m_csp::appendDefaultSrc, m_csp::withDefaultSrc);
  }

  @Test
  public void testConnectSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_CONNECT_SRC, m_csp::appendConnectSrc, m_csp::withConnectSrc);
  }

  @Test
  public void testFormAction() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_FORM_ACTION, m_csp::appendFormAction, m_csp::withFormAction);
  }

  @Test
  public void testFrameAncestors() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_FRAME_ANCESTORS, m_csp::appendFrameAncestors, m_csp::withFrameAncestors);
  }

  @Test
  public void testPluginTypes() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_PLUGIN_TYPES, m_csp::appendPluginTypes, m_csp::withPluginTypes);
  }

  @Test
  public void testToTokenWithNullValues() {
    // Test with empty content
    m_csp.withBaseUri(null);
    assertEquals("", m_csp.toToken());

    // Test adding null content to existing null content
    m_csp.appendBaseUri(null);
    assertEquals("", m_csp.toToken());

    // Test adding setting content
    m_csp.withBaseUri("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());

    // Test adding null content to existing content
    m_csp.appendBaseUri(null);
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());

    // Test adding content to existing null content
    m_csp.withBaseUri(null);
    m_csp.appendBaseUri("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());

    // Test setting or appending null values for other directives
    m_csp.withFontSrc(null);
    m_csp.appendConnectSrc(null);
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());
  }

  @Test
  public void testAppendWithDuplicatedValues() {
    m_csp.appendChildSrc("'foo'");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo'"), m_csp.toToken());

    m_csp.appendChildSrc("'bar'");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());

    m_csp.appendChildSrc("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());

    m_csp.appendChildSrc("fo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());

    m_csp.appendChildSrc("bar");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());
  }

  @Test
  public void testPutOrRemove() throws Exception {
    m_csp.withChildSrc("foo");
    assertEquals("expect only 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "foo"), m_csp.toToken());

    m_csp.withChildSrc(null);
    assertEquals("expect directive to be removed", "", m_csp.toToken());
  }

  /**
   * Runs test for one directive using method pointers to append* and with* methods.
   */
  protected void testDirective(String directive, Function<String, ContentSecurityPolicy> appendFunc, Function<String, ContentSecurityPolicy> withFunc) {
    appendFunc.apply("foo");
    assertEquals("expect only 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, directive, "foo"), m_csp.toToken());

    withFunc.apply("bar");
    assertEquals("expect only 'bar' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, directive, "bar"), m_csp.toToken());

    appendFunc.apply("foo");
    assertEquals("expect 'bar' and 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, directive, "bar", "foo"), m_csp.toToken());
  }

}
