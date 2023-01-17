/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.server.notification.ICoalescer;

/**
 * {@link ICoalescer} for {@link InvalidateCacheNotification}
 *
 * @since 5.2
 */
public class InvalidateCacheNotificationCoalescer implements ICoalescer<InvalidateCacheNotification> {

  @Override
  public List<InvalidateCacheNotification> coalesce(List<InvalidateCacheNotification> notifications) {
    List<InvalidateCacheNotification> result = new ArrayList<>();
    if (notifications.isEmpty()) {
      return result;
    }
    Map<String, List<ICacheEntryFilter<?, ?>>> filterMap = new HashMap<>();
    for (InvalidateCacheNotification notification : notifications) {
      List<ICacheEntryFilter<?, ?>> list = filterMap.get(notification.getCacheId());
      if (list == null) {
        list = new ArrayList<>();
        list.add(notification.getFilter());
      }
      else {
        coalesceFilters(list, notification.getFilter());
      }
      filterMap.put(notification.getCacheId(), list);
    }

    for (Entry<String, List<ICacheEntryFilter<?, ?>>> entry : filterMap.entrySet()) {
      for (ICacheEntryFilter<?, ?> filter : entry.getValue()) {
        result.add(new InvalidateCacheNotification(entry.getKey(), filter));
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  protected void coalesceFilters(List<ICacheEntryFilter<?, ?>> list, ICacheEntryFilter filter) {
    Iterator<ICacheEntryFilter<?, ?>> iterator = list.iterator();
    while (iterator.hasNext()) {
      ICacheEntryFilter<?, ?> otherFilter = iterator.next();
      ICacheEntryFilter<?, ?> newFilter = filter.coalesce(otherFilter);
      newFilter = newFilter != null ? newFilter : otherFilter.coalesce(filter);
      if (newFilter != null) {
        // coalesce worked, remove other filter from list and reset iterator
        iterator.remove();
        iterator = list.iterator();
        filter = newFilter;
      }
    }
    list.add(filter);
  }
}
