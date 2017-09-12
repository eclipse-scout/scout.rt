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

import org.eclipse.scout.rt.client.ui.desktop.IOpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.server.commons.servlet.cache.DownloadHttpResponseInterceptor;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;

/**
 * This class manages downloadable items. Each item has a TTL, after that time the item is removed automatically. When
 * the item is removed before the timeout occurs, the scheduled removal-job will be canceled.
 */
@Bean
public class DownloadHandlerStorage {

  public static class BinaryResourceHolderWithAction {
    private final BinaryResourceHolder m_holder;
    private final IOpenUriAction m_openUriAction;

    public BinaryResourceHolderWithAction(BinaryResourceHolder holder, IOpenUriAction openUriAction) {
      m_holder = holder;
      m_openUriAction = openUriAction;
    }

    public BinaryResourceHolder getHolder() {
      return m_holder;
    }

    public IOpenUriAction getOpenUriAction() {
      return m_openUriAction;
    }

  }

  /**
   * Execution hint to mark internal cleanup jobs.
   */
  public static final String RESOURCE_CLEANUP_JOB_MARKER = DownloadHandlerStorage.class.getName();

  private final Map<String, BinaryResourceHolderWithAction> m_valueMap = new HashMap<>();
  private final Map<String, IFuture> m_futureMap = new HashMap<>();

  protected Map<String, BinaryResourceHolderWithAction> valueMap() {
    return m_valueMap;
  }

  protected Map<String, IFuture> futureMap() {
    return m_futureMap;
  }

  /**
   * @return time to live in milliseconds
   */
  protected long getTTLForResource(BinaryResource res) {
    return TimeUnit.MINUTES.toMillis(1);
  }

  /**
   * Because certain OS/Download-Managers (for example the "Stock Browser" on Android) send two requests to actually
   * start the download of a file, we introduce a timeout before the removal of the downloadable resource in order to
   * allow multiple requests before the actual download.
   *
   * @return removal timeout after first request (in milliseconds)
   */
  protected long getRemovalTimeoutAfterFirstRequest() {
    return TimeUnit.SECONDS.toMillis(5);
  }

  /**
   * Put a downloadable item in the storage. After the TTL has passed the item is removed automatically.
   */
  public void put(String key, BinaryResourceHolder holder, IOpenUriAction opeUriAction) {
    long ttl = getTTLForResource(holder.get());
    synchronized (m_valueMap) {
      m_valueMap.put(key, new BinaryResourceHolderWithAction(holder, opeUriAction));
      scheduleRemoval(key, ttl);
    }
  }

  /**
   * @param key
   *          key to remove after TTL has expired
   * @param ttl
   *          time to live in milliseconds
   */
  protected void scheduleRemoval(final String key, long ttl) {
    final IFuture oldFuture = m_futureMap.put(key, Jobs.schedule(() -> removeOnTimeout(key), Jobs.newInput()
        .withExecutionHint(RESOURCE_CLEANUP_JOB_MARKER)
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(ttl, TimeUnit.MILLISECONDS))));

    if (oldFuture != null && !oldFuture.isCancelled()) {
      oldFuture.cancel(false);
    }
  }

  protected void removeOnTimeout(String key) {
    synchronized (m_valueMap) {
      m_valueMap.remove(key);
      IFuture future = m_futureMap.remove(key);
      if (future != null && !future.isCancelled()) {
        future.cancel(false);
      }
    }
  }

  /**
   * Retrieve a downloadable item from the storage.
   * <p>
   * Items with {@link OpenUriAction#DOWNLOAD} are automatically removed after a certain TTL
   * ({@link #getRemovalTimeoutAfterFirstRequest()}).
   */
  public BinaryResourceHolderWithAction get(String key) {
    BinaryResourceHolderWithAction resHolderWithAction;
    synchronized (m_valueMap) {
      resHolderWithAction = m_valueMap.get(key);
    }
    if (resHolderWithAction != null) {
      if (resHolderWithAction.getOpenUriAction() == OpenUriAction.DOWNLOAD) {
        scheduleRemoval(key, getRemovalTimeoutAfterFirstRequest());
      }
      else {
        if (!containsDownloadInterceptor(resHolderWithAction.getHolder())) {
          BinaryResourceHolder holder = new BinaryResourceHolder(resHolderWithAction.getHolder().get());
          holder.addHttpResponseInterceptor(new DownloadHttpResponseInterceptor(holder.get().getFilename()));
          put(key, holder, resHolderWithAction.getOpenUriAction());
        }
      }
    }
    return resHolderWithAction;

  }

  protected boolean containsDownloadInterceptor(BinaryResourceHolder holder) {
    for (IHttpResponseInterceptor interceptor : holder.getHttpResponseInterceptors()) {
      if (interceptor instanceof DownloadHttpResponseInterceptor) {
        return true;
      }
    }
    return false;
  }
}
