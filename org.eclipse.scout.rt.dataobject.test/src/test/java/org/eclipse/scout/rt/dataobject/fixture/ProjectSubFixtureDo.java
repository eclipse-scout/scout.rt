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

public class ProjectSubFixtureDo extends ProjectFixtureDo {

  public DoValue<Integer> count2() {
    return doValue("count2");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ProjectSubFixtureDo withCount2(Integer count2) {
    count2().set(count2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCount2() {
    return count2().get();
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public ProjectSubFixtureDo withCount(Integer count) {
    count().set(count);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public ProjectSubFixtureDo withName(String name) {
    name().set(name);
    return this;
  }
}
