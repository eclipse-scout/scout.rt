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

import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicyConfigProperties.CspDirectiveAppendProperty;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicyConfigProperties.CspDirectiveProperty;

/**
 * Content Security Policy which uses Scout defaults and can be customized using the {@link CspDirectiveProperty} and/or {@link CspDirectiveAppendProperty}.
 *
 * @see CspDirectiveProperty
 */
public class ConfigurableContentSecurityPolicy extends ContentSecurityPolicy {

  public static final Map<String, String> DEFAULTS;
  public static final char SEPARATOR_ENTRY_POINT = '#';

  @Serial
  private static final long serialVersionUID = -3275289365043640433L;

  static {
    Map<String, String> defaults = new LinkedHashMap<>();
    // fetch directives
    defaults.put(DIRECTIVE_SCRIPT_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_STYLE_SRC, EXPRESSION_SELF + SEPARATOR_EXPRESSION + EXPRESSION_UNSAFE_INLINE); // Scout requires inline styling
    defaults.put(DIRECTIVE_FONT_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_IMG_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_CONNECT_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_CHILD_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_DEFAULT_SRC, EXPRESSION_NONE); // Disable fallback handling, directives should be set explicitly
    defaults.put(DIRECTIVE_FRAME_SRC, "*"); // Everything is allowed because the iframes created by the BrowserField run in the sandbox mode and therefore handle security policy on their own.
    defaults.put(DIRECTIVE_MANIFEST_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_MEDIA_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_OBJECT_SRC, EXPRESSION_SELF);
    defaults.put(DIRECTIVE_WORKER_SRC, EXPRESSION_SELF);

    // document directives
    defaults.put(DIRECTIVE_BASE_URI, EXPRESSION_SELF);

    // navigation directives
    defaults.put(DIRECTIVE_FORM_ACTION, EXPRESSION_SELF);

    // reporting directives
    defaults.put(DIRECTIVE_REPORT_URI, REPORT_URL); // Report CSP violations to server, see ContentSecurityPolicyReportHandler

    DEFAULTS = unmodifiableMap(defaults);
  }

  @PostConstruct
  protected void initFromConfig() {
    initForPath(null); // by default initialize with the global config
  }

  /**
   * Initialize this instance with the config.properties valid for the given path.
   *
   * @param pathInfo
   *     The request path for which the configured CSP directives should be initialized.
   */
  public ContentSecurityPolicy initForPath(String pathInfo) {
    empty();
    getDefaults().forEach(this::putOrRemove);
    getConfigForEntryPoint(pathInfo, getConfig()).forEach(this::putOrRemove);
    getConfigForEntryPoint(pathInfo, getConfigAppend()).forEach(this::putOrAppend);
    return this;
  }

  protected Map<String, String> getDefaults() {
    return DEFAULTS;
  }

  protected Map<String, String> getConfigForEntryPoint(String pathInfo, Map<String, String> configMap) {
    if (configMap == null) {
      return emptyMap();
    }
    return configMap.entrySet().stream()
        .map(CspConfig::parse)
        .filter(entry -> entry.acceptPathInfo(pathInfo))
        .collect(toMap(CspConfig::directive, CspConfig::expressions, this::mergeExpressions, LinkedHashMap::new));
  }

  protected Map<String, String> getConfig() {
    return CONFIG.getPropertyValue(CspDirectiveProperty.class);
  }

  protected Map<String, String> getConfigAppend() {
    return CONFIG.getPropertyValue(CspDirectiveAppendProperty.class);
  }

  protected String mergeExpressions(String a, String b) {
    return a + SEPARATOR_EXPRESSION + b;
  }

  protected record CspConfig(String directive, String expressions, String entryPoint) {

    protected boolean acceptPathInfo(String pathInfo) {
      if (StringUtility.isNullOrEmpty(entryPoint)) {
        return true; // no entryPoint filter in config: directive is valid for all entrypoints -> always accept
      }
      if (StringUtility.isNullOrEmpty(pathInfo)) {
        return false; // only global directives requested but an entryPoint is configured: ignore
      }
      return pathInfo.equals(entryPoint) || pathInfo.endsWith('/' + entryPoint);
    }

    protected static CspConfig parse(Entry<String, String> configEntry) {
      return parse(configEntry.getKey(), configEntry.getValue());
    }

    protected static CspConfig parse(String key, String expressions) {
      int pos = key.indexOf(SEPARATOR_ENTRY_POINT);
      if (pos < 0) {
        return new CspConfig(key, expressions, null);
      }
      return new CspConfig(key.substring(0, pos), expressions, key.substring(pos + 1));
    }
  }
}
