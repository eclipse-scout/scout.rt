/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.csp;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy.CachedCspHash;
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

  @Test
  public void testChildSrc() {
    testDirective(ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, m_csp::appendChildSrc, m_csp::withChildSrc);
  }

  @Test
  public void testReportUri() {
    m_csp.appendReportUri("foo");
    assertEquals("report-uri foo", m_csp.toToken());

    m_csp.withReportUri("bar");
    assertEquals("report-uri bar", m_csp.toToken());

    m_csp.appendReportUri("foo");
    assertEquals("report-uri bar foo", m_csp.toToken());
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
  public void testToTokenWithNullValues() {
    // Test with empty content
    m_csp.withBaseUri(null);
    assertEquals("", m_csp.toToken());

    // Test adding null content to existing null content
    m_csp.appendBaseUri(null);
    assertEquals("", m_csp.toToken());

    // Test adding setting content
    m_csp.withBaseUri("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());

    // Test adding null content to existing content
    m_csp.appendBaseUri(null);
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());

    // Test adding content to existing null content
    m_csp.withBaseUri(null);
    m_csp.appendBaseUri("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());

    // Test setting or appending null values for other directives
    m_csp.withFontSrc(null);
    m_csp.appendConnectSrc(null);
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), m_csp.toToken());
  }

  @Test
  public void testEmptyAndNullValues() {
    assertEquals("", new ContentSecurityPolicy().empty().withFrameAncestors(null).toToken()); // null input is ignored
    assertEquals("frame-ancestors", new ContentSecurityPolicy().empty().withFrameAncestors("").toToken()); // frame-ancestors is included as empty (block all)
  }

  @Test
  public void testAppendWithDuplicatedValues() {
    m_csp.appendChildSrc("'foo'");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo'"), m_csp.toToken());

    m_csp.appendChildSrc("'bar'");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());

    m_csp.appendChildSrc("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());

    m_csp.appendChildSrc("fo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());

    m_csp.appendChildSrc("bar");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), m_csp.toToken());
  }

  @Test
  public void testPutOrRemove() {
    m_csp.withChildSrc("foo");
    assertEquals("expect only 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "foo"), m_csp.toToken());

    m_csp.withChildSrc(null);
    assertEquals("expect directive to be removed", "", m_csp.toToken());
  }

  @Test
  public void testWithSha256() {
    assertEquals("script-src 'sha256-tdQEXD9Gb6kf4sxqvnkjKhpXzfEE96JucW4KHieJ33g='", new ContentSecurityPolicy()
        .withSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, new CachedCspHash(() -> new ByteArrayInputStream("DEF".getBytes(StandardCharsets.UTF_8)))) // will be overwritten
        .withSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, new CachedCspHash(() -> new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8))))
        .toToken());
    assertEquals("", new ContentSecurityPolicy().withSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, null).toToken());
    assertEquals("", new ContentSecurityPolicy().withScriptSrc("whatever").withSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, null).toToken());
  }

  @Test
  public void testAppendSha256() {
    assertEquals("script-src 'sha256-lhtt0+3jy47LqsvWjeBAzXjrLtWIkTDM60xJJo6k1QY=' 'sha256-O2TblctVx2M5HHBxCEia4YtBEteDMA3jjgM7TJjD3q8='", new ContentSecurityPolicy()
        .appendSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, new CachedCspHash(() -> new ByteArrayInputStream("aa".getBytes(StandardCharsets.UTF_8))))
        .appendSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, new CachedCspHash(() -> new ByteArrayInputStream("bb".getBytes(StandardCharsets.UTF_8))))
        .toToken());
    assertEquals("script-src whatever 'sha256-O2TblctVx2M5HHBxCEia4YtBEteDMA3jjgM7TJjD3q8='", new ContentSecurityPolicy()
        .withScriptSrc("whatever")
        .appendSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, new CachedCspHash(() -> new ByteArrayInputStream("bb".getBytes(StandardCharsets.UTF_8))))
        .toToken());
    assertEquals("script-src whatever", new ContentSecurityPolicy()
        .withScriptSrc("whatever")
        .appendSha256(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, null)
        .toToken());
  }

  @Test
  public void testCopy() {
    ContentSecurityPolicy a = BEANS.get(ContentSecurityPolicy.class).withScriptSrc("https://eclipse.org/").withStyleSrc(null);
    ContentSecurityPolicy b = a.copy();
    assertEquals(a, b);
    assertNotSame(a, b);
  }

  @Test
  public void testWithArray() {
    ContentSecurityPolicy a = new ContentSecurityPolicy().withScriptSrc("https://eclipse.org/");

    assertEquals("", a.putOrRemove(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, (String[]) null).toToken());
    assertEquals("script-src 'self' 'unsafe-inline' 'unsafe-eval'", a.putOrRemove(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, ContentSecurityPolicy.EXPRESSION_SELF, ContentSecurityPolicy.EXPRESSION_UNSAFE_INLINE, ContentSecurityPolicy.EXPRESSION_UNSAFE_EVAL).toToken());
    assertEquals("script-src 'self' 'unsafe-inline'", a.putOrRemove(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, ContentSecurityPolicy.EXPRESSION_SELF, ContentSecurityPolicy.EXPRESSION_UNSAFE_INLINE).toToken());
    assertEquals("script-src 'self'", a.withScriptSrc(ContentSecurityPolicy.EXPRESSION_SELF).toToken());

    assertEquals("script-src https://eclipse.org/", new ContentSecurityPolicy().withScriptSrc("https://eclipse.org/").putOrAppend(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, (String[]) null).toToken());
    assertEquals("script-src https://eclipse.org/ 'self' 'unsafe-inline' 'unsafe-eval'", new ContentSecurityPolicy().withScriptSrc("https://eclipse.org/").putOrAppend(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, ContentSecurityPolicy.EXPRESSION_SELF, ContentSecurityPolicy.EXPRESSION_UNSAFE_INLINE, ContentSecurityPolicy.EXPRESSION_UNSAFE_EVAL).toToken());
    assertEquals("script-src https://eclipse.org/ 'self'", new ContentSecurityPolicy().withScriptSrc("https://eclipse.org/").putOrAppend(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, ContentSecurityPolicy.EXPRESSION_SELF).toToken());
  }

  @Test
  public void testRemoveExpression() {
    String start = "https://eclipse.org/ 'unsafe-inline' 'self' 'nonce-12345'";
    assertEquals("script-src https://eclipse.org/ 'unsafe-inline' 'nonce-12345'", new ContentSecurityPolicy()
        .withScriptSrc(start)
        .removeExpression(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, "'self'")
        .toToken());
    assertEquals("script-src https://eclipse.org/ 'unsafe-inline' 'self'", new ContentSecurityPolicy()
        .withScriptSrc(start)
        .removeExpression(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, "'nonce-12345'")
        .toToken());
    assertEquals("script-src 'unsafe-inline' 'self' 'nonce-12345'", new ContentSecurityPolicy()
        .withScriptSrc(start)
        .removeExpression(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, "https://eclipse.org/")
        .toToken());
  }

  /**
   * Runs test for one directive using method pointers to append* and with* methods.
   */
  protected void testDirective(String directive, Function<String, ContentSecurityPolicy> appendFunc, Function<String, ContentSecurityPolicy> withFunc) {
    appendFunc.apply("foo");
    assertEquals("expect only 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, directive, "foo"), m_csp.toToken());

    withFunc.apply("bar");
    assertEquals("expect only 'bar' as directive source", StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, directive, "bar"), m_csp.toToken());

    appendFunc.apply("foo");
    assertEquals("expect 'bar' and 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SEPARATOR_EXPRESSION, directive, "bar", "foo"), m_csp.toToken());
  }
}
