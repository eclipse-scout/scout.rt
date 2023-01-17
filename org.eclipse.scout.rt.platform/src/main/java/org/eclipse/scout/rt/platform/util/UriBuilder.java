/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Builder for {@link URI} and {@link URL} instances.
 *
 * @since 3.8.1
 */
public class UriBuilder {

  private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

  private String m_scheme;
  private String m_host;
  private int m_port = -1;
  private String m_path;
  private final Map<String, String> m_parameters = new LinkedHashMap<>(); // retain order of addition
  private String m_fragment;

  public UriBuilder() {
    super();
  }

  public UriBuilder(String uri) {
    this(uri, DEFAULT_ENCODING);
  }

  public UriBuilder(String uri, String encoding) {
    this(UriUtility.toUri(uri), encoding);
  }

  public UriBuilder(URL url) {
    this(UriUtility.urlToUri(url), DEFAULT_ENCODING);
  }

  public UriBuilder(URI uri) {
    this(uri, DEFAULT_ENCODING);
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

  /**
   * Adds the given path to the builder.
   * <p>
   * Note: the path will be URL encoded when you call the {@link #createURI()} method. If the path string passed to this
   * method is already URL encoded you should decode it first to avoid double encoded characters.
   *
   * @see UriUtility#decode(String)
   */
  public UriBuilder addPath(String path) {
    if (StringUtility.hasText(path)) {
      m_path = StringUtility.join("/", removeTrailingSlash(m_path), path);
    }
    ensureAbsolutePathWithHost();
    return this;
  }

  private String removeTrailingSlash(String path) {
    if (path != null && path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }

  public UriBuilder fragment(String fragment) {
    m_fragment = fragment;
    return this;
  }

  /**
   * Adds query string parameters. The queryString argument must have the format <code>key1=foo&key2=bar[&...]</code>.
   * Each key/value pair is added as a parameter to the builder.
   */
  public UriBuilder queryString(String queryString) {
    String[] keyValuePairs = StringUtility.split(queryString, "&");
    for (String keyValuePair : keyValuePairs) {
      String[] keyValue = keyValuePair.split("=", 2);
      this.parameter(keyValue[0], (keyValue.length == 2 ? keyValue[1] : ""));
    }
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

  public UriBuilder parameters(Map<String, String> parameters) {
    if (parameters != null) {
      parameters.forEach(this::parameter);
    }
    return this;
  }

  public URL createURL() {
    return createURL(DEFAULT_ENCODING);
  }

  public URI createURI() {
    return createURI(DEFAULT_ENCODING);
  }

  public URL createURL(String encoding) {
    URI uri = createURI(encoding);
    return UriUtility.uriToUrl(uri);
  }

  public URI createURI(String encoding) {
    try {
      return createURIInternal(encoding);
    }
    catch (URISyntaxException e) {
      throw new ProcessingException("error creating URI", e);
    }
  }

  // Inspired by: https://blog.stackhunter.com/2014/03/31/encode-special-characters-java-net-uri/
  // See also: http://stackoverflow.com/questions/19917079/java-net-uri-and-percent-in-query-parameter-value
  // It seems that the URI class knows nothing about key/value pairs used by most webservers.
  // the URI class just makes sure that the query-string is correct according to the rules defined by the RFC.
  // Thus: we can and should not use URI as an object to store query-parameters. Maybe we should replace our own
  // API with a custom GetRequest implementation.
  // Another approach would be to use URL instead of URI since, the URL class does not change the query string.
  private URI createURIInternal(String encoding) throws URISyntaxException {
    final URI uri = new URI(m_scheme, null, m_host, m_port, m_path, null, null);
    final String urlWithQuery = StringUtility.join("?", uri.toString(), getQueryString(encoding));
    final String fullUrl = StringUtility.join("#", urlWithQuery, m_fragment);
    return new URI(fullUrl);
  }

  /**
   * Ensures, that the path is absolute, if the host is defined
   */
  private void ensureAbsolutePathWithHost() {
    if (m_host != null && !m_host.isEmpty() && m_path != null && !m_path.startsWith("/")) {
      m_path = "/" + m_path;
    }
  }

  private String getQueryString(String encoding) {
    Assertions.assertNotNull(encoding);
    if (m_parameters.isEmpty()) {
      return null;
    }

    StringBuilder query = new StringBuilder();
    for (Entry<String, String> param : m_parameters.entrySet()) {
      if (!StringUtility.hasText(param.getKey())) {
        continue;
      }
      if (query.length() > 0) {
        query.append("&");
      }
      query.append(UriUtility.encode(param.getKey(), encoding));
      if (!StringUtility.isNullOrEmpty(param.getValue())) {
        query.append("=");
        query.append(UriUtility.encode(param.getValue(), encoding));
      }
    }
    return query.toString();
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
    return new LinkedHashMap<>(m_parameters);
  }
}
