/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import java.lang.annotation.Annotation;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Default {@link IObjectTypeProvider} implementation that resolves object types
 * from {@link ObjectType} annotations declared on classes.
 */
public class ObjectTypeAnnotationProvider implements IObjectTypeProvider {

  @Override
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
