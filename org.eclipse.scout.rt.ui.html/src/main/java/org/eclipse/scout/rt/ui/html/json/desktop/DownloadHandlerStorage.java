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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.ui.desktop.IDownloadHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;

/**
 * This class manages downloadable items. Each item has a TTL, after that time the item is removed automatically. When
 * the item is removed before the timeout occurs, the scheduled removal-job will be canceled.
 */
public class DownloadHandlerStorage {

  private final Map<String, IDownloadHandler> m_valueMap = new HashMap<>();
  private final Map<String, IFuture> m_futureMap = new HashMap<>();

  protected Map<String, IDownloadHandler> valueMap() {
    return m_valueMap;
  }

  protected Map<String, IFuture> futureMap() {
    return m_futureMap;
  }

  /**
   * Put a downloadable item in the storage, after the given TTL has passed the item is removed automatically.
   */
  public void put(String key, IDownloadHandler downloadHandler) {
    long ttl = downloadHandler.getTTL();
    if (ttl <= 0) {
      throw new IllegalArgumentException("TTL must be > 0");
    }
    synchronized (m_valueMap) {
      m_valueMap.put(key, downloadHandler);
      scheduleRemoval(key, ttl);
    }
  }

  protected void scheduleRemoval(final String key, long ttl) {
    IFuture<?> future = Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        removeOnTimeout(key);
      }
    }, ttl, TimeUnit.MILLISECONDS);
    m_futureMap.put(key, future);
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
  public IDownloadHandler remove(String key) {
    IFuture future = m_futureMap.remove(key);
    if (future != null) {
      future.cancel(false);
    }
    synchronized (m_valueMap) {
      return m_valueMap.remove(key);
    }
  }
}
