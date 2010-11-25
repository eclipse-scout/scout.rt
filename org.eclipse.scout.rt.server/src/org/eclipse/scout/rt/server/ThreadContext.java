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
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * This singleton is a container for objects associated with the
 * current Thread These objects are not accessible outside the Thread
 * This eliminates the need of creating special event dispatching
 * threads to run job queues, handle Thread-based session values etc.
 */

public final class ThreadContext {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ThreadContext.class);

  private ThreadContext() {
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

  /**
   * @return the value of type <T> stored in the current thread context
   */
  public static <T extends HttpServletRequest> T get(Class<T> key) {
    return getInternal(key);
  }

  public static <T extends HttpServletResponse> T get(Class<T> key) {
    return getInternal(key);
  }

  public static <T extends IServerSession> T get(Class<T> key) {
    return getInternal(key);
  }

  public static <T extends ITransaction> T get(Class<T> key) {
    return getInternal(key);
  }

  public static <T> T getCustomValue(Class<T> key) {
    return getInternal(key);
  }

  @SuppressWarnings("unchecked")
  private static <T> T getInternal(Class<T> key) {
    if (key == Locale.class) {
      return (T) LocaleThreadLocal.get();
    }
    if (key == NlsLocale.class) {
      return (T) NlsLocale.getDefault();
    }
    //
    HashMap<Class, Object> threadMap = getThreadMap(false);
    if (threadMap != null) {
      return (T) threadMap.get(key);
    }
    else {
      return null;
    }
  }

  public static Map<Class, Object> backup() {
    Map<Class, Object> map = getThreadMap(false);
    if (map != null) {
      return new HashMap<Class, Object>(map);
    }
    else {
      return new HashMap<Class, Object>();
    }
  }

  public static void restore(Map<Class, Object> map) {
    synchronized (MAP_LOCK) {
      if (map != null) {
        MAP.put(Thread.currentThread(), new HashMap<Class, Object>(map));
      }
      else {
        MAP.remove(Thread.currentThread());
      }
    }
  }

  /**
   * store the value in the current thread context (with all interfaces and
   * super classes of value as keys)
   */
  public static <T extends HttpServletRequest> void put(T value) {
    putInternal(value);
  }

  public static <T extends HttpServletResponse> void put(T value) {
    putInternal(value);
  }

  public static <T extends IServerSession> void put(T value) {
    putInternal(value);
  }

  public static <T extends ITransaction> void put(T value) {
    putInternal(value);
  }

  public static <T> void putCustomValue(T value) {
    putInternal(value);
  }

  private static <T> void putInternal(T value) {
    if (value == null) return;
    if (value instanceof Locale) {
      LocaleThreadLocal.set((Locale) value);
    }
    if (value instanceof NlsLocale) {
      NlsLocale.setThreadDefault((NlsLocale) value);
    }
    //
    HashMap<Class, Object> threadMap = getThreadMap(true);
    HashSet<Class> keys = new HashSet<Class>();
    enumKeys(value.getClass(), keys);
    for (Class key : keys) {
      threadMap.put(key, value);
    }
  }

  /**
   * remove the value from the current thread context
   */
  public static <T> void clear(T value) {
    clearInternal(value);
  }

  public static <T> void clearInternal(T value) {
    if (value == null) return;
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
