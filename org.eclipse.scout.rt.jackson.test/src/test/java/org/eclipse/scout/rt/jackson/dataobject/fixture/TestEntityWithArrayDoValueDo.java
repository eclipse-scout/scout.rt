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

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestEntityWithArrayDoValue")
public class TestEntityWithArrayDoValueDo extends DoEntity {

  public DoValue<String[]> stringArrayAttribute() {
    return doValue("stringArrayAttribute");
  }

  public DoValue<TestItemDo[]> itemDoArrayAttribute() {
    return doValue("itemDoArrayAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithArrayDoValueDo withStringArrayAttribute(String[] stringArrayAttribute) {
    stringArrayAttribute().set(stringArrayAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String[] getStringArrayAttribute() {
    return stringArrayAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithArrayDoValueDo withItemDoArrayAttribute(TestItemDo[] itemDoArrayAttribute) {
    itemDoArrayAttribute().set(itemDoArrayAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestItemDo[] getItemDoArrayAttribute() {
    return itemDoArrayAttribute().get();
  }
}
