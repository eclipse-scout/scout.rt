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
package org.eclipse.scout.rt.dataobject.id;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@CreateImmediately // validate on startup
public class IdExternalFormatter {

  private static final Logger LOG = LoggerFactory.getLogger(IdExternalFormatter.class);

  protected final Map<String, Class<? extends IId<?>>> m_nameToClassMap = new HashMap<>();
  protected final Map<Class<? extends IId<?>>, String> m_classToNameMap = new HashMap<>();

  @PostConstruct
  protected void createClassCache() {
    for (IClassInfo classInfo : ClassInventory.get().getKnownAnnotatedTypes(IdTypeName.class)) {
      String typeName = (String) classInfo.getAnnotationValue(IdTypeName.class, "value");
      Assertions.assertNotNullOrEmpty(typeName, "Invalid value for @{} on {} (must not be null or empty)", IdTypeName.class.getSimpleName(), classInfo.resolveClass().getName());
      try {
        @SuppressWarnings("unchecked")
        Class<? extends IId<?>> idClass = (Class<? extends IId<?>>) classInfo.resolveClass().asSubclass(IId.class);
        Class<? extends IId<?>> registeredIdClass = m_nameToClassMap.put(typeName, idClass);
        String registeredTypeName = m_classToNameMap.put(idClass, typeName);
        checkDuplicateIdTypeNames(idClass, typeName, registeredIdClass, registeredTypeName);
      }
      catch (@SuppressWarnings("squid:S1166") ClassCastException e) {
        LOG.warn("Class {} is annotated with @{} but does not implement {}. Skipping class.", classInfo.resolveClass().getName(), IdTypeName.class.getSimpleName(), IId.class.getName());
      }
    }
    LOG.debug("Registered {} id types", m_nameToClassMap.size());
  }

  /**
   * Checks for classes with the same {@link IdTypeName} annotation values.
   */
  protected void checkDuplicateIdTypeNames(Class<?> clazz, String typeName, Class<?> existingClass, String existingName) {
    Assertions.assertNull(existingClass, "{} and {} have the same type name '{}'. Use an unique @{} annotation value.", clazz, existingClass, typeName, IdTypeName.class.getSimpleName());
    Assertions.assertNull(existingName, "{} is annotated with @{} value '{}', but was already registered with type name '{}'. Register each class only once.", clazz, IdTypeName.class.getSimpleName(), typeName, existingName);
  }

  /**
   * Returns a string in the format <code>"[type-name]:[raw-id]"</code>.
   * <ul>
   * <li><b>type-name</b> is computed by {@link #getName(IId)}.
   * <li><b>raw-id</b> is the unwrapped id (see {@link IId#unwrapAsString()}).
   * </ul>
   */
  public <ID extends IId<?>> String toExternalForm(ID id) {
    StringBuilder sb = new StringBuilder();
    sb.append(Assertions.assertNotNull(getTypeName(id), "Missing @{} in class {}", IdTypeName.class.getSimpleName(), id.getClass().getName()));
    sb.append(":");
    sb.append(id.unwrapAsString());
    return sb.toString();
  }

  /**
   * Parses a string in the format <code>"[type-name]:[raw-id]"</code>.
   *
   * @throws IllegalArgumentException
   *           if the given string does not match the expected format.
   * @throws ProcessingException
   *           If the referenced class is not found
   */
  public <ID extends IId<?>> ID fromExternalForm(String externalForm) {
    if (externalForm == null) {
      return null;
    }
    String[] tmp = externalForm.split(":");
    if (tmp.length != 2) {
      throw new IllegalArgumentException("externalForm '" + externalForm + "' is invalid");
    }
    String typeName = tmp[0];
    String rawId = tmp[1];
    Class<? extends IId<?>> idClass = m_nameToClassMap.get(typeName);
    if (idClass == null) {
      throw new ProcessingException("No class found for type name '{}'", typeName);
    }
    @SuppressWarnings("unchecked")
    ID id = (ID) BEANS.get(IdFactory.class).createFromString(idClass, rawId);
    return id;
  }

  /**
   * @return the type name of the id class as defined by the {@link IdTypeName} annotation or <code>null</code> if the
   *         annotation is not present.
   */
  public String getTypeName(Class<? extends IId<?>> idClass) {
    return m_classToNameMap.get(idClass);
  }

  /**
   * @return id class which declares {@link IdTypeName} with {@code typeName}
   */
  public Class<? extends IId<?>> getIdClass(String typeName) {
    return m_nameToClassMap.get(typeName);
  }

  /**
   * @return the type name of the {@link IId} as defined by the {@link IdTypeName} annotation or <code>null</code> if
   *         the annotation is not present.
   */
  @SuppressWarnings("unchecked")
  public String getTypeName(IId<?> id) {
    return getTypeName((Class<? extends IId<?>>) id.getClass());
  }
}
