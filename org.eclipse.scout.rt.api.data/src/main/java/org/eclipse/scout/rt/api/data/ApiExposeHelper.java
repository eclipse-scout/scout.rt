/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import java.lang.annotation.Annotation;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Helper class for accessing values from {@link ApiExposed} and {@link FieldName} annotations
 * and resolving object types through {@link IObjectTypeProvider}s.
 */
@ApplicationScoped
public class ApiExposeHelper {

  public static final String OBJECT_TYPE_ATTRIBUTE_NAME = "objectType";

  /**
   * @return if the given instance has the {@link ApiExposed} annotation set (directly or on one of its super classes).
   */
  public boolean hasApiExposedAnnotation(Object instance) {
    if (instance == null) {
      return false;
    }
    return hasApiExposedAnnotation(instance.getClass());
  }

  /**
   * @return if the given {@link IBean} has the {@link ApiExposed} annotation set (directly or on one of its super
   * classes).
   */
  public boolean hasApiExposedAnnotation(IBean<?> bean) {
    return getAnnotation(bean, ApiExposed.class) != null;
  }

  /**
   * @return if the given class has the {@link ApiExposed} annotation set (directly or on one of its super classes).
   */
  public boolean hasApiExposedAnnotation(Class<?> clazz) {
    return getAnnotation(clazz, ApiExposed.class) != null;
  }

  /**
   * @return The object type of the given instance. The object type is resolved using the registered {@link IObjectTypeProvider}s.
   * If no provider returns an object type, {@code null} is returned.
   */
  public String objectTypeOf(Object instance) {
    if (instance == null) {
      return null;
    }
    return objectTypeOf(instance.getClass());
  }

  /**
   * @return The object type of the given class. The object type is resolved using the registered {@link IObjectTypeProvider}s.
   * If no provider returns an object type, {@code null} is returned.
   */
  public String objectTypeOf(Class<?> clazz) {
    return BEANS.all(IObjectTypeProvider.class).stream()
        .map(provider -> provider.objectTypeOf(clazz))
        .filter(StringUtility::hasText)
        .findFirst()
        .orElse(null);
  }

  /**
   * Resolves the object type of the given instance (using {@link #objectTypeOf(Object)}) and writes the value to the
   * {@value #OBJECT_TYPE_ATTRIBUTE_NAME} attribute of the provided {@link IDoEntity}. If the DoEntity already contains
   * such an attribute, it is preserved (nothing is overwritten). This method does nothing if the instance or the
   * doEntity is {@code null}.
   *
   * @param instance
   *     The instance whose object type should be resolved.
   * @param doEntity
   *     The target {@link IDoEntity} that should receive the {@value #OBJECT_TYPE_ATTRIBUTE_NAME} value.
   */
  public void setObjectTypeToDo(Object instance, IDoEntity doEntity) {
    if (instance == null) {
      return;
    }
    setObjectTypeToDo(instance.getClass(), doEntity);
  }

  /**
   * Resolves the object type of the given class (using {@link #objectTypeOf(Class)}) and writes the value to the
   * {@value #OBJECT_TYPE_ATTRIBUTE_NAME} attribute of the provided {@link IDoEntity}. If the DoEntity already contains
   * such an attribute, it is preserved (nothing is overwritten). This method does nothing if the class or the doEntity
   * is {@code null}.
   *
   * @param fromClass
   *     The class whose object type should be resolved.
   * @param doEntity
   *     The target {@link IDoEntity} that should receive the {@value #OBJECT_TYPE_ATTRIBUTE_NAME} value.
   */
  public void setObjectTypeToDo(Class<?> fromClass, IDoEntity doEntity) {
    if (doEntity == null || fromClass == null) {
      return;
    }

    if (StringUtility.hasText(doEntity.get(OBJECT_TYPE_ATTRIBUTE_NAME, String.class))) {
      return; // already custom objectType present
    }
    String declaredObjectType = objectTypeOf(fromClass);
    if (StringUtility.hasText(declaredObjectType)) {
      doEntity.put(OBJECT_TYPE_ATTRIBUTE_NAME, declaredObjectType);
    }
  }

  /**
   * @return The value of the {@link FieldName} annotation of the instance given (declared directly on the class or one
   * of its super classes). If the annotation is not present or has no value {@code null} is returned.
   */
  public String fieldNameOf(Object instance) {
    if (instance == null) {
      return null;
    }
    return fieldNameOf(instance.getClass());
  }

  /**
   * @return The value of the {@link FieldName} annotation of the class given (declared directly on the class or one of
   * its super classes). If the annotation is not present or has no value {@code null} is returned.
   */
  public String fieldNameOf(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    FieldName fieldName = getAnnotation(clazz, FieldName.class);
    if (fieldName == null) {
      return null;
    }
    String name = fieldName.value();
    if (StringUtility.hasText(name)) {
      return name;
    }
    return null;
  }

  protected <A extends Annotation> A getAnnotation(IBean<?> bean, Class<? extends A> annotation) {
    if (bean == null) {
      return null;
    }
    return bean.getBeanAnnotation(annotation);
  }

  protected <A extends Annotation> A getAnnotation(Class<?> clazz, Class<? extends A> annotation) {
    if (clazz == null || annotation == null) {
      return null;
    }
    // ask bean first: is faster and supports more features (inherited annotations from interfaces)
    IBean<?> bean = BEANS.getBeanManager().optBean(clazz);
    A result = getAnnotation(bean, annotation);
    if (result != null) {
      return result;
    }
    return clazz.getAnnotation(annotation);
  }
}
