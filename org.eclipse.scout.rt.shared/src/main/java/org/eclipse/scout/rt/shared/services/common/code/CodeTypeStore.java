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
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.shared.cache.ICache;

/**
 * CodeType store Maintains a map of partition- and language-dependent code type caches
 *
 * @since 4.3.0 (mars-M5)
 * @deprecated replaced with {@link ICache}. Will be removed in Scout 6.1.
 */
@SuppressWarnings("deprecation")
@Deprecated
public class CodeTypeStore {
  private Object m_storeLock;
  private HashMap<PartitionLanguageComposite, CodeTypeCache> m_store;

  public CodeTypeStore() {
    m_storeLock = new Object();
    m_store = new HashMap<PartitionLanguageComposite, CodeTypeCache>();
  }

  public CodeTypeCache getCodeTypeCache(Long partitionId, Locale locale) {
    synchronized (m_storeLock) {
      String key = locale.toString();
      PartitionLanguageComposite comp = new PartitionLanguageComposite(partitionId, key);

      CodeTypeCache cache = m_store.get(comp);
      if (cache == null) {
        cache = new CodeTypeCache();
        m_store.put(comp, cache);
      }
      return cache;
    }
  }

  public void unloadCodeTypeCache(Class<? extends ICodeType<?, ?>> type) {
    List<Class<? extends ICodeType<?, ?>>> codeTypeList = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    codeTypeList.add(type);
    unloadCodeTypeCache(codeTypeList);
  }

  public void unloadCodeTypeCacheNoFire(List<Class<? extends ICodeType<?, ?>>> types) {
    for (CodeTypeCache cache : m_store.values()) {
      cache.unloadCodeTypes(types);
    }
  }

  public void unloadCodeTypeCache(List<Class<? extends ICodeType<?, ?>>> types) {
    for (CodeTypeCache cache : m_store.values()) {
      cache.unloadCodeTypes(types);
    }
  }

  private class PartitionLanguageComposite {
    private Long partitionId;
    private String language;

    public PartitionLanguageComposite(Long partitionId, String language) {
      this.partitionId = partitionId;
      this.language = language;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((language == null) ? 0 : language.hashCode());
      result = prime * result + ((partitionId == null) ? 0 : partitionId.hashCode());
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
      PartitionLanguageComposite other = (PartitionLanguageComposite) obj;
      if (language == null) {
        if (other.language != null) {
          return false;
        }
      }
      else if (!language.equals(other.language)) {
        return false;
      }
      if (partitionId == null) {
        if (other.partitionId != null) {
          return false;
        }
      }
      else if (!partitionId.equals(other.partitionId)) {
        return false;
      }
      return true;
    }
  }
}
