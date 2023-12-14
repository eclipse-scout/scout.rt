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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.AttributeName;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestItemEx")
public class TestItemExDo extends TestItemDo {

  @Override
  @AttributeName("idEx")
  public DoValue<String> id() {
    return doValue("idEx");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestItemExDo withId(String id) {
    id().set(id);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public TestItemExDo withStringAttribute(String stringAttribute) {
    stringAttribute().set(stringAttribute);
    return this;
  }
}
