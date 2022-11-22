/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@CreateImmediately // validate id types on startup
public class IdInventory {

  private static final Logger LOG = LoggerFactory.getLogger(IdInventory.class);

  protected final Map<String, Class<? extends IId>> m_nameToClassMap = new HashMap<>();
  protected final Map<Class<? extends IId>, String> m_classToNameMap = new HashMap<>();

  @PostConstruct
  protected void createClassCache() {
    for (IClassInfo classInfo : ClassInventory.get().getKnownAnnotatedTypes(IdTypeName.class)) {
      String typeName = (String) classInfo.getAnnotationValue(IdTypeName.class, "value");
      assertNotNullOrEmpty(typeName, "Invalid value for @{} on {} (must not be null or empty)", IdTypeName.class.getSimpleName(), classInfo.resolveClass().getName());
      try {
        Class<? extends IId> idClass = classInfo.resolveClass().asSubclass(IId.class);
        registerIdTypeName(typeName, idClass);
      }
      catch (@SuppressWarnings("squid:S1166") ClassCastException e) {
        LOG.warn("Class {} is annotated with @{} but does not implement {}. Skipping class.", classInfo.resolveClass().getName(), IdTypeName.class.getSimpleName(), IId.class.getName());
      }
    }
    LOG.debug("Registered {} id types", m_nameToClassMap.size());
  }

  /**
   * Register mapping between id class and its id-typename.
   * <p>
   * Note: The access to the type mapping data structure is not synchronized and therefore not thread safe. Use this
   * method to set up the {@link IdInventory} instance directly after platform start and not to change the
   * {@link IdInventory} behavior dynamically at runtime.
   */
  public void registerIdTypeName(String typeName, Class<? extends IId> idClass) {
    Class<? extends IId> registeredIdClass = m_nameToClassMap.put(typeName, idClass);
    String registeredTypeName = m_classToNameMap.put(idClass, typeName);
    checkDuplicateIdTypeNames(idClass, typeName, registeredIdClass, registeredTypeName);
  }

  /**
   * Checks for classes with the same {@link IdTypeName} annotation values.
   */
  protected void checkDuplicateIdTypeNames(Class<?> clazz, String typeName, Class<?> existingClass, String existingName) {
    assertNull(existingClass, "{} and {} have the same type name '{}'. Use an unique @{} annotation value.", clazz, existingClass, typeName, IdTypeName.class.getSimpleName());
    assertNull(existingName, "{} is annotated with @{} value '{}', but was already registered with type name '{}'. Register each class only once.", clazz, IdTypeName.class.getSimpleName(), typeName, existingName);
  }

  /**
   * @return the type name of the id class as defined by the {@link IdTypeName} annotation or <code>null</code> if the
   *         annotation is not present.
   */
  public String getTypeName(Class<? extends IId> idClass) {
    return m_classToNameMap.get(idClass);
  }

  /**
   * Convenience method for {@code IdInventory.getTypeName(id.getClass())}.
   *
   * @return the type name of the {@link IId} as defined by the {@link IdTypeName} annotation or <code>null</code> if
   *         the annotation is not present.
   */
  public String getTypeName(IId id) {
    if (id == null) {
      return null;
    }
    return getTypeName(id.getClass());
  }

  /**
   * @return id class which declares {@link IdTypeName} with {@code typeName}
   */
  public Class<? extends IId> getIdClass(String typeName) {
    return m_nameToClassMap.get(typeName);
  }
}
