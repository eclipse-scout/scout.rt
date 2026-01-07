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

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static org.eclipse.scout.rt.platform.util.StringUtility.join;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicyConfigProperties.CspEnabledProperty;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicyConfigProperties.CspExclusionsProperty;

/**
 * Content Security Policy builder (Version 2). Initially the instance is completely empty.<br>
 * <b>Note: </b>This does not mean all is blocked, but all is allowed (because the absence of a directive is interpreted as 'allow everything' by browsers)!
 * To start with a policy blocking all and then selectively open as needed, use {@link BlockAllContentSecurityPolicy} instead.<br>
 * A "CSP token" to use in an HTTP header can be retrieved with the method {@link #toToken()}.
 *
 * @see <a href="https://www.w3.org/TR/CSP2/">https://www.w3.org/TR/CSP2/</a>
 */
@Bean
public class ContentSecurityPolicy implements Serializable {

  @Serial
  private static final long serialVersionUID = -6051392083382253495L;

  public static final String HTTP_HEADER = "Content-Security-Policy";
  public static final String REPORT_URL = "csp-report";

  public static final String SEPARATOR_DIRECTIVE = "; ";
  public static final String SEPARATOR_EXPRESSION = " ";

  // fetch directives
  public static final String DIRECTIVE_CHILD_SRC = "child-src";
  public static final String DIRECTIVE_CONNECT_SRC = "connect-src";
  public static final String DIRECTIVE_DEFAULT_SRC = "default-src";
  public static final String DIRECTIVE_FONT_SRC = "font-src";
  public static final String DIRECTIVE_FRAME_SRC = "frame-src";
  public static final String DIRECTIVE_IMG_SRC = "img-src";
  public static final String DIRECTIVE_MANIFEST_SRC = "manifest-src";
  public static final String DIRECTIVE_MEDIA_SRC = "media-src";
  public static final String DIRECTIVE_OBJECT_SRC = "object-src";
  public static final String DIRECTIVE_SCRIPT_SRC = "script-src";
  public static final String DIRECTIVE_STYLE_SRC = "style-src";
  public static final String DIRECTIVE_WORKER_SRC = "worker-src";

  // document directives
  public static final String DIRECTIVE_BASE_URI = "base-uri";
  public static final String DIRECTIVE_SANDBOX = "sandbox";

  // navigation directives
  public static final String DIRECTIVE_FORM_ACTION = "form-action";
  public static final String DIRECTIVE_FRAME_ANCESTORS = "frame-ancestors";

  // reporting directives
  public static final String DIRECTIVE_REPORT_URI = "report-uri";

  // expressions
  public static final String EXPRESSION_NONE = "'none'";
  public static final String EXPRESSION_SELF = "'self'";
  public static final String EXPRESSION_UNSAFE_INLINE = "'unsafe-inline'";
  public static final String EXPRESSION_UNSAFE_EVAL = "'unsafe-eval'";

  private final Map<String, String> m_directives = new LinkedHashMap<>();

  /**
   * @return unmodifiable {@link Map} of all CSP directives.
   */
  public Map<String, String> getDirectives() {
    return unmodifiableMap(m_directives);
  }

  /**
   * @return The current value of the given directive.
   */
  public String get(String directive) {
    return m_directives.get(lower(directive));
  }

  /**
   * Clear all directives from this rule set
   */
  public ContentSecurityPolicy empty() {
    m_directives.clear();
    return this;
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/base-uri">MDN</a> for more details.
   */
  public ContentSecurityPolicy withBaseUri(String baseUri) {
    return putOrRemove(DIRECTIVE_BASE_URI, baseUri);
  }

  /**
   * Appends {@code baseUri} to existing base URI directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/base-uri">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendBaseUri(String baseUri) {
    return putOrAppend(DIRECTIVE_BASE_URI, baseUri);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/child-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withChildSrc(String childSrc) {
    return putOrRemove(DIRECTIVE_CHILD_SRC, childSrc);
  }

  /**
   * Appends {@code childSrc} to existing child source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/child-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendChildSrc(String childSrc) {
    return putOrAppend(DIRECTIVE_CHILD_SRC, childSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/connect-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withConnectSrc(String connectSrc) {
    return putOrRemove(DIRECTIVE_CONNECT_SRC, connectSrc);
  }

  /**
   * Appends {@code connectSrc} to existing connect source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/connect-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendConnectSrc(String connectSrc) {
    return putOrAppend(DIRECTIVE_CONNECT_SRC, connectSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/default-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withDefaultSrc(String defaultSrc) {
    return putOrRemove(DIRECTIVE_DEFAULT_SRC, defaultSrc);
  }

  /**
   * Appends {@code defaultSrc} to existing default source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/default-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendDefaultSrc(String defaultSrc) {
    return putOrAppend(DIRECTIVE_DEFAULT_SRC, defaultSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/font-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withFontSrc(String fontSrc) {
    return putOrRemove(DIRECTIVE_FONT_SRC, fontSrc);
  }

  /**
   * Appends {@code fontSrc} to existing default font directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/font-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendFontSrc(String fontSrc) {
    return putOrAppend(DIRECTIVE_FONT_SRC, fontSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/form-action">MDN</a> for more details.
   */
  public ContentSecurityPolicy withFormAction(String formAction) {
    return putOrRemove(DIRECTIVE_FORM_ACTION, formAction);
  }

  /**
   * Appends {@code formAction} to existing form action directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/form-action">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendFormAction(String formAction) {
    return putOrAppend(DIRECTIVE_FORM_ACTION, formAction);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/frame-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withFrameSrc(String frameSrc) {
    return putOrRemove(DIRECTIVE_FRAME_SRC, frameSrc);
  }

  /**
   * Appends {@code frameSrc} to existing frame source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/frame-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendFrameSrc(String frameSrc) {
    return putOrAppend(DIRECTIVE_FRAME_SRC, frameSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/frame-ancestors">MDN</a> for more details.
   */
  public ContentSecurityPolicy withFrameAncestors(String frameAncestors) {
    return putOrRemove(DIRECTIVE_FRAME_ANCESTORS, frameAncestors);
  }

  /**
   * Appends {@code frameAncestors} to existing frame ancestors directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/frame-ancestors">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendFrameAncestors(String frameAncestors) {
    return putOrAppend(DIRECTIVE_FRAME_ANCESTORS, frameAncestors);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/img-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withImgSrc(String imgSrc) {
    return putOrRemove(DIRECTIVE_IMG_SRC, imgSrc);
  }

  /**
   * Appends {@code imgSrc} to existing image source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/img-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendImgSrc(String imgSrc) {
    return putOrAppend(DIRECTIVE_IMG_SRC, imgSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/manifest-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withManifestSrc(String manifestSrc) {
    return putOrRemove(DIRECTIVE_MANIFEST_SRC, manifestSrc);
  }

  /**
   * Appends {@code manifestSrc} to existing manifest source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/manifest-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendManifestSrc(String manifestSrc) {
    return putOrAppend(DIRECTIVE_MANIFEST_SRC, manifestSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/media-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withMediaSrc(String mediaSrc) {
    return putOrRemove(DIRECTIVE_MEDIA_SRC, mediaSrc);
  }

  /**
   * Appends {@code mediaSrc} to existing media source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/media-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendMediaSrc(String mediaSrc) {
    return putOrAppend(DIRECTIVE_MEDIA_SRC, mediaSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/object-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withObjectSrc(String objectSrc) {
    return putOrRemove(DIRECTIVE_OBJECT_SRC, objectSrc);
  }

  /**
   * Appends {@code objectSrc} to existing object source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/object-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendObjectSrc(String objectSrc) {
    return putOrAppend(DIRECTIVE_OBJECT_SRC, objectSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/report-uri">MDN</a> for more details.
   */
  public ContentSecurityPolicy withReportUri(String reportUri) {
    return putOrRemove(DIRECTIVE_REPORT_URI, reportUri);
  }

  /**
   * Appends {@code reportUri} to existing report URI directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/report-uri">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendReportUri(String reportUri) {
    return putOrAppend(DIRECTIVE_REPORT_URI, reportUri);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/sandbox">MDN</a> for more details.
   */
  public ContentSecurityPolicy withSandbox(String sandbox) {
    return putOrRemove(DIRECTIVE_SANDBOX, sandbox);
  }

  /**
   * Appends {@code sandbox} to existing sandbox directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/sandbox">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendSandbox(String sandbox) {
    return putOrAppend(DIRECTIVE_SANDBOX, sandbox);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/script-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withScriptSrc(String scriptSrc) {
    return putOrRemove(DIRECTIVE_SCRIPT_SRC, scriptSrc);
  }

  /**
   * Appends {@code scriptSrc} to existing script source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/script-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendScriptSrc(String scriptSrc) {
    return putOrAppend(DIRECTIVE_SCRIPT_SRC, scriptSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/style-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withStyleSrc(String styleSrc) {
    return putOrRemove(DIRECTIVE_STYLE_SRC, styleSrc);
  }

  /**
   * Appends {@code styleSrc} to existing style source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/style-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendStyleSrc(String styleSrc) {
    return putOrAppend(DIRECTIVE_STYLE_SRC, styleSrc);
  }

  /**
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/worker-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy withWorkerSrc(String workerSrc) {
    return putOrRemove(DIRECTIVE_WORKER_SRC, workerSrc);
  }

  /**
   * Appends {@code workerSrc} to existing worker source directive or creates new directive if it not already exists.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/worker-src">MDN</a> for more details.
   */
  public ContentSecurityPolicy appendWorkerSrc(String workerSrc) {
    return putOrAppend(DIRECTIVE_WORKER_SRC, workerSrc);
  }

  /**
   * Removes a single expression from the given directive.
   *
   * @param directive
   *     The directive to modify.
   * @param toRemove
   *     The expression within the directive to remove. Must exactly match (case-sensitive).
   * @return this
   */
  public ContentSecurityPolicy removeExpression(String directive, String toRemove) {
    String expressions = get(directive);
    if (expressions == null) {
      return this;
    }

    expressions = StringUtility.replace(expressions, toRemove, "");
    if (StringUtility.hasText(expressions)) {
      expressions = StringUtility.replace(expressions, "  ", " ").strip();
    }
    else {
      expressions = null;
    }
    return putOrRemove(directive, expressions);
  }

  /**
   * Sets the given hash as the only expression for this directive.
   *
   * @param directive
   *     The required directive to replace.
   * @param hash
   *     The {@link CachedCspHash} or {@code null} if the directive should be removed.
   * @return this
   */
  public ContentSecurityPolicy withSha256(String directive, CachedCspHash hash) {
    return putOrRemove(directive, hash == null ? null : "'" + hash.getToken() + "'");
  }

  /**
   * Appends the given hash to the given directive.
   *
   * @param directive
   *     The required directive to append to.
   * @param hash
   *     The {@link CachedCspHash} to append. If {@code null}, nothing is changed.
   * @return this.
   */
  public ContentSecurityPolicy appendSha256(String directive, CachedCspHash hash) {
    if (hash == null) {
      return this;
    }
    return putOrAppend(directive, "'" + hash.getToken() + "'");
  }

  /**
   * Replaces the given directive with the given expressions.
   *
   * @param directive
   *     The required directive to replace.
   * @param expressions
   *     The new expression. If {@code null}, the directive is removed. Otherwise, it is replaced with the given expressions.
   * @return this
   */
  public ContentSecurityPolicy putOrRemove(String directive, String... expressions) {
    return putOrRemove(directive, expressions == null ? null : joinExpressions(expressions));
  }

  /**
   * Overwrites the given directive or removes it.
   *
   * @param directive
   *     The required directive to modify.
   * @param expressions
   *     The new expressions. If it is {@code null}, the directive is removed, otherwise it is set to the given expressions (existing are replaced).
   * @return this
   */
  public ContentSecurityPolicy putOrRemove(String directive, String expressions) {
    if (!StringUtility.hasText(directive)) {
      return this;
    }

    String key = lower(directive);
    if (expressions == null) {
      m_directives.remove(key);
    }
    else {
      m_directives.put(key, expressions);
    }
    return this;
  }

  protected String lower(String s) {
    if (s == null) {
      return null;
    }
    return s.toLowerCase(Locale.ENGLISH);
  }

  protected String joinExpressions(String... expressions) {
    return Arrays.stream(expressions)
        .filter(StringUtility::hasText)
        .collect(joining(SEPARATOR_EXPRESSION));
  }

  /**
   * Appends the given expressions to the given directive.
   *
   * @param directive
   *     The required directive.
   * @param expressions
   *     The expressions to append. If {@code null}, nothing is changed.
   * @return this
   */
  public ContentSecurityPolicy putOrAppend(String directive, String... expressions) {
    if (expressions == null) {
      return this;
    }
    return putOrAppend(directive, joinExpressions(expressions));
  }

  /**
   * Appends the given expression to the given directive. If the directive does not exist yet, it is created.
   *
   * @param directive
   *     The required directive to modify.
   * @param expression
   *     The expression to add. If {@code null}, nothing is changed.
   * @return this.
   */
  public ContentSecurityPolicy putOrAppend(String directive, String expression) {
    if (expression == null) {
      return this;
    }
    if (!StringUtility.hasText(directive)) {
      return this;
    }

    String key = lower(directive);
    String existingSource = m_directives.get(key);
    if (StringUtility.hasText(existingSource)) {
      // Check for duplicates and do not add new expression, if expression already is part of the existing source
      if (!existingSource.contains(expression)) {
        m_directives.put(key, join(SEPARATOR_EXPRESSION, existingSource, expression));
      }
    }
    else {
      m_directives.put(key, expression);
    }
    return this;
  }

  public ContentSecurityPolicy copy() {
    try {
      // do not use the BeanManager here as any @PostConstruct should not be executed
      ContentSecurityPolicy copy = getClass().getConstructor().newInstance();
      copy.m_directives.putAll(m_directives);
      return copy;
    }
    catch (ReflectiveOperationException e) {
      throw new PlatformException("Unable to create new instance of '{}' using default constructor. ", getClass(), e);
    }
  }

  /**
   * @return a string describing all directives in this rule set, suitable as value for the {@link #HTTP_HEADER Content-Security-Policy HTTP header}.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy">MDN</a>.
   */
  public String toToken() {
    return m_directives.entrySet().stream()
        .map(entry -> join(SEPARATOR_EXPRESSION, entry.getKey(), entry.getValue()))
        .collect(joining(SEPARATOR_DIRECTIVE));
  }

  @Override
  public String toString() {
    return toToken();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ContentSecurityPolicy that = (ContentSecurityPolicy) o;
    return m_directives.equals(that.m_directives);
  }

  @Override
  public int hashCode() {
    return m_directives.hashCode();
  }

  /**
   * @param pathInfo
   *     The URL pathInfo to check.
   * @return if the CSP is enabled for the given path.
   */
  public boolean isEnabled(String pathInfo) {
    if (!CONFIG.getPropertyValue(CspEnabledProperty.class)) {
      return false;
    }
    List<Pattern> exclusions = CONFIG.getPropertyValue(CspExclusionsProperty.class);
    if (CollectionUtility.isEmpty(exclusions) || pathInfo == null) {
      return true;
    }
    for (Pattern exclusion : exclusions) {
      if (exclusion.matcher(pathInfo).matches()) {
        return false;
      }
    }
    return true;
  }

  /**
   * SHA-256 lazily computed (on first use) for the given {@link InputStream}. The hash is only computed once per instance.
   */
  @SuppressWarnings("ClassCanBeRecord")
  public static class CachedCspHash {
    private final LazyValue<String> m_hash;

    /**
     * @param dataSupplier
     *     Data source for the hash. Must not be {@code null}.
     */
    public CachedCspHash(Supplier<InputStream> dataSupplier) {
      Assertions.assertNotNull(dataSupplier); // early fail
      m_hash = new LazyValue<>(() -> computeSha256(dataSupplier));
    }

    /**
     * @return The CSP SHA-256 token including the prefix but without quotes.<br>
     * E.g.: {@code sha256-tdQEXD9Gb6kf4sxqvnkjKhpXzfEE96JucW4KHieJ33g=}
     */
    public String getToken() {
      return m_hash.get();
    }

    protected String computeSha256(Supplier<InputStream> dataSupplier) {
      try (InputStream data = dataSupplier.get()) {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        int n;
        byte[] buf = new byte[8192];
        while ((n = data.read(buf)) >= 0) {
          sha256.update(buf, 0, n);
        }
        String hash = Base64Utility.encode(sha256.digest(), false /* CSP spec allows url-safe and non-url-safe encoding */);
        return "sha256-" + hash;
      }
      catch (IOException | NoSuchAlgorithmException ex) {
        throw new ProcessingException("Error computing SHA-256.", ex);
      }
    }
  }
}
