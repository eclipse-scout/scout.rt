/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectProjectFixtureTypeVersions.DataObjectProjectFixture_1_2_3_004;
import org.eclipse.scout.rt.platform.Replace;

@Replace
@TypeVersion(DataObjectProjectFixture_1_2_3_004.class)
public class ProjectFixtureDo extends ScoutFixtureDo {

  public DoValue<Integer> count() {
    return doValue("count");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ProjectFixtureDo withCount(Integer count) {
    count().set(count);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCount() {
    return count().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public ProjectFixtureDo withName(String name) {
    name().set(name);
    return this;
  }
}
