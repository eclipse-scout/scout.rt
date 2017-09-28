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
package org.eclipse.scout.rt.shared.services.lookup;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache used to optimize performance on {@link BatchLookupCall}s with {@link IBatchLookupService}.
 * <p>
 * Cache should only be used per operation, do not use it as a class member.
 */
@SuppressWarnings("unchecked")
public class BatchLookupResultCache {
  private static final Logger LOG = LoggerFactory.getLogger(BatchLookupResultCache.class);
  private static final Object globalCacheableLock = new Object();
  private static final Map<Class<? extends ILookupCall>, Boolean> globalCacheable = new HashMap<>();

  private final Map<ILookupCall, List<ILookupRow<?>>> m_cache = new HashMap<>();

  /**
   * reset the result cache
   */
  public void reset() {
    m_cache.clear();
  }

  /**
   * @return the same as {@link LookupCall#getDataByKey()} but use the cache to lookup already fetched results
   */
  public List<ILookupRow<?>> getDataByKey(ILookupCall call) {
    if (call == null || call.getKey() == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<ILookupRow<?>> result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByKey();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return the same as {@link LookupCall#getDataByText()} but use the cache to lookup already fetched results
   */
  public List<ILookupRow<?>> getDataByText(ILookupCall call) {
    List<ILookupRow<?>> result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByText();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return the same as {@link LookupCall#getDataByAll()} but use the cache to lookup already fetched results
   */
  public List<ILookupRow<?>> getDataByAll(ILookupCall call) {
    List<ILookupRow<?>> result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByAll();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return the same as {@link LookupCall#getDataByRec()} but use the cache to lookup already fetched results
   */
  public List<ILookupRow<?>> getDataByRec(ILookupCall call) {
    List<ILookupRow<?>> result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByRec();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return a previous result based on {@link LookupCall#equals(Object)}
   */
  public List<ILookupRow<?>> getCachedResult(ILookupCall call) {
    if (call == null || !isCacheable(call.getClass())) {
      return null;
    }
    List<ILookupRow<?>> list = m_cache.get(call);
    if (list == null) {
      return null;
    }
    return CollectionUtility.arrayList(list);
  }

  /**
   * put a result and associate it with {@link LookupCall#equals(Object)}
   */
  public void putCachedResult(ILookupCall call, List<ILookupRow<?>> result) {
    if (call == null || result == null || !isCacheable(call.getClass())) {
      return;
    }
    m_cache.put(call, result);
  }

  /**
   * checks if the {@link LookupCall} class overrides the equals method and remembers the decision
   */
  public static boolean isCacheable(Class<? extends ILookupCall> clazz) {
    if (clazz == null) {
      return false;
    }
    synchronized (globalCacheableLock) {
      Boolean b = globalCacheable.computeIfAbsent(clazz, k -> verifyLookupCallBeanQuality(clazz));
      return b.booleanValue();
    }
  }

  /**
   * In order to use caching of results on local lookup calls, it is crucial that the javabean concepts are valid,
   * especially hashCode and equals.
   * <p>
   * Scout tries to help developers to find problems related to this issue and write a warning in development mode on
   * all local lookup call subclasses that do not overwrite hashCode and equals and contain additional members.
   */
  private static boolean verifyLookupCallBeanQuality(Class<? extends ILookupCall> clazz) {
    if (clazz == LocalLookupCall.class) {
      return true;
    }
    if (clazz == LookupCall.class) {
      return true;
    }
    Class<?> t = clazz;
    while (t != null && t != LookupCall.class) {
      //check for fields
      for (Field f : t.getDeclaredFields()) {
        if (f.isSynthetic() || f.isEnumConstant()) {
          continue;
        }
        if ((f.getModifiers() & (Modifier.STATIC)) != 0) {
          continue;
        }
        try {
          t.getDeclaredMethod("equals", Object.class);
          //found
        }
        catch (Exception ex) { // NOSONAR
          //not found
          LOG.warn("{} subclasses LookupCall with an additional member field '{}.{}' and should therefore override the 'boolean equals(Object obj)' and 'int hashCode()' methods",
              clazz, t.getSimpleName(), f.getName());
          return false;
        }
      }
      t = t.getSuperclass();
    }
    return true;
  }

}
