/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;

/**
 * Context object used to carry properties for {@link ScoutDataObjectModule} and its components (e.g. serializers and
 * deserializers).
 */
@Bean
public class ScoutDataObjectModuleContext {

  protected static final String DATA_OBJECT_MAPPER_CLASS_KEY = "dataObjectMapperClassKey";

  protected static final String TYPE_ATTRIBUTE_NAME_KEY = "typeAttributeNameKey";

  protected static final String TYPE_VERSION_ATTRIBUTE_NAME_KEY = "typeVersionAttributeNameKey";

  protected static final String IGNORE_TYPE_ATTRIBUTE_KEY = "ignoreTypeAttributeKey";

  protected static final String CONTRIBUTIONS_ATTRIBUTE_NAME_KEY = "contributionsAttributeNameKey";

  protected LazyValue<DoEntitySerializerAttributeNameComparator> m_comparator = new LazyValue<>(() -> BEANS.get(DoEntitySerializerAttributeNameComparator.class).init(this));

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

  public boolean belongsTo(Class<? extends IDataObjectMapper> dataObjectMapperClass) {
    Class<? extends IDataObjectMapper> actualDataObjectMapperClass = getDataObjectMapperClass();
    return actualDataObjectMapperClass != null && dataObjectMapperClass.isAssignableFrom(actualDataObjectMapperClass);
  }

  @SuppressWarnings("unchecked")
  public Class<? extends IDataObjectMapper> getDataObjectMapperClass() {
    return (Class<? extends IDataObjectMapper>) get(DATA_OBJECT_MAPPER_CLASS_KEY, Class.class);
  }

  public ScoutDataObjectModuleContext withDataObjectMapperClass(Class<? extends IDataObjectMapper> dataObjectMapperClass) {
    put(DATA_OBJECT_MAPPER_CLASS_KEY, dataObjectMapperClass);
    return this;
  }

  public DoEntitySerializerAttributeNameComparator getComparator() {
    return m_comparator.get();
  }

  public String getTypeAttributeName() {
    return get(TYPE_ATTRIBUTE_NAME_KEY, String.class);
  }

  public ScoutDataObjectModuleContext withTypeAttributeName(String typeAttributeName) {
    put(TYPE_ATTRIBUTE_NAME_KEY, typeAttributeName);
    return this;
  }

  public String getTypeVersionAttributeName() {
    return get(TYPE_VERSION_ATTRIBUTE_NAME_KEY, String.class);
  }

  public ScoutDataObjectModuleContext withTypeVersionAttributeName(String typeVersionAttributeName) {
    put(TYPE_VERSION_ATTRIBUTE_NAME_KEY, typeVersionAttributeName);
    return this;
  }

  public boolean isIgnoreTypeAttribute() {
    return BooleanUtility.nvl(get(IGNORE_TYPE_ATTRIBUTE_KEY, Boolean.class));
  }

  public ScoutDataObjectModuleContext withIgnoreTypeAttribute(boolean ignoreTypeAttribute) {
    put(IGNORE_TYPE_ATTRIBUTE_KEY, ignoreTypeAttribute);
    return this;
  }

  public String getContributionsAttributeName() {
    return get(CONTRIBUTIONS_ATTRIBUTE_NAME_KEY, String.class);
  }

  public ScoutDataObjectModuleContext withContributionsAttributeName(String contributionsAttributeName) {
    put(CONTRIBUTIONS_ATTRIBUTE_NAME_KEY, contributionsAttributeName);
    return this;
  }
}
