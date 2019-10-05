/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.security;

import java.io.Serializable;
import java.security.PermissionCollection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.util.Assertions;

public abstract class AbstractPermissionCollection extends PermissionCollection implements IPermissionCollection {
  private static final long serialVersionUID = 1L;

  private final Map<Class<?>, Object> m_values;

  public AbstractPermissionCollection() {
    m_values = new HashMap<Class<?>, Object>();
  }

  protected void assertNotReadOnly() {
    Assertions.assertFalse(isReadOnly(), "PermissionCollection is read-only");
  }

  @Override
  public <T extends Serializable> T getValue(Class<T> valueType) {
    return valueType.cast(m_values.get(valueType));
  }

  @Override
  public Stream<Object> getValues() {
    return m_values.values().stream();
  }

  @Override
  public <T extends Serializable> void setValue(Class<T> valueType, T value) {
    assertNotReadOnly();
    Assertions.assertNotNull(valueType);
    if (value == null) {
      m_values.remove(valueType);
    }
    else {
      m_values.put(valueType, value);
    }
  }
}
