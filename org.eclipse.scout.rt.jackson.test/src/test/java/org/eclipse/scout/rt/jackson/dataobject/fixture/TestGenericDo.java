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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestGeneric")
@SuppressWarnings("unchecked")
public class TestGenericDo<T> extends DoEntity {

  public DoValue<T> genericAttribute() {
    return doValue("genericAttribute");
  }

  public DoList<T> genericListAttribute() {
    return doList("genericListAttribute");
  }

  public DoValue<Map<String, T>> genericMapAttribute() {
    return doValue("genericMapAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericAttribute(T genericAttribute) {
    genericAttribute().set(genericAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public T getGenericAttribute() {
    return genericAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericListAttribute(Collection<? extends T> genericListAttribute) {
    genericListAttribute().updateAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericListAttribute(T... genericListAttribute) {
    genericListAttribute().updateAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<T> getGenericListAttribute() {
    return genericListAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericMapAttribute(Map<String, T> genericMapAttribute) {
    genericMapAttribute().set(genericMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, T> getGenericMapAttribute() {
    return genericMapAttribute().get();
  }
}
