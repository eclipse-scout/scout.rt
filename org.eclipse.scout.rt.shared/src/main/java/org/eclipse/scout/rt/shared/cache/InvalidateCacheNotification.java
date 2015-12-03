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
package org.eclipse.scout.rt.shared.cache;

import java.io.Serializable;

/**
 * Notification for invalidated cache entries. Based on {@link ICacheEntryFilter}.
 * <p>
 * This class is immutable.
 * 
 * @since 5.2
 */
public class InvalidateCacheNotification implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_cacheId;
  private final ICacheEntryFilter<?, ?> m_filter;

  public InvalidateCacheNotification(String cacheId, ICacheEntryFilter<?, ?> filter) {
    super();
    if (cacheId == null) {
      throw new IllegalArgumentException("cacheId should not be null");
    }
    if (filter == null) {
      throw new IllegalArgumentException("filter should not be null");
    }
    m_cacheId = cacheId;
    m_filter = filter;
  }

  public String getCacheId() {
    return m_cacheId;
  }

  public ICacheEntryFilter<?, ?> getFilter() {
    return m_filter;
  }

  @Override
  public String toString() {
    return "InvalidateCacheNotification [cacheId=" + m_cacheId + ", filter=" + m_filter + "]";
  }
}
