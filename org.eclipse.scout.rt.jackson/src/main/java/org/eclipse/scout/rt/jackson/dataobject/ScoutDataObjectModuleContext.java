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

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;

/**
 * Context object used to carry properties for {@link ScoutDataObjectModule} and its components (e.g. serializers and
 * deserializers).
 */
@Bean
public class ScoutDataObjectModuleContext {

  protected static final String TYPE_ATTRIBUTE_NAME_KEY = "typeAttributeNameKey";
  
  protected static final String IGNORE_TYPE_ATTRIBUTE_KEY = "ignoreTypeAttributeKey";

  protected final Map<String, Object> m_contextMap = new HashMap<>();

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

  public ScoutDataObjectModuleContext withTypeAttributeName(String typeAttributeName) {
    put(TYPE_ATTRIBUTE_NAME_KEY, typeAttributeName);
    return this;
  }

  public boolean isIgnoreTypeAttribute() {
    return BooleanUtility.nvl(get(IGNORE_TYPE_ATTRIBUTE_KEY, Boolean.class));
  }

  public ScoutDataObjectModuleContext withIgnoreTypeAttribute(boolean ignoreTypeAttribute) {
    put(IGNORE_TYPE_ATTRIBUTE_KEY, ignoreTypeAttribute);
    return this;
  }

  /**
   * TODO [9.1] pbz: remove method
   *
   * @deprecated Use {@link #withTypeAttributeName(String)} instead.
   */
  @Deprecated
  public void setTypeAttributeName(String typeAttributeName) {
    withTypeAttributeName(typeAttributeName);
  }
}
