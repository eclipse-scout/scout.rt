/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.net;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.net.internal.TTLCache;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Implementation of a java.net {@link ProxySelector}. To activate this
 * selector, call {@link ProxySelector#setDefault(ProxySelector)}. This will
 * enable the full capability of the eclipse proxy selector to the java.net
 * world. To make a connection, just create an {@link URL} and call {@link URL#openConnection()}. The
 * {@link ProxySelector} will take care of all
 * proxy related issues automatically.
 * <p>
 * This class also solves re-entrancy issues when using PAC scripts and self-references.
 * <p>
 * The config.ini property "org.eclipse.scout.net.cache" can be used to set the cache TTL in millis, default is 300'000
 * millis (5 minutes). Values &lt;=0 deactivate caching.
 * 
 * @deprecated this class is only used until
 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=299756 and
 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=257443 are solved.
 */
@Deprecated
public final class EclipseProxySelector extends ProxySelector {

  private AtomicBoolean m_initialized;
  private AtomicBoolean m_initializeInProgress;

  private boolean m_cacheEnabled;
  private Object m_cacheLock;
  private TTLCache<URI, List<Proxy>> m_cache;

  public EclipseProxySelector() {
    m_initialized = new AtomicBoolean();
    m_initializeInProgress = new AtomicBoolean();
    m_cacheEnabled = true;
    m_cacheLock = new Object();
    m_cache = new TTLCache<URI, List<Proxy>>(300000L);
    try {
      String ttlText = NetActivator.getDefault().getBundle().getBundleContext().getProperty(NetActivator.PLUGIN_ID + ".cache");
      if (ttlText != null) {
        long ttl = Long.parseLong(ttlText);
        if (ttl > 0) {
          m_cache.setTTL(ttl);
        }
        else {
          m_cacheEnabled = false;
        }
      }
    }
    catch (Throwable t) {
      //nop
    }
  }

  @Override
  public List<Proxy> select(URI uri) {
    List<Proxy> list;
    //check cache
    if (m_cacheEnabled) {
      synchronized (m_cacheLock) {
        list = m_cache.get(uri);
        if (list != null) {
          return list;
        }
      }
    }
    list = new ArrayList<Proxy>();
    IProxyData[] datas = reentrantSelectImpl(uri);
    if (datas != null) {
      for (IProxyData data : datas) {
        Proxy.Type javaNetType = null;
        if (data.getType().equals(IProxyData.SOCKS_PROXY_TYPE)) {
          javaNetType = Proxy.Type.SOCKS;
        }
        else if (data.getType().equals(IProxyData.HTTP_PROXY_TYPE)) {
          javaNetType = Proxy.Type.HTTP;
        }
        else if (data.getType().equals(IProxyData.HTTPS_PROXY_TYPE)) {
          javaNetType = Proxy.Type.HTTP;
        }
        if (javaNetType != null) {
          Proxy javaNetProxy = new Proxy(javaNetType, InetSocketAddress.createUnresolved(data.getHost(), data.getPort()));
          list.add(javaNetProxy);
        }
      }
    }
    if (list.size() == 0) {
      list.add(Proxy.NO_PROXY);
    }
    //update cache
    list = Collections.unmodifiableList(list);
    if (m_cacheEnabled) {
      synchronized (m_cacheLock) {
        m_cache.put(uri, list);
      }
    }
    return list;
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    NetActivator.getDefault().getLog().log(new Status(Status.WARNING, NetActivator.PLUGIN_ID, "Failed connecting to proxy server " + sa, ioe));
  }

  /**
   * fix for reentrancy
   */
  private IProxyData[] reentrantSelectImpl(URI uri) {
    synchronized (m_initialized) {
      if (!m_initialized.get()) {
        if (!m_initializeInProgress.get()) {
          try {
            m_initializeInProgress.set(true);
            return safeSelectImpl(uri);
          }
          finally {
            m_initialized.set(true);
            m_initializeInProgress.set(false);
          }
        }
      }
    }
    if (m_initialized.get()) {
      return safeSelectImpl(uri);
    }
    else {
      //pending init, must allow for direct connection
      return new IProxyData[0];
    }
  }

  private IProxyData[] safeSelectImpl(URI uri) {
    BundleContext context = NetActivator.getDefault().getBundle().getBundleContext();
    ServiceReference ref = context.getServiceReference(IProxyService.class.getName());
    if (ref != null) {
      try {
        IProxyService service = (IProxyService) context.getService(ref);

        //Necessary for backward compatibility to Eclipse 3.4 needed for Lotus Notes 8.5.2
        Version frameworkVersion = new Version(NetActivator.getDefault().getBundle().getBundleContext().getProperty("osgi.framework.version"));
        if (frameworkVersion.getMajor() == 3
            && frameworkVersion.getMinor() <= 4) {
          return new IProxyData[0];
        }
        else {
          try {
            Method method = IProxyService.class.getMethod("select", URI.class);
            return (IProxyData[]) method.invoke(service, uri);
          }
          catch (Exception e) {
            NetActivator.getDefault().getLog().log(new Status(Status.WARNING, NetActivator.PLUGIN_ID, "could not access method 'select' on 'IProxyService'.", e));
          }
        }
      }
      finally {
        context.ungetService(ref);
      }
    }
    return new IProxyData[0];
  }
}
