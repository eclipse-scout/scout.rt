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
package org.eclipse.scout.rt.mom.api;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.mom.api.IDestination.IResolveMethod;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to parse config values for MOM {@link Destination}s. The value must be specified in the format of an URI:
 * <p>
 * <i>ResolveMethod</i> <code>":///"</code> <i>DestinationName</i> [ <code>"?"</code> <i>Parameters</i></code> ]
 * <ul>
 * <li><b>ResolveMethod</b> (optional), one of <code>"jndi"</code>, <code>"define"</code>. If omitted,
 * <code>"define"</code> is assumed.
 * <li><b>DestinationName</b> (mandatory) is the symbolic name of the destination.
 * <li><b>Parameters</b> (optional) is a list of key-value pairs. Key and value are separated by <code>=</code>, pairs
 * by <code>&amp;</code>.
 * </ul>
 * Example values:
 * <ul>
 * <li><code>jndi:///comp/env/jms/globalNewsTopic</code>
 * <li><code>define:///mySubject?durable=true&security=off</code>
 * </ul>
 * <h3>Recommended Format / Special Characters / Escaping</h3>
 * <p>
 * Special characters must be escaped using the URL encoding scheme (UTF-8). Because the value is parsed using the
 * {@link URI} class, it must be a valid URI. Technically, all valid URIs are accepted by the parser, including "opaque"
 * URIs. However, we recommend to always use non-opaque URIs with <i>three slashes</i> after the scheme, e.g.
 * <code>jndi:///myQueue</code>.
 * <p>
 * Using three slashes has the advantage that encoded special characters are supported anywhere in the destination name.
 * In this format, the URI's "host" part (which does not support special characters by definition) is empty and all the
 * information is contained in the "path" part (which does support encoded special characters). The parser will
 * automatically skip the empty host and strip the leading slash from the path. For example, the value
 * <code>jndi:///%2Fres</code> resolves to the destination name "/res".
 * <p>
 * Here are some commonly used characters and their encoded representation:
 * <ul>
 * <li><code>:</code> = <code>%3A</code>
 * <li><code>?</code> = <code>%3F</code>
 * <li><code>&</code> = <code>%26</code>
 * <li><code>%</code> = <code>%25</code>
 * <li><code>@</code> = <code>%40</code>
 * <li><code>/</code> = <code>%2F</code>
 * <li><i>(space)</i> = <code>%20</code>
 * </ul>
 */
@Bean
public class DestinationConfigPropertyParser {

  private static final Logger LOG = LoggerFactory.getLogger(DestinationConfigPropertyParser.class);

  protected boolean m_parsed = false;
  protected String m_rawValue;
  protected IResolveMethod m_resolveMethod;
  protected String m_destinationName;
  protected Map<String, String> m_parameters;

  /**
   * @param value
   *          raw value to parse
   * @return this instance (useful for method chaining)
   * @throws AssertionException
   *           if parsing failed or method was called more than once
   */
  public DestinationConfigPropertyParser parse(final String value) {
    Assertions.assertFalse(m_parsed, "Already parsed");
    m_rawValue = value;

    if (StringUtility.hasText(value)) {
      final URI uri = getAsUri(value);
      extractResolveMethod(uri);
      extractDestinationName(uri);
      extractParameters(uri);
    }

    m_parsed = true;
    return this;
  }

  protected URI getAsUri(final String value) {
    try {
      return new URI(value);
    }
    catch (final URISyntaxException e) {
      LOG.warn("Cannot parse value as URI: {}", value, e);
      throw new AssertionException("Cannot parse value '{}'", value);
    }
  }

  protected String urlDecode(final String s) {
    if (StringUtility.hasText(s)) {
      try {
        return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
      }
      catch (final UnsupportedEncodingException e) {
        LOG.warn("Unsupported encoding", e);
      }
    }
    return s;
  }

  protected void extractResolveMethod(final URI uri) {
    final String rawMethod = uri.getScheme();
    Assertions.assertNotNullOrEmpty(rawMethod, "Resolve method not specified, please use format [resolve-method]:///[destination-name] [{}]", m_rawValue);
    m_resolveMethod = Assertions.assertNotNull(ResolveMethod.parse(rawMethod), "Unknown resolve method: '{}' [{}]", rawMethod, m_rawValue);
  }

  protected void extractDestinationName(final URI uri) {
    String name;
    if (uri.isOpaque()) {
      // "scheme:scheme-specific-part"
      name = uri.getSchemeSpecificPart();
    }
    else {
      // "scheme://host/path?parameters=map"
      name = StringUtility.join("", uri.getHost(), uri.getPath());
      // If the host is empty, strip the path's leading slash.  This is the case if three slashes are used after
      // the scheme. This format is recommended, because the host (unlike the path) may not contain special characters.
      if (StringUtility.startsWith(name, "/")) {
        name = name.substring(1);
      }
    }
    m_destinationName = Assertions.assertNotNullOrEmpty(name, "Missing destination name [{}]", m_rawValue);
  }

  protected void extractParameters(final URI uri) {
    m_parameters = new HashMap<>();
    for (final String param : StringUtility.split(uri.getRawQuery(), "&")) {
      if (!StringUtility.hasText(param)) {
        continue;
      }
      final String[] kv = StringUtility.split(param, "=", 2);
      if (kv.length != 2) {
        continue;
      }
      final String k = StringUtility.emptyIfNull(urlDecode(kv[0]));
      final String v = StringUtility.emptyIfNull(urlDecode(kv[1]));
      m_parameters.put(k, v);
    }
  }

  /**
   * @throws AssertionError
   *           if {@link #parse(String)} was not called before
   */
  public String getDestinationName() {
    Assertions.assertTrue(m_parsed, "Not parsed yet");
    return m_destinationName;
  }

  /**
   * @throws AssertionError
   *           if {@link #parse(String)} was not called before
   */
  public IResolveMethod getResolveMethod() {
    Assertions.assertTrue(m_parsed, "Not parsed yet");
    return m_resolveMethod;
  }

  /**
   * @return modifiable map of parameters (never <code>null</code>)
   * @throws AssertionError
   *           if {@link #parse(String)} was not called before
   */
  public Map<String, String> getParameters() {
    Assertions.assertTrue(m_parsed, "Not parsed yet");
    return m_parameters;
  }
}
