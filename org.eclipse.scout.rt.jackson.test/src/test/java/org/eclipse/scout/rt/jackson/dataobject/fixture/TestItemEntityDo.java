/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import javax.annotation.Generated;

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
