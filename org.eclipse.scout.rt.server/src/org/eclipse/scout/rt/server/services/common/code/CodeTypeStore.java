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
package org.eclipse.scout.rt.server.services.common.code;

/**
 * Title: BSI Scout V3
 *  Copyright (c) 2001,2009 BSI AG
 * @version 3.x
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.services.common.clientnotification.AllUserFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.service.SERVICES;

/**
 * CodeType store in servlet context for global code providing to http sessions
 * Maintains a map of partition- and language-dependent code type caches
 */
public class CodeTypeStore {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeTypeStore.class);

  private Object m_storeLock;
  private HashMap<PartitionLanguageComposite, CodeTypeCache> m_store;

  public CodeTypeStore() {
    m_storeLock = new Object();
    m_store = new HashMap<PartitionLanguageComposite, CodeTypeCache>();
  }

  public CodeTypeCache getCodeTypeCache(Locale locale) {
    Long partitionId = 0L;
    Map<String, Object> sharedVariableMap = ServerJob.getCurrentSession().getSharedVariableMap();
    if (sharedVariableMap.containsKey(ICodeType.PROP_PARTITION_ID)) {
      partitionId = (Long) sharedVariableMap.get(ICodeType.PROP_PARTITION_ID);
    }
    return getCodeTypeCache(partitionId, locale);
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

  public void unloadCodeTypeCache(Class<? extends ICodeType<?, ?>> type) throws ProcessingException {
    List<Class<? extends ICodeType<?, ?>>> codeTypeList = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    codeTypeList.add(type);
    unloadCodeTypeCache(codeTypeList);
  }

  public void unloadCodeTypeCacheNoFire(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    for (CodeTypeCache cache : m_store.values()) {
      cache.unloadCodeTypes(types);
    }
  }

  public void unloadCodeTypeCache(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    for (CodeTypeCache cache : m_store.values()) {
      cache.unloadCodeTypes(types);
    }
    // notify clients
    SERVICES.getService(IClientNotificationService.class).putNotification(new CodeTypeChangedNotification(types), new AllUserFilter(120000L));
  }

  private class PartitionLanguageComposite {
    private Long partitionId;
    private String language;

    public PartitionLanguageComposite(Long partitionId, String language) {
      this.partitionId = partitionId;
      this.language = language;
    }

    public Long getPartitionId() {
      return partitionId;
    }

    public String getLanguage() {
      return language;
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
