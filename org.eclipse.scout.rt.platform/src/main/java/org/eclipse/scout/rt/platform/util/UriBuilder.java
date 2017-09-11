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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

  private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

  private final Map<String, String> m_parameters = new HashMap<>();
  private String m_scheme;
  private String m_host;
  private int m_port = -1;
  private String m_path;
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
   *
   * @param queryString
   * @return
   */
  public UriBuilder queryString(String queryString) { // FIXME AWE write a unit test
    String[] keyValuePairs = queryString.split("&");
    for (String keyValuePair : keyValuePairs) {
      String[] keyValue = keyValuePair.split("=");
      this.parameter(keyValue[0], keyValue[1]);
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
    catch (MalformedURLException | URISyntaxException e) {
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
  private URI createURIInternal(String encoding) throws MalformedURLException, URISyntaxException {
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
    return CollectionUtility.copyMap(m_parameters);
  }
}
