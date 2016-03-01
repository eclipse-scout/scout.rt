/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for {@link URI} and {@link URL} instances.
 *
 * @since 3.8.1
 */
public class UriBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(UriBuilder.class);

  private final Map<String, String> m_parameters = new HashMap<>();
  private String m_scheme;
  private String m_host;
  private int m_port = -1;
  private String m_path;
  private String m_fragment;

  public UriBuilder() {
  }

  public UriBuilder(String uri) {
    this(uri, null);
  }

  public UriBuilder(String uri, String encoding) {
    this(UriUtility.toUri(uri), encoding);
  }

  public UriBuilder(URL url) {
    this(UriUtility.urlToUri(url), null);
  }

  public UriBuilder(URI uri) {
    this(uri, null);
  }

  public UriBuilder(URL url, String encoding) {
    this(UriUtility.urlToUri(url), encoding);
  }

  public UriBuilder(URI uri, String encoding) {
    if (uri == null) {
      return;
    }
    m_scheme = uri.getScheme();
    m_host = uri.getHost();
    m_port = uri.getPort();
    m_path = uri.getPath();
    m_fragment = uri.getFragment();
    Map<String, String> params = UriUtility.getQueryParameters(uri, encoding);
    m_parameters.putAll(params);
  }

  public UriBuilder scheme(String scheme) {
    m_scheme = scheme;
    return this;
  }

  public UriBuilder host(String host) {
    m_host = host;
    return this;
  }

  /**
   * Sets the remote host's port. Values lower than 1 are resetting a possibly set explicit port.
   *
   * @param port
   * @return
   */
  public UriBuilder port(int port) {
    if (port < 1) {
      m_port = -1;
    }
    else {
      m_port = port;
    }
    return this;
  }

  public UriBuilder path(String path) {
    m_path = path;
    return this;
  }

  public UriBuilder addPath(String path) {
    if (StringUtility.hasText(path)) {
      m_path = StringUtility.join("/", m_path, path);
    }
    return this;
  }

  public UriBuilder fragment(String fragment) {
    m_fragment = fragment;
    return this;
  }

  public UriBuilder parameter(String name, String value) {
    if (!StringUtility.hasText(name)) {
      return this;
    }
    if (value == null) {
      m_parameters.remove(name);
    }
    else {
      m_parameters.put(name, value);
    }
    return this;
  }

  public URL createURL() {
    return createURL(null);
  }

  public URI createURI() {
    return createURI(null);
  }

  public URL createURL(String encoding) {
    return UriUtility.uriToUrl(createURI(encoding));
  }

  public URI createURI(String encoding) {
    try {
      if (m_parameters == null || m_parameters.isEmpty()) {
        return new URI(m_scheme, null, m_host, m_port, m_path, null, m_fragment);
      }
      if (encoding == null) {
        encoding = UriUtility.ISO_8859_1;
      }
      StringBuilder query = new StringBuilder();
      for (Map.Entry<String, String> param : m_parameters.entrySet()) {
        if (!StringUtility.hasText(param.getKey())) {
          LOG.warn("ignoring parameter with empty key");
          continue;
        }
        if (query.length() > 0) {
          query.append("&");
        }
        try {
          query.append(URLEncoder.encode(param.getKey(), encoding));
          query.append("=");
          query.append(URLEncoder.encode(param.getValue(), encoding));
        }
        catch (UnsupportedEncodingException e) {
          throw new ProcessingException("Unsupported encoding '" + encoding + "'", e);
        }
      }
      return new URI(m_scheme, null, m_host, m_port, m_path, query.toString(), m_fragment);
    }
    catch (URISyntaxException e) {
      throw new ProcessingException("error creating URI", e);
    }
  }

  public String getScheme() {
    return m_scheme;
  }

  public String getHost() {
    return m_host;
  }

  public int getPort() {
    return m_port;
  }

  public String getPath() {
    return m_path;
  }

  public String getFragment() {
    return m_fragment;
  }

  public Map<String, String> getParameters() {
    return CollectionUtility.copyMap(m_parameters);
  }
}
