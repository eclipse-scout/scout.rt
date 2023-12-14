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

import org.eclipse.scout.rt.dataobject.DoMapEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("TestDoMapString")
@TypeVersion(JacksonFixtureTypeVersions.JacksonFixture_1_0_0.class)
public class TestDoMapStringDo extends DoMapEntity<String> {

  public DoValue<Integer> count() {
    return doValue("count");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoMapStringDo withCount(Integer count) {
    count().set(count);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCount() {
    return count().get();
  }
}
