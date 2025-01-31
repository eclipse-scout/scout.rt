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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public final class TypeFactoryUtility {

  private TypeFactoryUtility() {
  }

  /**
   * @see #toAttributeType(ParameterizedType, JsonToken)
   */
  public static AttributeType toAttributeType(ParameterizedType parametrizedType) {
    return toAttributeType(parametrizedType, null);
  }

  /**
   * Extracts type information of a {@link DoValue}, {@link DoList}, {@link DoSet}, {@link DoCollection} and the
   * optional {@link JsonToken} if invoked during deserialization.
   *
   * @param parametrizedType
   *     type of declared {@link DoNode}
   * @param currentToken
   *     optional {@link JsonToken} of the deserialization in progress
   */
  public static AttributeType toAttributeType(ParameterizedType parametrizedType, JsonToken currentToken) {
    if (ObjectUtility.isOneOf(parametrizedType.getRawType(), DoList.class, DoSet.class, DoCollection.class)) {
      JavaType listItemsType = TypeFactory.defaultInstance().constructType(parametrizedType.getActualTypeArguments()[0]);
      return AttributeType.ofDoCollection(TypeFactory.defaultInstance().constructParametricType((Class<?>) parametrizedType.getRawType(), listItemsType));
    }
    else if (DoValue.class == parametrizedType.getRawType()) {
      Type typeArg = parametrizedType.getActualTypeArguments()[0];
      if (typeArg == Object.class && currentToken == JsonToken.START_ARRAY) {
        // special case: declared DoNode<Object> with list-typed value
        typeArg = DoList.class;
      }
      return AttributeType.ofDoValue(TypeFactory.defaultInstance().constructType(typeArg));
    }
    throw new PlatformException("Could not convert type {}, only DoValue<?>, DoList<?>, DoSet<?> and DoCollection<?> supported", parametrizedType);
  }
}
