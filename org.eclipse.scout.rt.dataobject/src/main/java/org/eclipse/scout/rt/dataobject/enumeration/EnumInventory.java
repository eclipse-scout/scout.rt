/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.enumeration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inventory (and cache) for all {@link IEnum} with a {@link EnumName} annotation.
 */
@ApplicationScoped
public class EnumInventory {

  private static final Logger LOG = LoggerFactory.getLogger(EnumInventory.class);

  /**
   * <b>NOTE:</b> These from/to class maps contains the static, compile-time "class" to "enum name" mapping as defined
   * by the Jandex index.
   */
  private final Map<String, Class<? extends IEnum>> m_enumNameToClassMap = new HashMap<>();
  private final Map<Class<? extends IEnum>, String> m_classToEnumName = new HashMap<>();

  @PostConstruct
  protected void init() {
    ClassInventory.get()
        .getKnownAnnotatedTypes(EnumName.class)
        .stream()
        .map(IClassInfo::resolveClass)
        .forEach(this::registerClass);
    LOG.info("Registry initialized, found {} {} implementations with @{} annotation.", m_enumNameToClassMap.size(), IEnum.class.getSimpleName(), EnumName.class.getSimpleName());
  }

  /**
   * @return Enum name for specified class {@code queryClazz} or <code>null</code> if none is found.
   */
  public String toEnumName(Class<?> queryClazz) {
    return m_classToEnumName.get(queryClazz);
  }

  /**
   * Returns the correct class for specified {@code enumName}.
   *
   * @return Class for specified {@code enumName}, if class is uniquely resolvable, else {@code null}
   */
  public Class<? extends IEnum> fromEnumName(String enumName) {
    return m_enumNameToClassMap.get(enumName);
  }

  /**
   * @return Map with all enum name to {@link IEnum} class mappings
   */
  public Map<String, Class<? extends IEnum>> getEnumNameToClassMap() {
    return Collections.unmodifiableMap(m_enumNameToClassMap);
  }

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  /**
   * Adds {@code clazz} to registry.
   */
  protected void registerClass(Class<?> clazz) {
    if (IEnum.class.isAssignableFrom(clazz)) {
      Class<? extends IEnum> entityClass = clazz.asSubclass(IEnum.class);
      String name = resolveEnumName(clazz);
      if (StringUtility.hasText(name)) {
        String registeredName = m_classToEnumName.put(entityClass, name);
        Class<? extends IEnum> registeredClass = m_enumNameToClassMap.put(name, entityClass);
        checkDuplicateClassMapping(clazz, name, registeredName, registeredClass);
        LOG.debug("Registered class {} with enum name '{}'", entityClass, name);
      }
      else {
        LOG.warn("Class {} is annotated with @{} with an empty enum name value, skip registration", clazz.getName(), EnumName.class.getSimpleName());
      }
    }
    else {
      LOG.warn("Class {} is annotated with @{} but is not an instance of {}, skip registration", clazz.getName(), EnumName.class.getSimpleName(), IEnum.class);
    }
  }

  /**
   * Checks for {@link IEnum} classes with duplicated {@link EnumName} annotation values.
   */
  protected void checkDuplicateClassMapping(Class<?> clazz, String name, String existingName, Class<? extends IEnum> existingClass) {
    Assertions.assertNull(existingClass, "{} and {} have the same type '{}', use an unique @{} annotation value.", clazz, existingClass, name, EnumName.class.getSimpleName());
    Assertions.assertNull(existingName, "{} was already registered with enum name {}, register each class only once.", clazz, existingName, EnumName.class.getSimpleName());
  }

  protected String resolveEnumName(Class<?> c) {
    EnumName enumNameAnn = c.getAnnotation(EnumName.class);
    return enumNameAnn == null ? null : enumNameAnn.value();
  }
}
