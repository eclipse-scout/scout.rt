/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.exception.PlatformException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public final class TypeFactoryUtility {

  private TypeFactoryUtility() {
  }

  /**
   * @return Jackson {@link JavaType} representing the declared {@link ParameterizedType} of {@link DoValue} or {@link DoList} type.
   */
  public static JavaType toJavaType(ParameterizedType parametrizedType) {
    if (DoList.class == parametrizedType.getRawType()) {
      JavaType listItemsType = TypeFactory.defaultInstance().constructType(parametrizedType.getActualTypeArguments()[0]);
      return TypeFactory.defaultInstance().constructParametricType((Class<?>) parametrizedType.getRawType(), listItemsType);
    }
    else if (DoValue.class == parametrizedType.getRawType()) {
      Type typeArg = parametrizedType.getActualTypeArguments()[0];
      return TypeFactory.defaultInstance().constructType(typeArg);
    }
    throw new PlatformException("Could not convert type {}, only DoValue<?> and DoList<?> supported", parametrizedType);
  }
}
