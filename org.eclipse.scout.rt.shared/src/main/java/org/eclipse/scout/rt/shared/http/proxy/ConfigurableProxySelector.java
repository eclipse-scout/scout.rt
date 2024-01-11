/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An implementation of a {@link ProxySelector}, an instance (created using the default constructor) is installed by
 * default for all Apache HTTP Clients created using {@link ApacheHttpTransportFactory}. Additionally an instance using
 * the default constructor is also installed as default system-wide (VM-wide) {@link ProxySelector} using
 * {@link ProxySelector#setDefault(ProxySelector)}.
 * </p>
 * <p>
 * A proxy list for a specific {@link URI} is resolved in the following order:
 * </p>
 * <ul>
 * <li>No proxy is used if the {@link URI} matches a pattern defined by {@link ProxyIgnoreProperty}.</li>
 * <li>Checks if a proxy list is defined by {@link ProxyConfigurationProperty} for the specific {@link URI}.</li>
 * <li>Checks if a {@link ConfigurableProxySelector#getFallbackProxySelector()} is defined (by default none is defined)
 * and tries to resolve a proxy list using this fallback proxy selector.</li>
 * <li>If {@link ConfigurableProxySelector#isUseSystemDefaultProxySelectorAsFallback()} is enabled (enabled by default),
 * checks if a {@link ProxySelector#getDefault()} proxy selector is defined (which does not equal this proxy selector
 * itself to avoid a {@link StackOverflowError}) and tries to resolve a proxy list using this fallback proxy
 * selector.</li>
 * </ul>
 * <p>
 * As soon as the first item matches and returns at least one proxy (actually either decides for a direct connection or
 * to use a specific proxy list) no further checks are evaluated.
 * </p>
 */
@Bean
public class ConfigurableProxySelector extends ProxySelector {

  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableProxySelector.class);

  private boolean m_useSystemDefaultProxySelectorAsFallback = true;
  private ProxySelector m_fallbackProxySelector;

  /**
   * Filled during construction by {@link ProxyConfigurationProperty}.
   */
  private final Map<Pattern, Proxy> m_proxyMap;

  /**
   * Filled during construction by {@link ProxyIgnoreProperty}.
   */
  private final List<Pattern> m_proxyIgnoreList;

  public ConfigurableProxySelector() {
    this(ProxyConfigurationProperty.class, ProxyIgnoreProperty.class);
  }

  /**
   * Second constructor allowing the use of configuration properties other than {@link ProxyConfigurationProperty} and
   * {@link ProxyIgnoreProperty} if this class is not constructed by the bean manager.
   */
  public ConfigurableProxySelector(Class<? extends AbstractStringListConfigProperty> proxyProperty, Class<? extends AbstractStringListConfigProperty> ignoreProxyProperty) {
    // load proxy map
    List<String> proxyMap = CONFIG.getPropertyValue(proxyProperty);
    m_proxyMap = new HashMap<>();
    if (proxyMap != null) {
      for (String proxyConfiguration : proxyMap) {
        Pattern pattern = Pattern.compile(proxyConfiguration.substring(0, proxyConfiguration.lastIndexOf('=')), Pattern.CASE_INSENSITIVE);
        String proxyAddress = proxyConfiguration.substring(proxyConfiguration.lastIndexOf('=') + 1);
        String hostname = proxyAddress.substring(0, proxyAddress.lastIndexOf(':'));
        int port = Integer.parseInt(proxyAddress.substring(proxyAddress.lastIndexOf(':') + 1));
        Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(hostname, port));
        m_proxyMap.put(pattern, proxy);
      }
    }
    LOG.trace("Proxy configuration: {}", m_proxyMap);

    // load ignore proxy list
    List<String> proxyIgnoreList = CONFIG.getPropertyValue(ignoreProxyProperty);
    m_proxyIgnoreList = new ArrayList<>();
    if (proxyIgnoreList != null) {
      for (String ignoreProxy : proxyIgnoreList) {
        m_proxyIgnoreList.add(Pattern.compile(ignoreProxy, Pattern.CASE_INSENSITIVE));
      }
    }
    LOG.trace("Proxy ignore list: {}", m_proxyIgnoreList);
  }

  public boolean isUseSystemDefaultProxySelectorAsFallback() {
    return m_useSystemDefaultProxySelectorAsFallback;
  }

  public void setUseSystemDefaultProxySelectorAsFallback(boolean useSystemDefaultProxySelectorAsFallback) {
    m_useSystemDefaultProxySelectorAsFallback = useSystemDefaultProxySelectorAsFallback;
  }

  public ProxySelector getFallbackProxySelector() {
    return m_fallbackProxySelector;
  }

  public void setFallbackProxySelector(ProxySelector fallbackProxySelector) {
    m_fallbackProxySelector = fallbackProxySelector;
  }

  public Map<Pattern, Proxy> getProxyMap() {
    return m_proxyMap;
  }

  @Override
  public List<Proxy> select(URI uri) {
    LOG.trace("Selecting proxy for {}.", uri);

    if (!m_proxyIgnoreList.isEmpty()) {
      for (Pattern ignorePattern : m_proxyIgnoreList) {
        if (ignorePattern.matcher(uri.toString()).matches()) {
          LOG.trace("Using no proxy for {} specified by proxy ignore list.", uri);
          return Collections.singletonList(Proxy.NO_PROXY);
        }
      }
    }

    if (!m_proxyMap.isEmpty()) {
      List<Proxy> proxyList = new ArrayList<>();
      for (Entry<Pattern, Proxy> entry : m_proxyMap.entrySet()) {
        if (entry.getKey().matcher(uri.toString()).matches()) {
          proxyList.add(entry.getValue());
        }
      }
      if (!proxyList.isEmpty()) {
        LOG.trace("Using {} for {} (configured by properties).", proxyList, uri);
        return proxyList;
      }
    }

    ProxySelector fallbackProxySelector = getFallbackProxySelector();
    if (fallbackProxySelector != null) {
      List<Proxy> proxyList = fallbackProxySelector.select(uri);
      if (proxyList != null && !proxyList.isEmpty()) {
        LOG.trace("Using {} for {} (configured by fallback proxy selector).", proxyList, uri);
        return proxyList;
      }
    }

    if (isUseSystemDefaultProxySelectorAsFallback()) {
      fallbackProxySelector = ProxySelector.getDefault();
      if (fallbackProxySelector != null && fallbackProxySelector != this) {
        List<Proxy> proxyList = fallbackProxySelector.select(uri);
        if (proxyList != null && !proxyList.isEmpty()) {
          LOG.trace("Using {} for {} (configured by system default proxy selector).", proxyList, uri);
          return proxyList;
        }
      }
    }

    // no proxy found (not in configuration and also not in fallback lists)
    LOG.trace("No proxy found for {} using a direct connection.", uri);
    return Collections.singletonList(Proxy.NO_PROXY);
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    LOG.error("Connection to {} using {} as proxy failed.", uri, sa, ioe);
  }

  /**
   * Install an instance of {@link ConfigurableProxySelector} as default using
   * {@link ProxySelector#setDefault(ProxySelector)}
   */
  public static class ProxySelectorInstaller implements IPlatformListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProxySelectorInstaller.class);

    @Override
    public void stateChanged(PlatformEvent event) {
      if (event.getState() == State.BeanManagerPrepared) {
        ProxySelector.setDefault(createProxySelector());
        LOG.trace("Installed default proxy selector");;
      }
    }

    protected ConfigurableProxySelector createProxySelector() {
      return BEANS.get(ConfigurableProxySelector.class);
    }
  }
}
