/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.session;

import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.ObjectName;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.context.PlatformIdentifier;
import org.eclipse.scout.rt.platform.jmx.MBeanUtility;
import org.eclipse.scout.rt.server.IServerSession;

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
    return MBeanUtility.toJmxName("org.eclipse.scout.rt.server", PlatformIdentifier.get(), ServerSessionCache.class.getSimpleName());
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
