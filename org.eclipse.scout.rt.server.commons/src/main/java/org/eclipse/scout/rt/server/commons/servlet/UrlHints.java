/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Holder for URL param hints for servlets.
 * <p>
 * The following hints are supported:
 * <ul>
 * <li><b><code>?cache=(true|false)</code></b>: Enable/disable HTTP caching of resources. Default value is
 * <code>false</code> (<code>true</code> in development mode).
 * <li><b><code>?compress=(true|false)</code></b>: Enable/disable GZIP compression (if client supports it). Default
 * value is <code>false</code> (<code>true</code> in development mode).
 * <li><b><code>?minify=(true|false)</code></b>: Enable/disable "minification" of JS/CSS files. Default value is
 * <code>false</code> (<code>true</code> in development mode).
 * <li><b><code>?inspector=(true|false)</code></b>: Enable/disable inspector attributes in DOM ("modelClass",
 * "classId"). Default value is <code>true</code> (<code>false</code> in development mode).
 * <li><b><code>?debug=(true|false)</code></b>: Enable/disable all of the above flags.
 * </ul>
 * <p>
 * The state of all URL hints is stored as a cookie to make it available in subsequent requests without the need to
 * create a HTTP session.
 *
 * @see UrlHintsHelper
 */
@Bean
public class UrlHints implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final LazyValue<UrlHintsHelper> URL_HINTS_HELPER = new LazyValue<>(UrlHintsHelper.class);

  private static final String URL_PARAM_DEBUG = "debug";
  private static final String URL_PARAM_CACHE_HINT = "cache";
  private static final String URL_PARAM_COMPRESS_HINT = "compress";
  private static final String URL_PARAM_MINIFY_HINT = "minify";
  private static final String URL_PARAM_INSPECTOR_HINT = "inspector";

  private static final Pattern COOKIE_STRING_PATTERN = Pattern.compile("C([01])Z([01])M([01])I([01])");

  private boolean m_cache;
  private boolean m_compress;
  private boolean m_minify;
  private boolean m_inspector;

  private boolean m_changed = false;
  private boolean m_readOnly = false;

  @PostConstruct
  protected void initDefaults() {
    m_cache = !Platform.get().inDevelopmentMode();
    m_compress = !Platform.get().inDevelopmentMode();
    m_minify = !Platform.get().inDevelopmentMode();
    m_inspector = Platform.get().inDevelopmentMode();
  }

  public boolean isCache() {
    return m_cache;
  }

  public void setCache(boolean cache) {
    assertWritable();
    if (m_cache != cache) {
      m_cache = cache;
      m_changed = true;
    }
  }

  public boolean isCompress() {
    return m_compress;
  }

  public void setCompress(boolean compress) {
    assertWritable();
    if (m_compress != compress) {
      m_compress = compress;
      m_changed = true;
    }
  }

  public boolean isMinify() {
    return m_minify;
  }

  public void setMinify(boolean minify) {
    assertWritable();
    if (m_minify != minify) {
      m_minify = minify;
      m_changed = true;
    }
  }

  public boolean isInspector() {
    return m_inspector;
  }

  public void setInspector(boolean inspector) {
    assertWritable();
    if (m_inspector != inspector) {
      m_inspector = inspector;
      m_changed = true;
    }
  }

  public boolean isChanged() {
    return m_changed;
  }

  public void setChanged(boolean changed) {
    assertWritable();
    m_changed = changed;
  }

  public boolean isReadOnly() {
    return m_readOnly;
  }

  public void setReadOnly() {
    m_readOnly = true;
  }

  protected void assertWritable() {
    Assertions.assertFalse(m_readOnly, "Object is read-only");
  }

  public void setFromUrlParams(HttpServletRequest req) {
    Boolean paramDebug = getRequestParameterBoolean(req, URL_PARAM_DEBUG);
    Boolean paramCache = getRequestParameterBoolean(req, URL_PARAM_CACHE_HINT);
    Boolean paramCompress = getRequestParameterBoolean(req, URL_PARAM_COMPRESS_HINT);
    Boolean paramMinify = getRequestParameterBoolean(req, URL_PARAM_MINIFY_HINT);
    Boolean paramInspector = getRequestParameterBoolean(req, URL_PARAM_INSPECTOR_HINT);

    if (paramDebug != null) {
      setCache(!paramDebug.booleanValue());
      setCompress(!paramDebug.booleanValue());
      setMinify(!paramDebug.booleanValue());
      setInspector(paramDebug.booleanValue());
    }
    if (paramCache != null) {
      setCache(paramCache.booleanValue());
    }
    if (paramCompress != null) {
      setCompress(paramCompress.booleanValue());
    }
    if (paramMinify != null) {
      setMinify(paramMinify.booleanValue());
    }
    if (paramInspector != null) {
      setInspector(paramInspector.booleanValue());
    }
  }

  /**
   * @return {@link Boolean#TRUE} if the given URL parameter is <code>"true"</code>, {@link Boolean#FALSE} for all other
   *         values, and <code>null</code> if the parameter is not set at all.
   */
  @SuppressWarnings("findbugs:NP_BOOLEAN_RETURN_NULL") // see JavaDoc
  protected Boolean getRequestParameterBoolean(HttpServletRequest req, String parameterName) {
    String s = req.getParameter(parameterName);
    if (s == null) {
      return null;
    }
    return TypeCastUtility.castValue(s, Boolean.class);
  }

  public String toCookieString() {
    return new StringBuilder()
        .append("C").append(m_cache ? "1" : "0")
        .append("Z").append(m_compress ? "1" : "0")
        .append("M").append(m_minify ? "1" : "0")
        .append("I").append(m_inspector ? "1" : "0")
        .toString();
  }

  public void setFromCookieString(String cookieString) {
    if (cookieString == null) {
      return;
    }
    Matcher m = COOKIE_STRING_PATTERN.matcher(cookieString);
    if (m.matches()) {
      setCache("1".equals(m.group(1)));
      setCompress("1".equals(m.group(2)));
      setMinify("1".equals(m.group(3)));
      setInspector("1".equals(m.group(4)));
    }
  }

  public String toHumanReadableString() {
    return "cache=" + m_cache + ", compress=" + m_compress + ", minify=" + m_minify + ", inspector=" + m_inspector;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_changed ? 1231 : 1237);
    result = prime * result + (m_compress ? 1231 : 1237);
    result = prime * result + (m_inspector ? 1231 : 1237);
    result = prime * result + (m_minify ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    UrlHints other = (UrlHints) obj;
    if (m_changed != other.m_changed) {
      return false;
    }
    if (m_compress != other.m_compress) {
      return false;
    }
    if (m_inspector != other.m_inspector) {
      return false;
    }
    if (m_minify != other.m_minify) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    interceptToStringBuilder(builder);
    return builder.toString();
  }

  protected void interceptToStringBuilder(ToStringBuilder builder) {
    builder
        .attr("cache", m_cache)
        .attr("compress", m_compress)
        .attr("minify", m_minify)
        .attr("inspector", m_inspector)
        .attr("changed", m_changed)
        .attr("readOnly", m_readOnly);
  }

  /**
   * Static convenience method delegating to
   * {@link UrlHintsHelper#updateHints(HttpServletRequest, HttpServletResponse)}.
   */
  public static void updateHints(HttpServletRequest req, HttpServletResponse resp) {
    URL_HINTS_HELPER.get().updateHints(req, resp);
  }

  /**
   * Static convenience method delegating to {@link UrlHintsHelper#isCacheHint(HttpServletRequest)}.
   */
  public static boolean isCacheHint(HttpServletRequest req) {
    return URL_HINTS_HELPER.get().isCacheHint(req);
  }

  /**
   * Static convenience method delegating to {@link UrlHintsHelper#isCompressHint(HttpServletRequest)}.
   */
  public static boolean isCompressHint(HttpServletRequest req) {
    return URL_HINTS_HELPER.get().isCompressHint(req);
  }

  /**
   * Static convenience method delegating to {@link UrlHintsHelper#isMinifyHint(HttpServletRequest)}.
   */
  public static boolean isMinifyHint(HttpServletRequest req) {
    return URL_HINTS_HELPER.get().isMinifyHint(req);
  }

  /**
   * Static convenience method delegating to {@link UrlHintsHelper#isInspectorHint(HttpServletRequest)}.
   */
  public static boolean isInspectorHint(HttpServletRequest req) {
    return URL_HINTS_HELPER.get().isInspectorHint(req);
  }
}
