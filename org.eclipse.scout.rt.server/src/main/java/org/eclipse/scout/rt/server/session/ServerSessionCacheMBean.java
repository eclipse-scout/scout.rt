/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import java.util.Map.Entry;

import javax.management.ObjectName;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.jmx.MBeanUtility;
import org.eclipse.scout.rt.server.IServerSession;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@ApplicationScoped
@CreateImmediately
public class ServerSessionCacheMBean implements IServerSessionCacheMBean {

  @PostConstruct
  protected void register() {
    MBeanUtility.register(jmxObjectName(), this);
  }

  @PreDestroy
  protected void unregister() {
    MBeanUtility.unregister(jmxObjectName());
  }

  protected ObjectName jmxObjectName() {
    return MBeanUtility.toJmxName("org.eclipse.scout.rt.server", ServerSessionCache.class.getSimpleName());
  }

  @Override
  public int getCacheSize() {
    return getCache().size();
  }

  @Override
  public int getNumLockedRootLocks() {
    return getCache().numLockedRootLocks();
  }

  @Override
  public int getNumRootLocks() {
    return getCache().numRootLocks();
  }

  protected ServerSessionCache getCache() {
    return BEANS.get(ServerSessionCache.class);
  }

  @Override
  public ServerSessionCacheEntry[] getEntries() {
    return getCache()
        .cacheMap()
        .entrySet()
        .stream()
        .map(this::toServerSessionCacheEntry)
        .toArray(ServerSessionCacheEntry[]::new);
  }

  protected ServerSessionCacheEntry toServerSessionCacheEntry(Entry<String, ServerSessionEntry> mapEntry) {
    ServerSessionEntry entry = mapEntry.getValue();
    return new ServerSessionCacheEntry(mapEntry.getKey(), entry.httpSessionCount(), getServerSessionStatus(entry));
  }

  protected String getServerSessionStatus(ServerSessionEntry entry) {
    IServerSession scoutSession = entry.getScoutSession();
    if (scoutSession == null) {
      return "no session created";
    }
    if (scoutSession.isActive()) {
      return "started";
    }
    if (scoutSession.isStopping()) {
      return "stopping";
    }
    return "stopped";
  }
}
