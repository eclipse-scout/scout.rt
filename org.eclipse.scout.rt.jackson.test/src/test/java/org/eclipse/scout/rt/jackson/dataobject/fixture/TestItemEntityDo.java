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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestItemEntity")
public class TestItemEntityDo extends DoEntity {

  public DoValue<TestItemDo> item() {
    return doValue("item");
  }

  public DoValue<ITestBaseEntityDo> itemIfc() {
    return doValue("itemIfc");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemEntityDo withItem(TestItemDo item) {
    item().set(item);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo getItem() {
    return item().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemEntityDo withItemIfc(ITestBaseEntityDo itemIfc) {
    itemIfc().set(itemIfc);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ITestBaseEntityDo getItemIfc() {
    return itemIfc().get();
  }
}
