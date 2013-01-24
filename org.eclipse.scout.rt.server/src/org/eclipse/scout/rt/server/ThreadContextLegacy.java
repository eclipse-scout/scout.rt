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
package org.eclipse.scout.rt.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * This legacy utility will be removed in the next release.
 * 
 * @deprecated
 */
@Deprecated
public final class ThreadContextLegacy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ThreadContextLegacy.class);

  private ThreadContextLegacy() {
  }

  private static final Object MAP_LOCK;
  private static final WeakHashMap<Thread, HashMap<Class, Object>> MAP;

  static {
    MAP_LOCK = new Object();
    MAP = new WeakHashMap<Thread, HashMap<Class, Object>>();
  }

  private static HashMap<Class, Object> getThreadMap(boolean autoCreate) {
    synchronized (MAP_LOCK) {
      HashMap<Class, Object> threadMap = MAP.get(Thread.currentThread());
      if (threadMap == null && autoCreate) {
        threadMap = new HashMap<Class, Object>();
        MAP.put(Thread.currentThread(), threadMap);
      }
      return threadMap;
    }
  }

  public static <T> T getCustomValue(Class<T> key) {
    return getInternal(key);
  }

  public static <T> T get(Class<T> key) {
    return getInternal(key);
  }

  @SuppressWarnings("unchecked")
  private static <T> T getInternal(Class<T> key) {
    if (key == null) {
      return null;
    }
    if (Locale.class.isAssignableFrom(key)) {
      return (T) LocaleThreadLocal.get();
    }
    if (HttpServletRequest.class.isAssignableFrom(key)) {
      return (T) ThreadContext.getHttpServletRequest();
    }
    if (HttpServletResponse.class.isAssignableFrom(key)) {
      return (T) ThreadContext.getHttpServletResponse();
    }
    if (IServerSession.class.isAssignableFrom(key)) {
      return (T) ThreadContext.getServerSession();
    }
    if (ITransaction.class.isAssignableFrom(key)) {
      return (T) ThreadContext.getTransaction();
    }
    //
    HashMap<Class, Object> threadMap = getThreadMap(false);
    if (threadMap != null) {
      return (T) threadMap.get(key);
    }
    return null;
  }

  public static Map<Class, Object> backup() {
    HashMap<Class, Object> copyMap = new HashMap<Class, Object>();
    Map<Class, Object> rawMap = getThreadMap(false);
    if (rawMap != null) {
      copyMap.putAll(rawMap);
    }
    copyMap.put(HttpServletRequest.class, ThreadContext.getHttpServletRequest());
    copyMap.put(HttpServletResponse.class, ThreadContext.getHttpServletResponse());
    copyMap.put(IServerSession.class, ThreadContext.getServerSession());
    copyMap.put(ITransaction.class, ThreadContext.getTransaction());
    return copyMap;
  }

  public static void restore(Map<Class, Object> map) {
    HashMap<Class, Object> copyMap = (map != null ? new HashMap<Class, Object>(map) : new HashMap<Class, Object>());
    ThreadContext.putHttpServletRequest((HttpServletRequest) copyMap.remove(HttpServletRequest.class));
    ThreadContext.putHttpServletResponse((HttpServletResponse) copyMap.remove(HttpServletResponse.class));
    ThreadContext.putServerSession((IServerSession) copyMap.remove(IServerSession.class));
    ThreadContext.putTransaction((ITransaction) copyMap.remove(ITransaction.class));
    synchronized (MAP_LOCK) {
      MAP.put(Thread.currentThread(), copyMap);
    }
  }

  /**
   * store the value in the current thread context (with all interfaces and
   * super classes of value as keys)
   */
  public static <T> void putCustomValue(T value) {
    putInternal(value);
  }

  public static <T> void put(T value) {
    putInternal(value);
  }

  private static <T> void putInternal(T value) {
    if (value == null) {
      return;
    }
    if (value instanceof Locale) {
      LocaleThreadLocal.set((Locale) value);
      return;
    }
    if (value instanceof HttpServletRequest) {
      ThreadContext.putHttpServletRequest((HttpServletRequest) value);
      return;
    }
    if (value instanceof HttpServletResponse) {
      ThreadContext.putHttpServletResponse((HttpServletResponse) value);
      return;
    }
    if (value instanceof IServerSession) {
      ThreadContext.putServerSession((IServerSession) value);
      return;
    }
    if (value instanceof ITransaction) {
      ThreadContext.putTransaction((ITransaction) value);
      return;
    }
    HashMap<Class, Object> threadMap = getThreadMap(true);
    HashSet<Class> keys = new HashSet<Class>();
    enumKeys(value.getClass(), keys);
    for (Class key : keys) {
      threadMap.put(key, value);
    }
  }

  public static <T> void clear(T value) {
    clearInternal(value);
  }

  public static <T> void clearInternal(T value) {
    if (value == null) {
      return;
    }
    HashMap<Class, Object> threadMap = getThreadMap(false);
    if (threadMap != null) {
      HashSet<Class> keys = new HashSet<Class>();
      enumKeys(value.getClass(), keys);
      for (Class key : keys) {
        threadMap.remove(key);
      }
    }
  }

  private static void enumKeys(Class c, HashSet<Class> keys) {
    if (c != null) {
      keys.add(c);
      for (Class i : c.getInterfaces()) {
        enumKeys(i, keys);
      }
      enumKeys(c.getSuperclass(), keys);
    }
  }
}
