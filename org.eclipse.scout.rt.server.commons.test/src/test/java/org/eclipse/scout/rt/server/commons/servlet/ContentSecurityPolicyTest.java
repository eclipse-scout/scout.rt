package org.eclipse.scout.rt.server.commons.servlet;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Test;

/**
 * Test for {@link ContentSecurityPolicy}
 */
public class ContentSecurityPolicyTest {

  /**
   * Wrapper class to setup {@link ContentSecurityPolicy} for various tests. <br>
   * Note: When using Java 8 this class can be replaced with lambda expressions.
   */
  protected abstract static class P_ContentSecurityPolicyWrapper {
    private final String m_directive;

    public P_ContentSecurityPolicyWrapper(String directive) {
      m_directive = directive;
    }

    protected abstract void with(ContentSecurityPolicy csp, String source);

    protected abstract void append(ContentSecurityPolicy csp, String source);

    protected String getDirective() {
      return m_directive;
    }
  }

  @Test
  public void testBaseUri() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_BASE_URI) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withBaseUri(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendBaseUri(source);
      }
    });
  }

  @Test
  public void testStyleSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_STYLE_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withStyleSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendStyleSrc(source);
      }
    });
  }

  @Test
  public void testScriptSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withScriptSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendScriptSrc(source);
      }
    });
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testFrameSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_FRAME_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withFrameSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendFrameSrc(source);
      }
    });
  }

  @Test
  public void testChildSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_CHILD_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withChildSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendChildSrc(source);
      }
    });
  }

  @Test
  public void testReportUri() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_REPORT_URI) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withReportUri(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendReportUri(source);
      }
    });
  }

  @Test
  public void testFontSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_FONT_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withFontSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendFontSrc(source);
      }
    });
  }

  @Test
  public void testImgSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_IMG_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withImgSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendImgSrc(source);
      }
    });
  }

  @Test
  public void testMediaSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_MEDIA_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withMediaSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendMediaSrc(source);
      }
    });
  }

  @Test
  public void testObjectSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_OBJECT_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withObjectSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendObjectSrc(source);
      }
    });
  }

  @Test
  public void testSandbox() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_SANDBOX) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withSandbox(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendSandbox(source);
      }
    });
  }

  @Test
  public void testDefaultSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_DEFAULT_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withDefaultSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendDefaultSrc(source);
      }
    });
  }

  @Test
  public void testConnectSrc() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_CONNECT_SRC) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withConnectSrc(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendConnectSrc(source);
      }
    });
  }

  @Test
  public void testFormAction() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_FORM_ACTION) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withFormAction(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendFormAction(source);
      }
    });
  }

  @Test
  public void testFrameAncestors() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_FRAME_ANCESTORS) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withFrameAncestors(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendFrameAncestors(source);
      }
    });
  }

  @Test
  public void testPluginTypes() {
    testDirective(new P_ContentSecurityPolicyWrapper(ContentSecurityPolicy.DIRECTIVE_PLUGIN_TYPES) {
      @Override
      protected void with(ContentSecurityPolicy csp, String source) {
        csp.withPluginTypes(source);
      }

      @Override
      protected void append(ContentSecurityPolicy csp, String source) {
        csp.appendPluginTypes(source);
      }
    });
  }

  @Test
  public void testToTokenWithNullValues() {
    ContentSecurityPolicy csp = new ContentSecurityPolicy();
    csp.empty();

    // Test with empty content
    csp.withBaseUri(null);
    assertEquals("", csp.toToken());

    // Test adding null content to existing null content
    csp.appendBaseUri(null);
    assertEquals("", csp.toToken());

    // Test adding setting content
    csp.withBaseUri("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), csp.toToken());

    // Test adding null content to existing content
    csp.appendBaseUri(null);
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), csp.toToken());

    // Test adding content to existing null content
    csp.withBaseUri(null);
    csp.appendBaseUri("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), csp.toToken());

    // Test setting or appending null values for other directives
    csp.withFontSrc(null);
    csp.appendConnectSrc(null);
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_BASE_URI, "foo"), csp.toToken());
  }

  @Test
  public void testAppendWithDuplicatedValues() {
    ContentSecurityPolicy csp = new ContentSecurityPolicy();
    csp.empty();
    csp.appendChildSrc("'foo'");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo'"), csp.toToken());

    csp.appendChildSrc("'bar'");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), csp.toToken());

    csp.appendChildSrc("foo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), csp.toToken());

    csp.appendChildSrc("fo");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), csp.toToken());

    csp.appendChildSrc("bar");
    assertEquals(StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "'foo' 'bar'"), csp.toToken());
  }

  /**
   * Runs test for one directive using wrapper class around {@link ContentSecurityPolicy}
   */
  protected void testDirective(P_ContentSecurityPolicyWrapper wrapper) {
    ContentSecurityPolicy csp = new ContentSecurityPolicy();
    csp.empty();
    wrapper.append(csp, "foo");
    assertEquals("expect only 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, wrapper.getDirective(), "foo"), csp.toToken());

    wrapper.with(csp, "bar");
    assertEquals("expect only 'bar' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, wrapper.getDirective(), "bar"), csp.toToken());

    wrapper.append(csp, "foo");
    assertEquals("expect 'bar' and 'foo' as directive source", StringUtility.join(ContentSecurityPolicy.SOURCE_SEPARATOR, wrapper.getDirective(), "bar", "foo"), csp.toToken());
  }
}
