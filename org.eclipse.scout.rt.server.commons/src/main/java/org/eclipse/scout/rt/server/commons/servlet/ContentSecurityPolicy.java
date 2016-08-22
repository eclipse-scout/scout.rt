package org.eclipse.scout.rt.server.commons.servlet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This bean holds a modifiable set of Content Security Policy Level 2 (CSP) directives. The default rules are defined
 * by the method {@link #initDirectives()}. A "CSP token" to use in a HTTP header can be retrieved with the method
 * {@link #toToken()}.
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
 */
@Bean
public class ContentSecurityPolicy {

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
    withDefaultSrc("'self'");
    withScriptSrc("'self'");
    withStyleSrc("'self' 'unsafe-inline'");
    withFrameSrc("*");
    withChildSrc("*");
    withReportUri(HttpServletControl.CSP_REPORT_URL); // see also ContentSecurityPolicyReportHandler
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
    m_directives.put("base-uri", baseUri);
    return this;
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-child-src">https://www.w3.org/TR/CSP2/#directive-child-src</a>
   */
  public ContentSecurityPolicy withChildSrc(String childSrc) {
    m_directives.put("child-src", childSrc);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-connect-src">https://www.w3.org/TR/CSP2/#directive-connect-src</a>
   */
  public ContentSecurityPolicy withConnectSrc(String connectSrc) {
    m_directives.put("connect-src", connectSrc);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-default-src">https://www.w3.org/TR/CSP2/#directive-default-src</a>
   */
  public ContentSecurityPolicy withDefaultSrc(String defaultSrc) {
    m_directives.put("default-src", defaultSrc);
    return this;
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-font-src">https://www.w3.org/TR/CSP2/#directive-font-src</a>
   */
  public ContentSecurityPolicy withFontSrc(String fontSrc) {
    m_directives.put("font-src", fontSrc);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-form-action">https://www.w3.org/TR/CSP2/#directive-form-action</a>
   */
  public ContentSecurityPolicy withFormAction(String formAction) {
    m_directives.put("form-action", formAction);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-frame-ancestors">https://www.w3.org/TR/CSP2/#directive-frame-ancestors</a>
   */
  public ContentSecurityPolicy withFrameAncestors(String frameAncestors) {
    m_directives.put("frame-ancestors", frameAncestors);
    return this;
  }

  /**
   * @deprecated use <code>child-src</code> instead
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-frame-srcc">https://www.w3.org/TR/CSP2/#directive-frame-src</a>
   */
  @Deprecated
  public ContentSecurityPolicy withFrameSrc(String frameSrc) {
    m_directives.put("frame-src", frameSrc);
    return this;
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-img-src">https://www.w3.org/TR/CSP2/#directive-img-src</a>
   */
  public ContentSecurityPolicy withImgSrc(String imgSrc) {
    m_directives.put("img-src", imgSrc);
    return this;
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-media-src">https://www.w3.org/TR/CSP2/#directive-media-src</a>
   */
  public ContentSecurityPolicy withMediaSrc(String mediaSrc) {
    m_directives.put("media-src", mediaSrc);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-object-src">https://www.w3.org/TR/CSP2/#directive-object-src</a>
   */
  public ContentSecurityPolicy withObjectSrc(String objectSrc) {
    m_directives.put("object-src", objectSrc);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-plugin-types">https://www.w3.org/TR/CSP2/#directive-plugin-types</a>
   */
  public ContentSecurityPolicy withPluginTypes(String pluginTypes) {
    m_directives.put("plugin-types", pluginTypes);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-report-uri">https://www.w3.org/TR/CSP2/#directive-report-uri</a>
   */
  public ContentSecurityPolicy withReportUri(String reportUri) {
    m_directives.put("report-uri", reportUri);
    return this;
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-sandbox">https://www.w3.org/TR/CSP2/#directive-sandbox</a>
   */
  public ContentSecurityPolicy withSandbox(String sandbox) {
    m_directives.put("sandbox", sandbox);
    return this;
  }

  /**
   * @see <a href=
   *      "https://www.w3.org/TR/CSP2/#directive-script-src">https://www.w3.org/TR/CSP2/#directive-script-src</a>
   */
  public ContentSecurityPolicy withScriptSrc(String scriptSrc) {
    m_directives.put("script-src", scriptSrc);
    return this;
  }

  /**
   * @see <a href="https://www.w3.org/TR/CSP2/#directive-style-src">https://www.w3.org/TR/CSP2/#directive-style-src</a>
   */
  public ContentSecurityPolicy withStyleSrc(String styleSrc) {
    m_directives.put("style-src", styleSrc);
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
        cspDirectives.add(StringUtility.join(" ", entry.getKey(), entry.getValue()));
      }
    }
    return StringUtility.join("; ", cspDirectives);
  }

  @Override
  public String toString() {
    return toToken();
  }
}
