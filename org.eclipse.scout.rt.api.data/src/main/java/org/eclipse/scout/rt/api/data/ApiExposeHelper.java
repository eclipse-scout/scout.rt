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

@ApplicationScoped
public class ApiExposeHelper {

  public boolean hasApiExposedAnnotation(Object instance) {
    if (instance == null) {
      return false;
    }
    return hasApiExposedAnnotation(instance.getClass());
  }

  public boolean hasApiExposedAnnotation(IBean<?> bean) {
    return getAnnotation(bean, ApiExposed.class) != null;
  }

  public boolean hasApiExposedAnnotation(Class<?> clazz) {
    return getAnnotation(clazz, ApiExposed.class) != null;
  }

  public String objectTypeOf(Object instance) {
    if (instance == null) {
      return null;
    }
    return objectTypeOf(instance.getClass());
  }

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

  public void setObjectTypeToDo(Object instance, IDoEntity doEntity) {
    if (instance == null) {
      return;
    }
    setObjectTypeToDo(instance.getClass(), doEntity);
  }

  public void setObjectTypeToDo(Class<?> fromClass, IDoEntity doEntity) {
    if (doEntity == null || fromClass == null) {
      return;
    }
    String objectTypeAttributeName = "objectType";
    if (StringUtility.hasText(doEntity.get(objectTypeAttributeName, String.class))) {
      return; // already custom objectType present
    }
    String declaredObjectType = objectTypeOf(fromClass);
    if (StringUtility.hasText(declaredObjectType)) {
      doEntity.put(objectTypeAttributeName, declaredObjectType);
    }
  }

  public String fieldNameOf(Object instance) {
    if (instance == null) {
      return null;
    }
    return fieldNameOf(instance.getClass());
  }

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
    IBean<?> bean = BEANS.getBeanManager().optBean(clazz);
    A result = getAnnotation(bean, annotation);
    if (result != null) {
      return result;
    }
    return clazz.getAnnotation(annotation);
  }
}
