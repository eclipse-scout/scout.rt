/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

/**
 * Test {@link DoEntity} with attributes of DO type using no TypeName.
 */
@TypeName("TestEntityWithAttributesWithoutTypeName")
public class TestEntityWithAttributesWithoutTypeNameDo extends DoEntity {

  public DoValue<TestWithoutTypeNameDo> valueAttribute() {
    return doValue("valueAttribute");
  }

  public DoList<TestWithoutTypeNameDo> listAttribute() {
    return doList("listAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithAttributesWithoutTypeNameDo withValueAttribute(TestWithoutTypeNameDo valueAttribute) {
    valueAttribute().set(valueAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestWithoutTypeNameDo getValueAttribute() {
    return valueAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithAttributesWithoutTypeNameDo withListAttribute(Collection<? extends TestWithoutTypeNameDo> listAttribute) {
    listAttribute().updateAll(listAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithAttributesWithoutTypeNameDo withListAttribute(TestWithoutTypeNameDo... listAttribute) {
    listAttribute().updateAll(listAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<TestWithoutTypeNameDo> getListAttribute() {
    return listAttribute().get();
  }
}
