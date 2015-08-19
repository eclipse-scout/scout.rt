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
package org.eclipse.scout.rt.ui.html.cache;

import java.util.Locale;

/**
 * Cache key is a composite of 'resourcePath' and 'locale'. When a resource is not dependent on a Locale, it should use
 * the resourcePath constructor, to create a HttpCacheKey instance.
 */
public class HttpCacheKey {

  private final String m_resourcePath;

  private final Locale m_locale;

  public HttpCacheKey(String resourcePath) {
    this(resourcePath, null);
  }

  public HttpCacheKey(String resourcePath, Locale locale) {
    m_resourcePath = resourcePath;
    m_locale = locale;
  }

  public String getResourcePath() {
    return m_resourcePath;
  }

  public Locale getLocale() {
    return m_locale;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_locale == null) ? 0 : m_locale.hashCode());
    result = prime * result + ((m_resourcePath == null) ? 0 : m_resourcePath.hashCode());
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
    HttpCacheKey other = (HttpCacheKey) obj;
    if (m_locale == null) {
      if (other.m_locale != null) {
        return false;
      }
    }
    else if (!m_locale.equals(other.m_locale)) {
      return false;
    }
    if (m_resourcePath == null) {
      if (other.m_resourcePath != null) {
        return false;
      }
    }
    else if (!m_resourcePath.equals(other.m_resourcePath)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "HttpCacheKey[m_resourcePath=" + m_resourcePath + " m_locale=" + m_locale + "]";
  }

}
