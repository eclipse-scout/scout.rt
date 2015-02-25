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
package org.eclipse.scout.commons.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An object that stores job specific context properties.
 * <p>
 * <strong>This class is not thread-safe.</strong>
 *
 * @since 3.8.2
 */
public class JobContext implements Iterable<Entry<Object, Object>> {

  /**
   * {@link JobContext} associated with the current thread.
   */
  public static final ThreadLocal<JobContext> CURRENT = new ThreadLocal<>();

  private static final int DEFAULT_INITIAL_CAPACITY = 5;

  private Map<Object, Object> m_properties;

  public JobContext() {
  }

  /**
   * Creates a flat copy of the given {@link JobContext}.
   *
   * @param context
   *          {@link JobContext} to be copied; if <code>null</code>, an empty {@link JobContext} is created.
   * @return copy of the given {@link JobContext}.
   */
  public static JobContext copy(final JobContext context) {
    final JobContext copy = new JobContext();
    if (context != null && context.m_properties != null) {
      copy.m_properties = new HashMap<>(context.m_properties);
    }
    return copy;
  }

  public Object get(final Object key) {
    if (m_properties == null || key == null) {
      return null;
    }
    return m_properties.get(key);
  }

  public void set(final Object key, final Object value) {
    if (value == null) {
      if (m_properties == null) {
        return;
      }
      m_properties.remove(key);
      if (m_properties.isEmpty()) {
        m_properties = null;
      }
    }
    else {
      if (m_properties == null) {
        m_properties = new HashMap<>(DEFAULT_INITIAL_CAPACITY);
      }
      m_properties.put(key, value);
    }
  }

  public void clear() {
    m_properties = null;
  }

  @Override
  public Iterator<Entry<Object, Object>> iterator() {
    final Map<Object, Object> map = (m_properties == null ? Collections.emptyMap() : m_properties);
    return map.entrySet().iterator();
  }
}
