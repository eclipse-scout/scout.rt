/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.dto;

/**
 *
 */
public final class DtoUtility {

  private DtoUtility() {
  }

  public static Class<?> getDataAnnotationValue(Class<?> clazz) {
    while (clazz != null && !Object.class.equals(clazz)) {
      Data annotation = clazz.getAnnotation(Data.class);
      if (annotation != null) {
        Class<?> value = annotation.value();
        if (value != null && !Object.class.equals(value)) {
          return value;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }

}
