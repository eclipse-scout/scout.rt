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
package org.eclipse.scout.rt.shared;

import java.util.TreeMap;

import org.eclipse.scout.commons.StringUtility;

/**
 * @since 11.06.2008
 */
public class ContextMap extends TreeMap<String, Object> {
  private static final long serialVersionUID = 1L;

  // Needed for get to be compatible with TreeMap<String, Long>.get
  public Long get(String key) {
    return (Long) super.get(key);
  }

  // Whenever we need an object not of type Long, we need to explicitly pass the
  // desired class
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> cls) {
    return (T) super.get(key);
  }

  public ContextMap copy() {
    ContextMap cm = new ContextMap();
    cm.putAll(this);
    return cm;
  }

  @Override
  public Object put(String key, Object value) {
    if (value == null) {
      return value;
    }
    else if (value instanceof String) {
      if (StringUtility.hasText((String) value)) {
        super.put(key, value);
      }
      else if (!super.containsKey(key)) {
        // put an empty String only, if the value had not been set yet
        super.put(key, value);
      }
    }
    else if (value instanceof Long) {
      if ((Long) value != 0L) {
        super.put(key, value);
      }
      else if (!super.containsKey(key)) {
        // put a numeric "0" only, if the value had not been set yet
        super.put(key, value);
      }
    }
    else {
      super.put(key, value);
    }
    return value;
  }

  public ContextMap copyPut(String key, Object value) {
    ContextMap cm = copy();
    cm.put(key, value);
    return cm;
  }
}
