/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.cache;

/**
 * Listener interface to be notified when {@link ICache#invalidate(ICacheEntryFilter, boolean)} has been called.
 */
public interface ICacheInvalidationListener<K, V> {

  /**
   * Called when {@link ICache#invalidate(ICacheEntryFilter, boolean)} has been called.
   *
   * @param filter
   *          The {@link ICacheEntryFilter} which was used in the invalidation. May be {@code null} (which means nothing
   *          was invalidated).
   * @param propagate
   *          {@code true} indicates that the event occurred on this node and will be forwarded to other cluster nodes,
   *          {@code false} indicates that the event occurred on another node and was propagated to the current node
   *          through the cluster.
   */
  void invalidated(ICacheEntryFilter<K, V> filter, boolean propagate);
}
