/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoMapEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

@TypeName("TestDoMapObject")
@TypeVersion(JacksonFixtureTypeVersions.JacksonFixture_1_0_0.class)
public class TestDoMapObjectDo extends DoMapEntity<Object> {

  public DoValue<Integer> count() {
    return doValue("count");
  }

  public DoValue<TestItemDo> namedItem() {
    return doValue("namedItem");
  }

  public DoValue<TestItem3Do> namedItem3() {
    return doValue("namedItem3");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoMapObjectDo withCount(Integer count) {
    count().set(count);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCount() {
    return count().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoMapObjectDo withNamedItem(TestItemDo namedItem) {
    namedItem().set(namedItem);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo getNamedItem() {
    return namedItem().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoMapObjectDo withNamedItem3(TestItem3Do namedItem3) {
    namedItem3().set(namedItem3);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItem3Do getNamedItem3() {
    return namedItem3().get();
  }
}
