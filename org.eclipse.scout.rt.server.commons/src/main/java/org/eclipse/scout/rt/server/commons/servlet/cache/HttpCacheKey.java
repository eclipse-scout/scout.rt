/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * Cache key is a composite of 'resourcePath' and 'locale'. When a resource is not dependent on a Locale, it should use
 * the resourcePath constructor, to create a HttpCacheKey instance.
 */
public final class HttpCacheKey implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_resourcePath;
  private final Map<String, String> m_attributes;

  public HttpCacheKey(String resourcePath) {
    this(resourcePath, null);
  }

  /**
   * @param resourcePath
   *          optional
   * @param attributes
   *          an (optional) array of arbitrary cache attributes which may be exclusive for a certain type of
   *          cache-object. For instance a HTML-document will store the 'theme' here.
   */
  public HttpCacheKey(String resourcePath, Map<String, String> attributes) {
    m_resourcePath = resourcePath;
    m_attributes = attributes != null && !attributes.isEmpty() ? new HashMap<String, String>(attributes) : null;
  }

  public String getResourcePath() {
    return m_resourcePath;
  }

  public String getAttribute(String key) {
    return m_attributes != null ? m_attributes.get(key) : null;
  }

  @Override
  public int hashCode() {
    return (m_resourcePath != null ? m_resourcePath.hashCode() : 0) + (m_attributes != null ? m_attributes.hashCode() : 0);
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
    return ObjectUtility.equals(m_resourcePath, other.m_resourcePath) && ObjectUtility.equals(m_attributes, other.m_attributes);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, false)
        .attr("resourcePath", m_resourcePath)
        .attr("attributes", m_attributes)
        .toString();
  }
}
