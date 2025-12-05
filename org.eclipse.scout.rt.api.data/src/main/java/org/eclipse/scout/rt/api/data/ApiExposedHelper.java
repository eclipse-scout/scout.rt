/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Helper class to access values of {@link ApiExposed},{@link ObjectType} and {@link FieldName} annotation values.
 */
@ApplicationScoped
public class ApiExposedHelper {

  public static final String OBJECT_TYPE_ATTRIBUTE_NAME = "objectType";

  /**
   * @return if the given instance has the {@link ApiExposed} annotation with {@link ApiExposed#value()} = {@code true} set (directly or on one of its super classes).
   */
  public boolean isApiExposed(Object instance) {
    if (instance == null) {
      return false;
    }
    return isApiExposed(instance.getClass());
  }

  /**
   * @return if the given {@link IBean} has the {@link ApiExposed} annotation with {@link ApiExposed#value()} = {@code true} set (directly or on one of its super
   * classes).
   */
  public boolean isApiExposed(IBean<?> bean) {
    ApiExposed ann = getAnnotation(bean, ApiExposed.class);
    return ann != null && ann.value();
  }

  /**
   * @return if the given class has the {@link ApiExposed} annotation with {@link ApiExposed#value()} = {@code true} set (directly or on one of its super classes).
   */
  public boolean isApiExposed(Class<?> clazz) {
    ApiExposed ann = getApiExposedAnnotation(clazz);
    return ann != null && ann.value();
  }

  /**
   * @return if the given method has the {@link ApiExposed} annotation with {@link ApiExposed#value()} = {@code true} set (directly or its declaring class).
   */
  public boolean isApiExposed(Method method) {
    ApiExposed ann = getApiExposedAnnotation(method);
    return ann != null && ann.value();
  }

  /**
   * @return the {@link ApiExposed} annotation if defined on the class (if available bean manager is used to determine the annotation otherwise the class itself is tested)
   */
  public ApiExposed getApiExposedAnnotation(Class<?> clazz) {
    return getAnnotation(clazz, ApiExposed.class);
  }

  /**
   * @return the {@link ApiExposed} annotation if defined for the method, if not the declaring class is checked using {@link #getApiExposedAnnotation(Class)}
   */
  public ApiExposed getApiExposedAnnotation(Method method) {
    ApiExposed annotation = method.getAnnotation(ApiExposed.class);
    if (annotation != null) {
      return annotation;
    }

    Class<?> declaringClass = method.getDeclaringClass();
    return getApiExposedAnnotation(declaringClass);
  }

  /**
   * @return The value of the {@link ObjectType} annotation of the instance given (declared directly on the class or one
   * of its super classes). If the annotation is not present or has no value {@code null} is returned.
   */
  public String objectTypeOf(Object instance) {
    if (instance == null) {
      return null;
    }
    return objectTypeOf(instance.getClass());
  }

  /**
   * @return The value of the {@link ObjectType} annotation of the class given (declared directly on the class or one of
   * its super classes). If the annotation is not present or has no value {@code null} is returned.
   */
  public String objectTypeOf(Class<?> clazz) {
    ObjectType annotation = getAnnotation(clazz, ObjectType.class);
    if (annotation == null) {
      return null;
    }
    String objectType = annotation.value();
    if (StringUtility.hasText(objectType)) {
      return objectType;
    }
    return null;
  }

  /**
   * Reads the {@link ObjectType} annotation value of the instance given (using {@link #objectTypeOf(Object)}) and
   * writes the value to the {@value #OBJECT_TYPE_ATTRIBUTE_NAME} attribute in the {@link IDoEntity} given. If the
   * DoEntity already contains such an attribute, it is preserved (nothing is overwritten). This method does nothing if
   * the instance or the doEntity is {@code null}.
   *
   * @param instance
   *     The instance whose class has the {@link ObjectType} annotation that should be read.
   * @param doEntity
   *     The target {@link IDoEntity} that should receive the {@value #OBJECT_TYPE_ATTRIBUTE_NAME} annotation
   *     value.
   */
  public void setObjectTypeToDo(Object instance, IDoEntity doEntity) {
    if (instance == null) {
      return;
    }
    setObjectTypeToDo(instance.getClass(), doEntity);
  }

  /**
   * Reads the {@link ObjectType} annotation value of the class given (using {@link #objectTypeOf(Class)}) and writes
   * the value to the {@value #OBJECT_TYPE_ATTRIBUTE_NAME} attribute in the {@link IDoEntity} given. If the DoEntity
   * already contains such an attribute, it is preserved (nothing is overwritten). This method does nothing if the class
   * or the doEntity is {@code null}.
   *
   * @param fromClass
   *     The class that has the {@link ObjectType} annotation that should be read.
   * @param doEntity
   *     The target {@link IDoEntity} that should receive the {@value #OBJECT_TYPE_ATTRIBUTE_NAME} annotation
   *     value.
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
