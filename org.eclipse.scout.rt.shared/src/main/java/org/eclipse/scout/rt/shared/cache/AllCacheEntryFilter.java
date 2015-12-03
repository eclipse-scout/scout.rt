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

/**
 * Special filter that accepts all entries
 * <p>
 * This class is immutable.
 *
 * @since 5.2
 */
public final class AllCacheEntryFilter<K, V> implements ICacheEntryFilter<K, V> {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean accept(K key, V value) {
    return true;
  }

  @Override
  public ICacheEntryFilter<K, V> coalesce(ICacheEntryFilter<K, V> other) {
    return this;
  }
}
