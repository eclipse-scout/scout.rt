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
package org.eclipse.scout.rt.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An object that stores client job specific context properties.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * </p>
 * 
 * @since 3.8.2
 */
public class ClientJobContext implements Iterable<Entry<Object, Object>> {

  private Map<Object, Object> m_properties;

  public ClientJobContext() {
  }

  public ClientJobContext(ClientJobContext properties) {
    if (properties != null && properties.m_properties != null) {
      m_properties = new HashMap<Object, Object>(properties.m_properties);
    }
  }

  public Object get(Object key) {
    if (m_properties == null || key == null) {
      return null;
    }
    return m_properties.get(key);
  }

  public void set(Object key, Object value) {
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
        m_properties = new HashMap<Object, Object>(5);
      }
      m_properties.put(key, value);
    }
  }

  public void clear() {
    if (m_properties == null) {
      return;
    }
    m_properties = null;
  }

  @Override
  public Iterator<Entry<Object, Object>> iterator() {
    if (m_properties == null) {
      return Collections.emptyMap().entrySet().iterator();
    }
    return m_properties.entrySet().iterator();
  }
}
