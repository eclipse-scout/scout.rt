/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Will be replaced with a default method implementation with java 1.8
 *
 * @since 5.2
 */
public abstract class AbstractCacheValueResolver<K, V> implements ICacheValueResolver<K, V> {

  @Override
  public Map<K, V> resolveAll(Set<K> keys) {
    Map<K, V> result = new HashMap<>();
    for (K key : keys) {
      result.put(key, resolve(key));
    }
    return result;
  }
}
