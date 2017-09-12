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
package org.eclipse.scout.rt.client.services.common.icon;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract icon provider service. By default {@link #getFolderName()} links to
 * <code><em>concrete class's package name</em>.icons</code> (e.g. <em>org.eclipse.scout.sample.icons</em> if the icon
 * provider service is called <code>org.eclipse.scout.sample.SampleIconProviderService</code>).
 */
public abstract class AbstractIconProviderService implements IIconProviderService {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractIconProviderService.class);
  private static final String[] DEFAULT_ICON_EXTENSIONS = {"png", "gif", "jpg"};

  private final ReentrantReadWriteLock m_cacheLock = new ReentrantReadWriteLock();
  private final Map<String, IconSpec> m_cache = new HashMap<>();

  protected AbstractIconProviderService() {
  }

  protected boolean isCacheEnabled() {
    return true;
  }

  protected void clearCache() {
    m_cacheLock.writeLock().lock();
    try {
      m_cache.clear();
    }
    finally {
      m_cacheLock.writeLock().unlock();
    }
  }

  protected String[] getIconExtensions() {
    return DEFAULT_ICON_EXTENSIONS;
  }

  @Override
  public IconSpec getIconSpec(String iconName) {
    if (iconName == null) {
      return null;
    }
    //check cache
    m_cacheLock.readLock().lock();
    try {
      IconSpec spec = m_cache.get(iconName);
      if (spec != null || m_cache.containsKey(iconName)) {
        return spec;
      }
    }
    finally {
      m_cacheLock.readLock().unlock();
    }
    //fill cache
    m_cacheLock.writeLock().lock();
    try {
      IconSpec spec = findIconSpec(iconName);
      m_cache.put(iconName, spec);
      return spec;
    }
    finally {
      m_cacheLock.writeLock().unlock();
    }
  }

  protected IconSpec findIconSpec(String iconName) {
    String relativePathBase = iconName;
    if (StringUtility.isNullOrEmpty(relativePathBase)) {
      return null;
    }
    relativePathBase = relativePathBase.replaceAll("\\A[\\/\\\\]*", "");
    String[] exts = getIconExtensions();
    String[] relativePaths = new String[exts.length + 1];
    String[] iconNames = new String[exts.length + 1];
    relativePaths[0] = relativePathBase;
    iconNames[0] = iconName;
    for (int i = 1; i < relativePaths.length; i++) {
      relativePaths[i] = relativePathBase + "." + exts[i - 1];
      iconNames[i] = iconName + "." + exts[i - 1];
    }

    IconSpec spec = null;
    spec = findIconSpec(relativePaths, iconNames);
    return spec;
  }

  protected IconSpec findIconSpec(String[] relativePaths, String[] iconNames) {
    if (relativePaths != null && relativePaths.length > 0) {
      for (int i = 0; i < relativePaths.length; i++) {
        String relativePath = relativePaths[i];
        String iconName = iconNames[i];
        URL url = findResource(relativePath);
        if (url != null) {
          try (InputStream in = url.openStream()) {
            IconSpec iconSpec = new IconSpec();
            byte[] content = IOUtility.readBytes(in);
            if (content != null) {
              iconSpec.setContent(content);
            }
            iconSpec.setName(iconName);
            return iconSpec;
          }
          catch (Exception e) {
            LOG.error("could not read input stream from url '{}'.", url, e);
          }
        }
      }
    }
    return null;
  }

  protected abstract URL findResource(String relativePath);
}
