/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestGenericDoEntityMap")
public class TestGenericDoEntityMapDo<T extends Map<String, ? extends IDoEntity>> extends DoEntity {

  public DoValue<T> genericMapAttribute() {
    return doValue("genericMapAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDoEntityMapDo<T> withGenericMapAttribute(T genericMapAttribute) {
    genericMapAttribute().set(genericMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public T getGenericMapAttribute() {
    return genericMapAttribute().get();
  }
}
