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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * This class manages downloadable items. Each item has a TTL, after that time the item is removed automatically. When
 * the item is removed before the timeout occurs, the scheduled removal-job will be canceled.
 */
@Bean
public class DownloadHandlerStorage {
  private final Map<String, BinaryResource> m_valueMap = new HashMap<>();
  private final Map<String, IFuture> m_futureMap = new HashMap<>();

  public DownloadHandlerStorage() {

  }

  protected Map<String, BinaryResource> valueMap() {
    return m_valueMap;
  }

  protected Map<String, IFuture> futureMap() {
    return m_futureMap;
  }

  protected long getTTLForResource(BinaryResource res) {
    return TimeUnit.MINUTES.toMillis(1);
  }

  /**
   * Put a downloadable item in the storage, after the given TTL has passed the item is removed automatically.
   */
  public void put(String key, BinaryResource res) {
    long ttl = getTTLForResource(res);
    synchronized (m_valueMap) {
      m_valueMap.put(key, res);
      scheduleRemoval(key, ttl);
    }
  }

  protected void scheduleRemoval(final String key, long ttl) {
    m_futureMap.put(key, Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        removeOnTimeout(key);
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withSchedulingDelay(ttl, TimeUnit.MILLISECONDS)));
  }

  protected void removeOnTimeout(String key) {
    synchronized (m_valueMap) {
      m_valueMap.remove(key);
      m_futureMap.remove(key);
    }
  }

  /**
   * Remove a downloadable item from the storage.
   */
  public BinaryResource remove(String key) {
    IFuture future = m_futureMap.remove(key);
    if (future != null) {
      future.cancel(false);
    }
    synchronized (m_valueMap) {
      return m_valueMap.remove(key);
    }
  }
}
