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
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;

@TypeName("scout.TestEntityWithEnumMapKey")
public class TestEntityWithEnumMapKeyDo extends DoEntity {

  public DoValue<Map<FixtureEnum, String>> map() {
    return doValue("map");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithEnumMapKeyDo withMap(Map<FixtureEnum, String> map) {
    map().set(map);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<FixtureEnum, String> getMap() {
    return map().get();
  }
}
