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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
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

  protected static final String SUPPRESS_TYPE_ATTRIBUTE_KEY = "suppressTypeAttributeKey";

  protected static final String CONTRIBUTIONS_ATTRIBUTE_NAME_KEY = "contributionsAttributeNameKey";

  protected static final String LENIENT_MODE_KEY = "lenientModeKey";

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

  /**
   * @see #withIgnoreTypeAttribute(boolean)
   */
  public boolean isIgnoreTypeAttribute() {
    return BooleanUtility.nvl(get(IGNORE_TYPE_ATTRIBUTE_KEY, Boolean.class));
  }

  /**
   * Flag to ignore type attributes when deserializing a JSON document structure. Forces to create raw {@link DoEntity}
   * instances for each deserialized JSON object instead.
   */
  public ScoutDataObjectModuleContext withIgnoreTypeAttribute(boolean ignoreTypeAttribute) {
    put(IGNORE_TYPE_ATTRIBUTE_KEY, ignoreTypeAttribute);
    return this;
  }

  /**
   * @see #withSuppressTypeAttribute(boolean)
   */
  public boolean isSuppressTypeAttribute() {
    return BooleanUtility.nvl(get(SUPPRESS_TYPE_ATTRIBUTE_KEY, Boolean.class));
  }

  /**
   * Flag to suppress writing type attributes (e.g. '_type' attribute in JSON document) when serializing a data object
   * with {@link TypeName} annotation into a JSON document.
   * <p>
   * <b>NOTE:</b> A JSON document written without type information may not be deserialized correctly if any polymorphic
   * types are used within the data object structure, e.g. a {@link DoList} typed with {@link IDoEntity} containing
   * different data object subclasses.
   */
  public ScoutDataObjectModuleContext withSuppressTypeAttribute(boolean suppressTypeAttribute) {
    put(SUPPRESS_TYPE_ATTRIBUTE_KEY, suppressTypeAttribute);
    return this;
  }

  public String getContributionsAttributeName() {
    return get(CONTRIBUTIONS_ATTRIBUTE_NAME_KEY, String.class);
  }

  public ScoutDataObjectModuleContext withContributionsAttributeName(String contributionsAttributeName) {
    put(CONTRIBUTIONS_ATTRIBUTE_NAME_KEY, contributionsAttributeName);
    return this;
  }

  /**
   * @return <code>true</code> if lenient serialization/deserialization should be used. Non-lenient
   *         serialization/deserialization will fail if types don't match the expected ones according to the given
   *         structure.
   */
  public boolean isLenientMode() {
    return BooleanUtility.nvl(get(LENIENT_MODE_KEY, Boolean.class));
  }

  public ScoutDataObjectModuleContext withLenientMode(boolean lenientMode) {
    put(LENIENT_MODE_KEY, true);
    return this;
  }
}
