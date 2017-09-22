package org.eclipse.scout.rt.server.commons.servlet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.CspEnabledProperty.CspDirectiveProperty;

/**
 * This bean holds a modifiable set of Content Security Policy Level 2 (CSP) directives. The default rules are defined
 * by the method {@link #initDirectives()}. A "CSP token" to use in a HTTP header can be retrieved with the method
 * {@link #toToken()}.
 * <p>
 * Use the 'scout.csp.directive' config property to configure individual CSP directives in your Scout application.
 * <p>
 * Possible values for directives:
 * <table border=1 cellpadding=3 cellspacing=0>
 * <tr>
 * <td><code>*</code></td>
 * <td>Allows any resources</td>
 * </tr>
 * <tr>
 * <td><code>'none'</code></td>
 * <td>Allows no resources at all (= deny all)</td>
 * </tr>
 * <tr>
 * <td><code>'self'</code></td>
 * <td>Allows resources from same origin (same scheme, host, port)</td>
 * </tr>
 * <tr>
 * <td><code>data:</code></td>
 * <td>Allows resources from data: scheme URIs</td>
 * </tr>
 * <tr>
 * <td><i>domain.example.com</i></td>
 * <td>Allows resources from the specified domain name (but any scheme)</td>
 * </tr>
 * <tr>
 * <td><i>*.example.com</i></td>
 * <td>Allows resources from all sub-domains under example.com</td>
 * </tr>
 * <tr>
 * <td><i>https://domain.example.com</i></td>
 * <td>Allows resources from the specified domain name (but only https: scheme)</td>
 * </tr>
 * <tr>
 * <td><i>https:</i></td>
 * <td>Allows resources from all domains (but only https: scheme)</td>
 * </tr>
 * <tr>
 * <td><code>'unsafe-inline'</code></td>
 * <td>Allows use of inline elements</td>
 * </tr>
 * <tr>
 * <td><code>'unsafe-eval'</code></td>
 * <td>Allows use of JavaScript's <i>eval()</i></td>
 * </tr>
 * </table>
 *
 * @see <a href="https://www.w3.org/TR/CSP2/">https://www.w3.org/TR/CSP2/</a>
 * @see CspDirectiveProperty
 */
@Bean
public class ContentSecurityPolicy {

  public static final String DIRECTIVE_SEPARATOR = "; ";
  public static final String SOURCE_SEPARATOR = " ";

  public static final String DIRECTIVE_BASE_URI = "base-uri";
  public static final String DIRECTIVE_IMG_SRC = "img-src";
  public static final String DIRECTIVE_STYLE_SRC = "style-src";
  public static final String DIRECTIVE_CHILD_SRC = "child-src";
  public static final String DIRECTIVE_CONNECT_SRC = "connect-src";
  public static final String DIRECTIVE_DEFAULT_SRC = "default-src";
  public static final String DIRECTIVE_FONT_SRC = "font-src";
  public static final String DIRECTIVE_FORM_ACTION = "form-action";
  public static final String DIRECTIVE_FRAME_ANCESTORS = "frame-ancestors";
  public static final String DIRECTIVE_MEDIA_SRC = "media-src";
  public static final String DIRECTIVE_OBJECT_SRC = "object-src";
  public static final String DIRECTIVE_PLUGIN_TYPES = "plugin-types";
  public static final String DIRECTIVE_REPORT_URI = "report-uri";
  public static final String DIRECTIVE_SANDBOX = "sandbox";
  public static final String DIRECTIVE_SCRIPT_SRC = "script-src";
  /**
   * @deprecated use <code>child-src</code> instead
   */
  @Deprecated
  public static final String DIRECTIVE_FRAME_SRC = "frame-src";

  private final Map<String, String> m_directives = new LinkedHashMap<>();

  public ContentSecurityPolicy() {
    initDirectives();
  }

  /**
   * Default rules for content security policy (CSP):
   * <ul>
   * <li><b>default-src 'self'</b><br>
   * Only accept 'self' sources by default.</li>
   * <li><b>script-src 'self'</b><br>
   * <li><b>style-src 'self' 'unsafe-inline'</b><br>
   * Without inline styling many widgets would not work as expected.</li>
   * <li><b>frame-src *; child-src *</b><br>
   * Everything is allowed because the iframes created by the browser field run in the sandbox mode and therefore handle
   * the security policy by their own.</li>
   * <li><b>report-uri {@link HttpServletControl#CSP_REPORT_URL}</b><br>
   * Report CSP violations to server, see ContentSecurityPolicyReportHandler</li>
   * </ul>
   */
  protected void initDirectives() {
    withBaseUri(getConfiguredDefault(DIRECTIVE_BASE_URI, null));
    withImgSrc(getConfiguredDefault(DIRECTIVE_IMG_SRC, "'self'"));
    withStyleSrc(getConfiguredDefault(DIRECTIVE_STYLE_SRC, "'self' 'unsafe-inline'"));
    withChildSrc(getConfiguredDefault(DIRECTIVE_CHILD_SRC, "*"));
    withConnectSrc(getConfiguredDefault(DIRECTIVE_CONNECT_SRC, null));
    withDefaultSrc(getConfiguredDefault(DIRECTIVE_DEFAULT_SRC, "'self'"));
    withFontSrc(getConfiguredDefault(DIRECTIVE_FONT_SRC, null));
    withFormAction(getConfiguredDefault(DIRECTIVE_FORM_ACTION, null));
    withFrameAncestors(getConfiguredDefault(DIRECTIVE_FRAME_ANCESTORS, null));
    withMediaSrc(getConfiguredDefault(DIRECTIVE_MEDIA_SRC, null));
    withObjectSrc(getConfiguredDefault(DIRECTIVE_OBJECT_SRC, null));
    withPluginTypes(getConfiguredDefault(DIRECTIVE_PLUGIN_TYPES, null));
    withReportUri(getConfiguredDefault(DIRECTIVE_REPORT_URI, HttpServletControl.CSP_REPORT_URL)); // see also ContentSecurityPolicyReportHandler
    withSandbox(getConfiguredDefault(DIRECTIVE_SANDBOX, null));
    withScriptSrc(getConfiguredDefault(DIRECTIVE_SCRIPT_SRC, "'self'"));
    withFrameSrc(getConfiguredDefault(DIRECTIVE_FRAME_SRC, "*"));
  }

  protected String getConfiguredDefault(String directiveKey, String fallbackValue) {
    Map<String, String> mapProperty = CONFIG.getPropertyValue(CspDirectiveProperty.class);
    if (mapProperty == null) {
      return fallbackValue;
    }
    String configValue = mapProperty.get(directiveKey);
    if (configValue == null) {
      return fallbackValue;
    }
    return configValue;
  }

  /**
   * Settings a directive to null is the same as removing the directive entirely.
   */
  protected void putOrRemove(String key, String value) {
    if (value == null) {
      m_directives.remove(key);
    }
    else {
      m_directives.put(key, value);
    }
  }

  /**
   * @return live map of all CSP directives in this rule set
   */
  public final Map<String, String> getDirectives() {
    return m_directives;
  }

  /**
   * Clear all directives from this rule set
   */
  public void empty() {
    m_directives.clear();
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-base-uri">https://www.w3.org/TR/CSP2/#directive-base-uri</a>
   */
  public ContentSecurityPolicy withBaseUri(String baseUri) {
    putOrRemove(DIRECTIVE_BASE_URI, baseUri);
    return this;
  }

  /**
   * Appends {@code baseUri} to existing base URI directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-base-uri">https://www.w3.org/TR/CSP2/#directive-base-uri</a>
   */
  public ContentSecurityPolicy appendBaseUri(String baseUri) {
    return addOrAppend(DIRECTIVE_BASE_URI, baseUri);
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-child-src">https://www.w3.org/TR/CSP2/#directive-child-src</a>
   */
  public ContentSecurityPolicy withChildSrc(String childSrc) {
    putOrRemove(DIRECTIVE_CHILD_SRC, childSrc);
    return this;
  }

  /**
   * Appends {@code childSrc} to existing child source directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-child-src">https://www.w3.org/TR/CSP2/#directive-child-src</a>
   */
  public ContentSecurityPolicy appendChildSrc(String childSrc) {
    return addOrAppend(DIRECTIVE_CHILD_SRC, childSrc);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-connect-src">https://www.w3.org/TR/CSP2/#directive-connect-src</a>
   */
  public ContentSecurityPolicy withConnectSrc(String connectSrc) {
    putOrRemove(DIRECTIVE_CONNECT_SRC, connectSrc);
    return this;
  }

  /**
   * Appends {@code connectSrc} to existing connect source directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-connect-src">https://www.w3.org/TR/CSP2/#directive-connect-src</a>
   */
  public ContentSecurityPolicy appendConnectSrc(String connectSrc) {
    return addOrAppend(DIRECTIVE_CONNECT_SRC, connectSrc);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-default-src">https://www.w3.org/TR/CSP2/#directive-default-src</a>
   */
  public ContentSecurityPolicy withDefaultSrc(String defaultSrc) {
    putOrRemove(DIRECTIVE_DEFAULT_SRC, defaultSrc);
    return this;
  }

  /**
   * Appends {@code defaultSrc} to existing default source directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-default-src">https://www.w3.org/TR/CSP2/#directive-default-src</a>
   */
  public ContentSecurityPolicy appendDefaultSrc(String defaultSrc) {
    return addOrAppend(DIRECTIVE_DEFAULT_SRC, defaultSrc);
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-font-src">https://www.w3.org/TR/CSP2/#directive-font-src</a>
   */
  public ContentSecurityPolicy withFontSrc(String fontSrc) {
    putOrRemove(DIRECTIVE_FONT_SRC, fontSrc);
    return this;
  }

  /**
   * Appends {@code fontSrc} to existing default font directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-font-src">https://www.w3.org/TR/CSP2/#directive-font-src</a>
   */
  public ContentSecurityPolicy appendFontSrc(String fontSrc) {
    return addOrAppend(DIRECTIVE_FONT_SRC, fontSrc);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-form-action">https://www.w3.org/TR/CSP2/#directive-form-action</a>
   */
  public ContentSecurityPolicy withFormAction(String formAction) {
    putOrRemove(DIRECTIVE_FORM_ACTION, formAction);
    return this;
  }

  /**
   * Appends {@code formAction} to existing form action directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-form-action">https://www.w3.org/TR/CSP2/#directive-form-action</a>
   */
  public ContentSecurityPolicy appendFormAction(String formAction) {
    return addOrAppend(DIRECTIVE_FORM_ACTION, formAction);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-frame-ancestors">https://www.w3.org/TR/CSP2/#directive-frame-ancestors</a>
   */
  public ContentSecurityPolicy withFrameAncestors(String frameAncestors) {
    putOrRemove(DIRECTIVE_FRAME_ANCESTORS, frameAncestors);
    return this;
  }

  /**
   * Appends {@code frameAncestors} to existing frame ancestors directive or creates new directive if it not already
   * exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-frame-ancestors">https://www.w3.org/TR/CSP2/#directive-frame-ancestors</a>
   */
  public ContentSecurityPolicy appendFrameAncestors(String frameAncestors) {
    return addOrAppend(DIRECTIVE_FRAME_ANCESTORS, frameAncestors);
  }

  /**
   * @deprecated use <code>child-src</code> instead
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-frame-srcc">https://www.w3.org/TR/CSP2/#directive-frame-src</a>
   */
  @Deprecated
  public ContentSecurityPolicy withFrameSrc(String frameSrc) {
    putOrRemove(DIRECTIVE_FRAME_SRC, frameSrc);
    return this;
  }

  /**
   * Appends {@code frameSrc} to existing frame source directive or creates new directive if it not already exists.
   *
   * @deprecated use <code>child-src</code> instead
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-frame-srcc">https://www.w3.org/TR/CSP2/#directive-frame-src</a>
   */
  @Deprecated
  public ContentSecurityPolicy appendFrameSrc(String frameSrc) {
    return addOrAppend(DIRECTIVE_FRAME_SRC, frameSrc);
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-img-src">https://www.w3.org/TR/CSP2/#directive-img-src</a>
   */
  public ContentSecurityPolicy withImgSrc(String imgSrc) {
    putOrRemove(DIRECTIVE_IMG_SRC, imgSrc);
    return this;
  }

  /**
   * Appends {@code imgSrc} to existing image source directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-img-src">https://www.w3.org/TR/CSP2/#directive-img-src</a>
   */
  public ContentSecurityPolicy appendImgSrc(String imgSrc) {
    return addOrAppend(DIRECTIVE_IMG_SRC, imgSrc);
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-media-src">https://www.w3.org/TR/CSP2/#directive-media-src</a>
   */
  public ContentSecurityPolicy withMediaSrc(String mediaSrc) {
    putOrRemove(DIRECTIVE_MEDIA_SRC, mediaSrc);
    return this;
  }

  /**
   * Appends {@code mediaSrc} to existing media source directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-media-src">https://www.w3.org/TR/CSP2/#directive-media-src</a>
   */
  public ContentSecurityPolicy appendMediaSrc(String mediaSrc) {
    return addOrAppend(DIRECTIVE_MEDIA_SRC, mediaSrc);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-object-src">https://www.w3.org/TR/CSP2/#directive-object-src</a>
   */
  public ContentSecurityPolicy withObjectSrc(String objectSrc) {
    putOrRemove(DIRECTIVE_OBJECT_SRC, objectSrc);
    return this;
  }

  /**
   * Appends {@code objectSrc} to existing object source directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-object-src">https://www.w3.org/TR/CSP2/#directive-object-src</a>
   */
  public ContentSecurityPolicy appendObjectSrc(String objectSrc) {
    return addOrAppend(DIRECTIVE_OBJECT_SRC, objectSrc);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-plugin-types">https://www.w3.org/TR/CSP2/#directive-plugin-types</a>
   */
  public ContentSecurityPolicy withPluginTypes(String pluginTypes) {
    putOrRemove(DIRECTIVE_PLUGIN_TYPES, pluginTypes);
    return this;
  }

  /**
   * Appends {@code pluginTypes} to existing pluginTypes directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-plugin-types">https://www.w3.org/TR/CSP2/#directive-plugin-types</a>
   */
  public ContentSecurityPolicy appendPluginTypes(String pluginTypes) {
    return addOrAppend(DIRECTIVE_PLUGIN_TYPES, pluginTypes);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-report-uri">https://www.w3.org/TR/CSP2/#directive-report-uri</a>
   */
  public ContentSecurityPolicy withReportUri(String reportUri) {
    putOrRemove(DIRECTIVE_REPORT_URI, reportUri);
    return this;
  }

  /**
   * Appends {@code reportUri} to existing report URI directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-report-uri">https://www.w3.org/TR/CSP2/#directive-report-uri</a>
   */
  public ContentSecurityPolicy appendReportUri(String reportUri) {
    return addOrAppend(DIRECTIVE_REPORT_URI, reportUri);
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-sandbox">https://www.w3.org/TR/CSP2/#directive-sandbox</a>
   */
  public ContentSecurityPolicy withSandbox(String sandbox) {
    putOrRemove(DIRECTIVE_SANDBOX, sandbox);
    return this;
  }

  /**
   * Appends {@code sandbox} to existing sandbox directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-sandbox">https://www.w3.org/TR/CSP2/#directive-sandbox</a>
   */
  public ContentSecurityPolicy appendSandbox(String sandbox) {
    return addOrAppend(DIRECTIVE_SANDBOX, sandbox);
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-script-src">https://www.w3.org/TR/CSP2/#directive-script-src</a>
   */
  public ContentSecurityPolicy withScriptSrc(String scriptSrc) {
    putOrRemove(DIRECTIVE_SCRIPT_SRC, scriptSrc);
    return this;
  }

  /**
   * Appends {@code scriptSrc} to existing script source directive or creates new directive if it not already exists.
   *
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-script-src">https://www.w3.org/TR/CSP2/#directive-script-src</a>
   */
  public ContentSecurityPolicy appendScriptSrc(String scriptSrc) {
    return addOrAppend(DIRECTIVE_SCRIPT_SRC, scriptSrc);
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-style-src">https://www.w3.org/TR/CSP2/#directive-style-src</a>
   */
  public ContentSecurityPolicy withStyleSrc(String styleSrc) {
    putOrRemove(DIRECTIVE_STYLE_SRC, styleSrc);
    return this;
  }

  /**
   * Appends {@code styleSrc} to existing style source directive or creates new directive if it not already exists.
   *
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-style-src">https://www.w3.org/TR/CSP2/#directive-style-src</a>
   */
  public ContentSecurityPolicy appendStyleSrc(String styleSrc) {
    return addOrAppend(DIRECTIVE_STYLE_SRC, styleSrc);
  }

  /**
   * Adds {@code value} for {@code key} directive or appends {@code value} if directive with {@code key} already exists.
   */
  protected ContentSecurityPolicy addOrAppend(String key, String value) {
    if (value == null) {
      return this;
    }
    if (m_directives.containsKey(key)) {
      String existingSource = m_directives.get(key);
      // Check for duplicates and do not add new value, if value already is part of the existing source
      if (!StringUtility.containsString(existingSource, value)) {
        m_directives.put(key, StringUtility.join(SOURCE_SEPARATOR, existingSource, value));
      }
    }
    else {
      m_directives.put(key, value);
    }
    return this;
  }

  /**
   * @return a string describing all directives in this rule set, suitable as value for the
   *         <code>"Content-Security-Policy"</code> HTTP header
   * @see <a href="https://www.w3.org/TR/CSP2/#policy_token">https://www.w3.org/TR/CSP2/#policy_token</a>
   */
  public String toToken() {
    List<String> cspDirectives = new ArrayList<>();
    for (Entry<String, String> entry : m_directives.entrySet()) {
      if (entry.getKey() != null && entry.getValue() != null) {
        cspDirectives.add(StringUtility.join(SOURCE_SEPARATOR, entry.getKey(), entry.getValue()));
      }
    }
    return StringUtility.join(DIRECTIVE_SEPARATOR, cspDirectives);
  }

  @Override
  public String toString() {
    return toToken();
  }
}
