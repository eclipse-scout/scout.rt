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
package org.eclipse.scout.rt.shared.services.lookup;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Cache used to optimize performance on {@link BatchLookupCall}s with {@link IBatchLookupService}.
 * <p>
 * Cache should only be used per operation, do not use it as a class member.
 */
public class BatchLookupResultCache {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BatchLookupResultCache.class);
  private static final Object globalCacheableLock = new Object();
  private static final HashMap<Class<? extends LookupCall>, Boolean> globalCacheable = new HashMap<Class<? extends LookupCall>, Boolean>();

  private HashMap<LookupCall, LookupRow[]> m_cache = new HashMap<LookupCall, LookupRow[]>();

  public BatchLookupResultCache() {
  }

  /**
   * reset the result cache
   */
  public void reset() {
    m_cache.clear();
  }

  /**
   * @return the same as {@link LookupCall#getDataByKey()} but use the cache to lookup already fetched results
   */
  public LookupRow[] getDataByKey(LookupCall call) throws ProcessingException {
    if (call == null || call.getKey() == null) {
      return LookupRow.EMPTY_ARRAY;
    }
    LookupRow[] result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByKey();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return the same as {@link LookupCall#getDataByText()} but use the cache to lookup already fetched results
   */
  public LookupRow[] getDataByText(LookupCall call) throws ProcessingException {
    LookupRow[] result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByText();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return the same as {@link LookupCall#getDataByAll()} but use the cache to lookup already fetched results
   */
  public LookupRow[] getDataByAll(LookupCall call) throws ProcessingException {
    LookupRow[] result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByAll();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return the same as {@link LookupCall#getDataByRec()} but use the cache to lookup already fetched results
   */
  public LookupRow[] getDataByRec(LookupCall call) throws ProcessingException {
    LookupRow[] result = getCachedResult(call);
    if (result == null) {
      result = call.getDataByRec();
      putCachedResult(call, result);
    }
    return result;
  }

  /**
   * @return a previous result based on {@link LookupCall#equals(Object)}
   */
  public LookupRow[] getCachedResult(LookupCall call) {
    if (call == null || !isCacheable(call.getClass())) {
      return null;
    }
    return m_cache.get(call);
  }

  /**
   * put a result and associate it with {@link LookupCall#equals(Object)}
   */
  public void putCachedResult(LookupCall call, LookupRow[] result) {
    if (call == null || result == null || !isCacheable(call.getClass())) {
      return;
    }
    m_cache.put(call, result);
  }

  /**
   * checks if the {@link LookupCall} class overrides the equals method and remembers the decision
   */
  public static boolean isCacheable(Class<? extends LookupCall> clazz) {
    if (clazz == null) {
      return false;
    }
    synchronized (globalCacheableLock) {
      Boolean b = globalCacheable.get(clazz);
      if (b == null) {
        b = verifyLookupCallBeanQuality(clazz);
        globalCacheable.put(clazz, b);
      }
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
  private static boolean verifyLookupCallBeanQuality(Class<? extends LookupCall> clazz) {
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
        catch (Throwable ex) {
          //not found
          LOG.warn("" + clazz + " subclasses LookupCall with an additional member field '" + t.getSimpleName() + "." + f.getName() + "' and should therefore override the 'boolean equals(Object obj)' and 'int hashCode()' methods");
          return false;
        }
      }
      t = t.getSuperclass();
    }
    return true;
  }

}
