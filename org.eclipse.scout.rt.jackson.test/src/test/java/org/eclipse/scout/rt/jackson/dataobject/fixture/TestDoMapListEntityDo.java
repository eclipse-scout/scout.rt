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

import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoMapEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestDoMapListEntity")
public class TestDoMapListEntityDo extends DoMapEntity<List<TestItemDo>> {

  public DoValue<Integer> count() {
    return doValue("count");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoMapListEntityDo withCount(Integer count) {
    count().set(count);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCount() {
    return count().get();
  }
}
