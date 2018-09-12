/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Context object used to carry properties for {@link ScoutDataObjectModule} and its components (e.g. serializers and
 * deserializers).
 */
public class ScoutDataObjectModuleContext {

  private static final String TYPE_ATTRIBUTE_NAME_KEY = "typeAttributeNameKey";

  private final Map<String, Object> m_contextMap = new HashMap<>();

  public void put(String key, Object value) {
    m_contextMap.put(key, value);
  }

  public Object get(String key) {
    return m_contextMap.get(key);
  }

  public <T> T get(String key, Class<T> clazz) {
    return Assertions.assertType(get(key), clazz);
  }

  /* **************************************************************************
   * NAMED PROPERTIES
   * *************************************************************************/

  public String getTypeAttributeName() {
    return get(TYPE_ATTRIBUTE_NAME_KEY, String.class);
  }

  public void setTypeAttributeName(String typeAttributeName) {
    put(TYPE_ATTRIBUTE_NAME_KEY, typeAttributeName);
  }
}
